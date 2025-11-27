package com.darkdecoy.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Lobby {

    private String id;
    private String mode;
    private String hostId;
    private boolean gameStarted = false;
    private List<Player> players = new ArrayList<>();
    private boolean impostorKnows = true;
    private String prompt;
    private String roundHostId;
    private String decoyPrompt;

    public Lobby(String id, String hostId, String mode) {
        this.id = id;
        this.hostId = hostId;
        this.mode = mode;
    }

}