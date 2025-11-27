package com.darkdecoy.controller;

import com.darkdecoy.model.Lobby;
import com.darkdecoy.service.LobbyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name,
                         @RequestParam String mode,
                         Model model) {

        String playerId = UUID.randomUUID().toString();

        Lobby lobby = lobbyService.createLobby(name, mode, playerId);

        model.addAttribute("lobby", lobby);
        model.addAttribute("playerName", name);
        model.addAttribute("playerId", playerId); // ✅ Add this line
        model.addAttribute("modeDescription", getModeDescription(mode));
        model.addAttribute("modeDisplay", getModeDisplay(mode));

        return "lobby";
    }


    @PostMapping("/join")
    public String join(@RequestParam String name,
                       @RequestParam String lobbyId,
                       Model model) {

        if (lobbyId == null || lobbyId.isBlank() || !lobbyService.lobbyExists(lobbyId)) {
            model.addAttribute("errorMessage", "That lobby code doesn’t exist. Please double-check and try again.");
            return "error";
        }

        String playerId = UUID.randomUUID().toString();

        Lobby lobby = lobbyService.joinLobby(lobbyId, name, playerId);
        String mode = lobby.getMode() != null ? lobby.getMode() : "dark";

        model.addAttribute("lobby", lobby);
        model.addAttribute("playerName", name);
        model.addAttribute("playerId", playerId);
        model.addAttribute("modeDescription", getModeDescription(mode));
        model.addAttribute("modeDisplay", getModeDisplay(mode));
        return "lobby";
    }

    @PostMapping("/start")
    public String start(@RequestParam String lobbyId,
                        @RequestParam String prompt,
                        @RequestParam(required = false) String decoyPrompt,
                        @RequestParam String playerId,
                        @RequestParam(required = false, defaultValue = "true") boolean impostorKnows,
                        Model model) {
        try {
            Lobby lobby = lobbyService.startGame(lobbyId, prompt, decoyPrompt, playerId, impostorKnows);
            model.addAttribute("lobby", lobby);
            model.addAttribute("playerId", playerId);
            return "round";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }



    private String getModeDescription(String mode) {
        return switch (mode) {
            case "dark" -> "One player will be completely in the dark — they won’t get the prompt. Their goal is to blend in without being caught!";
            case "decoy" -> "Everyone gets a word, but one player gets a decoy word that’s close but not quite right. Try to spot who’s faking it!";
            default -> "Welcome to Dark Decoy — a game of bluffing, quick thinking, and catching your friends in the act.";
        };
    }

    private String getModeDisplay(String mode) {
        return mode.equals("dark") ? "In the Dark" : "With Decoy";
    }
}