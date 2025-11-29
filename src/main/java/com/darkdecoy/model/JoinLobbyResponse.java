package com.darkdecoy.model;

public record JoinLobbyResponse(
        Lobby lobby,
        String playerId
) {}
