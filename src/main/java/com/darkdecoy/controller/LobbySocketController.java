package com.darkdecoy.controller;

import com.darkdecoy.model.Lobby;
import com.darkdecoy.service.LobbyService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LobbySocketController {

    private final LobbyService lobbyService;

    public LobbySocketController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @MessageMapping("/updateLobby")
    @SendTo("/topic/lobbyUpdates")
    public Lobby broadcastLobby(Lobby lobby) {
        return lobbyService.getLobby(lobby.getId());
    }

}