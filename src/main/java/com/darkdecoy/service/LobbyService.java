package com.darkdecoy.service;

import com.darkdecoy.model.Lobby;
import com.darkdecoy.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyService {

    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Lobby createLobby(String hostName, String mode, String hostId) {
        String id = generateCode();
        Player host = new Player(hostId, hostName, false);
        Lobby lobby = new Lobby(id, hostId, mode);
        lobby.getPlayers().add(host);
        lobbies.put(id, lobby);
        return lobby;
    }


    public Lobby joinLobby(String lobbyId, String name, String playerId) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) throw new IllegalArgumentException("Lobby not found");

        Optional<Player> existing = lobby.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst();

        if (existing.isEmpty()) {
            Player player = new Player(playerId, name, false);
            lobby.getPlayers().add(player);
        } else {
            existing.get().setName(name);
        }

        messagingTemplate.convertAndSend("/topic/lobbyUpdates", lobby);
        return lobby;
    }

    public Lobby startGame(String lobbyId, String prompt, String decoyPrompt, String roundHostId) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) throw new IllegalArgumentException("Lobby not found");

        // Must have at least 4 players now
        if (lobby.getPlayers().size() < 4) {
            throw new IllegalStateException("At least 4 players are required to start the game.");
        }

        lobby.setPrompt(prompt);
        lobby.setDecoyPrompt(decoyPrompt);
        lobby.setGameStarted(true);
        lobby.setRoundHostId(roundHostId);

        // Reset impostors
        lobby.getPlayers().forEach(p -> p.setImpostor(false));

        // Filter out the round host (they're sitting out)
        List<Player> eligiblePlayers = new ArrayList<>();
        for (Player p : lobby.getPlayers()) {
            if (!p.getId().equals(roundHostId)) eligiblePlayers.add(p);
        }

        // Pick one impostor among the non-host players
        Player impostor = eligiblePlayers.get(new Random().nextInt(eligiblePlayers.size()));
        impostor.setImpostor(true);

        // Notify all clients to start the round
        messagingTemplate.convertAndSend("/topic/gameStart", lobby);

        return lobby;
    }

    public Lobby getLobby(String id) {
        return lobbies.get(id);
    }

    private String generateCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        } while (lobbies.containsKey(code));
        return code;
    }


    public boolean lobbyExists(String id) {
        return lobbies.containsKey(id);
    }
}
