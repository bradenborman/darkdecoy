package com.darkdecoy.model;

public record RoundResponse(
        Lobby lobby,
        Player player,
        boolean isRoundHost,
        boolean isImpostor,
        boolean impostorKnows,
        String visibleWord,
        String roleMessage
) {}
