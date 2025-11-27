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

    public Lobby createLobby(String hostName, String mode, String hostId) {
        String id = generateCode();
        Player host = new Player(hostId, hostName, false);
        Lobby lobby = new Lobby(id, hostId, mode);
        lobby.getPlayers().add(host);
        lobbyRepository.save(lobby);
        return lobby;
    }

    public Lobby joinLobby(String lobbyId, String name, String playerId) {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        lobby.getPlayers().removeIf(p -> p.getId().equals(playerId));
        Player player = new Player(playerId, name, false);
        lobby.getPlayers().add(player);

        lobbyRepository.save(lobby);
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, lobby); // scoped to lobby
        return lobby;
    }

    public Lobby startGame(String lobbyId, String prompt, String decoyPrompt, String roundHostId, boolean impostorKnows) {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));

        if (lobby.getPlayers().size() < 4) {
            throw new IllegalStateException("At least 4 players are required to start the game.");
        }

        lobby.setPrompt(prompt);
        lobby.setDecoyPrompt(decoyPrompt);
        lobby.setGameStarted(true);
        lobby.setRoundHostId(roundHostId);
        lobby.setImpostorKnows(impostorKnows);

        lobby.getPlayers().forEach(p -> p.setImpostor(false));

        List<Player> eligiblePlayers = new ArrayList<>();
        for (Player p : lobby.getPlayers()) {
            if (!p.getId().equals(roundHostId)) eligiblePlayers.add(p);
        }

        Player impostor = eligiblePlayers.get(new Random().nextInt(eligiblePlayers.size()));
        impostor.setImpostor(true);

        lobbyRepository.save(lobby);
        messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/start", lobby); // scoped to lobby
        return lobby;
    }


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

}