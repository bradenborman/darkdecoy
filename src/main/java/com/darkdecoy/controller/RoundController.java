package com.darkdecoy.controller;

import com.darkdecoy.model.Lobby;
import com.darkdecoy.model.Player;
import com.darkdecoy.service.LobbyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class RoundController {

    private final LobbyService lobbyService;

    public RoundController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/round")
    public String round(@RequestParam String lobbyId,
                        @RequestParam String playerId,
                        Model model) {

        Lobby lobby = lobbyService.getLobby(lobbyId);
        if (lobby == null) {
            model.addAttribute("errorMessage", "Lobby not found or expired.");
            return "error";
        }

        Optional<Player> playerOpt = lobby.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst();

        if (playerOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Player not found in this lobby.");
            return "error";
        }

        Player player = playerOpt.get();
        boolean isRoundHost = playerId.equals(lobby.getRoundHostId());
        boolean isImpostor = player.isImpostor();
        boolean impostorKnows = lobby.isImpostorKnows();

        String visibleWord;
        String roleMessage;

        if (isRoundHost) {
            // Round host sits out
            visibleWord = "(none)";
            roleMessage = "You submitted this round — sit back and watch!";
        } else if (isImpostor) {
            if (lobby.getDecoyPrompt() != null && !lobby.getDecoyPrompt().isBlank()) {
                visibleWord = lobby.getDecoyPrompt();
                if (impostorKnows) {
                    roleMessage = "You are the decoy impostor.";
                } else {
                    roleMessage = "Your word for this round.";
                }
            } else {
                // In the dark mode
                visibleWord = lobby.getPrompt();
                if (impostorKnows) {
                    roleMessage = "You are the dark impostor. You have no word!";
                } else {
                    roleMessage = "Your word for this round.";
                }
            }
        } else {
            visibleWord = lobby.getPrompt();
            roleMessage = "Your word for this round.";
        }

        model.addAttribute("lobby", lobby);
        model.addAttribute("player", player);
        model.addAttribute("visibleWord", visibleWord);
        model.addAttribute("roleMessage", roleMessage);
        model.addAttribute("isImpostor", isImpostor);
        model.addAttribute("isRoundHost", isRoundHost);
        model.addAttribute("impostorKnows", impostorKnows);

        return "round";
    }
}
