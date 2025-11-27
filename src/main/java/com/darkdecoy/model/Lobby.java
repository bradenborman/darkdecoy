package com.darkdecoy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Lobby {

    @Id
    private String id;

    private String mode;
    private String hostId;
    private boolean gameStarted = false;
    private boolean impostorKnows = false;
    private String prompt;
    private String roundHostId;
    private String decoyPrompt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    public Lobby() {}

    public Lobby(String id, String hostId, String mode) {
        this.id = id;
        this.hostId = hostId;
        this.mode = mode;
    }

}