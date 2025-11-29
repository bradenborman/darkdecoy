package com.darkdecoy.controller;

import com.darkdecoy.model.JoinLobbyResponse;
import com.darkdecoy.model.Lobby;
import com.darkdecoy.model.Player;
import com.darkdecoy.model.RoundResponse;
import com.darkdecoy.service.LobbyService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/create")
    public JoinLobbyResponse createLobby(@RequestParam String name,
                                         @RequestParam String mode,
                                         @RequestParam String prompt,
                                         @RequestParam(required = false) String decoyPrompt,
                                         @RequestParam(defaultValue = "true") boolean impostorKnows) {

        String playerId = UUID.randomUUID().toString();

        Lobby lobby = lobbyService.createLobby(
                name,
                mode,
                playerId,
                prompt,
                decoyPrompt,
                impostorKnows
        );

        return new JoinLobbyResponse(lobby, playerId);
    }


    @PostMapping("/join")
    public JoinLobbyResponse joinLobby(@RequestParam String name,
                                       @RequestParam String lobbyId) {

        if (!lobbyService.lobbyExists(lobbyId)) {
            throw new IllegalArgumentException("Lobby not found");
        }

        String playerId = UUID.randomUUID().toString();

        Lobby lobby = lobbyService.joinLobby(lobbyId, name, playerId);

        return new JoinLobbyResponse(lobby, playerId);
    }


    @GetMapping("/{lobbyId}")
    public Lobby getLobby(@PathVariable String lobbyId) {
        return lobbyService.getLobby(lobbyId);
    }

    @GetMapping("/round-info")
    public RoundResponse getRoundInfo(
            @RequestParam String lobbyId,
            @RequestParam String playerId
    ) {

        Lobby lobby = lobbyService.getLobby(lobbyId);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found");
        }

        Player player = lobby.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        boolean isHost = playerId.equals(lobby.getRoundHostId());
        boolean isImpostor = player.isImpostor();
        boolean impostorKnows = lobby.isImpostorKnows();

        String visibleWord;
        String roleMessage;

// HOST VIEW
        if (isHost) {
            visibleWord = "(none)";
            roleMessage = "You submitted this round.";
        }

// IN THE DARK MODE (mode = "dark")
        else if (lobby.getMode().equalsIgnoreCase("dark")) {

            if (isImpostor) {
                // impostor gets NOTHING
                visibleWord = "";
                roleMessage = "You are in the dark. You get no word!";
            } else {
                // normal players get the real prompt
                visibleWord = lobby.getPrompt();
                roleMessage = "Your word for this round.";
            }
        }

// DECOY MODE (mode = "decoy")
        else { // mode = decoy

            if (isImpostor) {
                // impostor sees decoy if provided, otherwise real word
                if (lobby.getDecoyPrompt() != null && !lobby.getDecoyPrompt().isBlank()) {
                    visibleWord = lobby.getDecoyPrompt();
                    roleMessage = "Try to blend in. They cannot know you're faking it.";
                } else {
                    visibleWord = lobby.getPrompt();
                    roleMessage = "Try to blend in. They cannot know you're faking it.";
                }
            } else {
                // normal players get real prompt
                visibleWord = lobby.getPrompt();
                roleMessage = "Your word for this round.";
            }
        }


        return new RoundResponse(
                lobby,
                player,
                isHost,
                isImpostor,
                impostorKnows,
                visibleWord,
                ""
        );
    }


    @PostMapping("/start")
    public Lobby startGame(@RequestParam String lobbyId,
                           @RequestParam String playerId) {

        lobbyService.startGame(lobbyId, playerId);

        return lobbyService.getLobby(lobbyId);
    }


}