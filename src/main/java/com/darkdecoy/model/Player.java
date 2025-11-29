package com.darkdecoy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Player {

    @Id
    private String id;
    private String name;
    private boolean impostor;
    private String assignedWord;

    public Player() {}

    public Player(String id, String name, boolean impostor) {
        this.id = id;
        this.name = name;
        this.impostor = impostor;
    }

}