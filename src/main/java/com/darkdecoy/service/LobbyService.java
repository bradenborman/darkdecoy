package com.darkdecoy.service;

import com.darkdecoy.model.Lobby;
import com.darkdecoy.model.Player;
import com.darkdecoy.repository.LobbyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LobbyService {

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    // --------------------------------------------
    // CREATE LOBBY (now includes prompt settings)
    // --------------------------------------------
    public Lobby createLobby(String hostName,
                             String mode,
                             String hostId,
                             String prompt,
                             String decoyPrompt,
                             boolean impostorKnows) {

        String id = generateCode();

        Player host = new Player(hostId, hostName, false);

        Lobby lobby = new Lobby(id, hostId, mode);
        lobby.getPlayers().add(host);

        // store round settings up front, new behavior
        lobby.setPrompt(prompt);
        lobby.setDecoyPrompt(decoyPrompt);
        lobby.setImpostorKnows(impostorKnows);

        lobbyRepository.save(lobby);
        return lobby;
    }


    // --------------------------------------------
    // JOIN LOBBY
    // --------------------------------------------
    public Lobby joinLobby(String lobbyId, String name, String playerId) {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        lobby.getPlayers().removeIf(p -> p.getId().equals(playerId));

        Player player = new Player(playerId, name, false);
        lobby.getPlayers().add(player);

        lobbyRepository.save(lobby);

        // send updated player list to lobby subscribers
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, lobby);

        return lobby;
    }


    // --------------------------------------------
    // START GAME (no prompt or decoy here anymore)
    // --------------------------------------------
    public Lobby startGame(String lobbyId, String hostId) {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        if (lobby.getPlayers().size() < 4) {
            throw new IllegalStateException("At least 4 players are required to start the game.");
        }

        lobby.setGameStarted(true);
        lobby.setRoundHostId(hostId);

        // clear impostor marks and previous assignments
        lobby.getPlayers().forEach(p -> {
            p.setImpostor(false);
            p.setAssignedWord(null);
        });

        // determine eligible impostors (host cannot be impostor)
        List<Player> eligibleImpostors = new ArrayList<>();
        for (Player p : lobby.getPlayers()) {
            if (!p.getId().equals(hostId)) {
                eligibleImpostors.add(p);
            }
        }

        // randomly select impostor
        Player impostor = eligibleImpostors.get(
                new Random().nextInt(eligibleImpostors.size())
        );
        impostor.setImpostor(true);

        // assign words
        String realWord = lobby.getPrompt();
        String decoyWord = lobby.getDecoyPrompt();

        for (Player p : lobby.getPlayers()) {
            if (p.isImpostor()) {
                p.setAssignedWord(decoyWord);
            } else {
                p.setAssignedWord(realWord);
            }
        }

        // persist lobby and players
        lobbyRepository.save(lobby);

        // notify all clients game started
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/start", lobby);

        return lobby;
    }


    // --------------------------------------------
    // UTILITY
    // --------------------------------------------
    public Lobby getLobby(String id) {
        return lobbyRepository.findById(id).orElse(null);
    }

    public boolean lobbyExists(String id) {
        return lobbyRepository.existsById(id);
    }

    private String generateCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        } while (lobbyRepository.existsById(code));
        return code;
    }


    public Map<String, Object> getPlayerWord(String lobbyId, String playerId) {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        if (!lobby.isGameStarted()) {
            throw new IllegalStateException("Game has not started yet.");
        }

        Player player = lobby.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid player ID."));

        return Map.of(
                "name", player.getName(),
                "impostor", player.isImpostor(),
                "assignedWord", player.getAssignedWord() == null ? "" : player.getAssignedWord()
        );
    }

}