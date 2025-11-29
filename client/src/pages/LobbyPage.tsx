import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

import { Lobby, RoundResponse } from "@types/api";

import LobbyHeader from "@components/LobbyHeader";
import LobbyInfo from "@components/LobbyInfo";
import FooterBar from "@components/FooterBar";

import "@styles/lobby.scss";

export default function LobbyPage() {
  const [searchParams] = useSearchParams();
  const lobbyId = searchParams.get("lobbyId") ?? "";
  const playerId = searchParams.get("playerId") ?? "";

  const navigate = useNavigate();

  const [lobby, setLobby] = useState<Lobby | null>(null);
  const [round, setRound] = useState<RoundResponse | null>(null);

  const [error, setError] = useState<string | null>(null);
  const [loadingStart, setLoadingStart] = useState(false);

  const isHost = useMemo(() => {
    if (!lobby || !playerId) return false;
    return lobby.hostId === playerId;
  }, [lobby, playerId]);

  const playerCount = lobby?.players?.length ?? 0;
  const canStart = playerCount >= 4;

  // If round has been loaded we treat the game as started
  const roundStarted = !!round;

  // Load lobby on mount
  useEffect(() => {
    if (!lobbyId) return;

    async function loadLobby() {
      try {
        const res = await fetch(`/api/lobby/${lobbyId}`);
        if (!res.ok) throw new Error();

        const data = (await res.json()) as Lobby;
        setLobby(data);
      } catch {
        setError("Lobby not found or expired.");
      }
    }

    loadLobby();
  }, [lobbyId]);

  // WebSockets
  useEffect(() => {
    if (!lobbyId) return;

    const socket = new SockJS("/ws");
    const stompClient = new Client({
      webSocketFactory: () => socket as any,
      debug: () => {}
    });

    stompClient.onConnect = () => {
      // When lobby updates (players join / leave)
      stompClient.subscribe(`/topic/lobby/${lobbyId}`, (msg) => {
        try {
          const updatedLobby = JSON.parse(msg.body) as Lobby;
          setLobby(updatedLobby);
        } catch (e) {
          console.error("Failed to parse lobby update", e);
        }
      });

      // When game begins
      stompClient.subscribe(`/topic/lobby/${lobbyId}/start`, async (msg) => {
        try {
          // Update lobby from the message so gameStarted etc are correct
          try {
            const startedLobby = JSON.parse(msg.body) as Lobby;
            setLobby(startedLobby);
          } catch (e) {
            console.error("Failed to parse lobby start payload", e);
          }

          const res = await fetch(
            `/api/lobby/round-info?lobbyId=${lobbyId}&playerId=${playerId}`
          );
          if (!res.ok) throw new Error();

          const r = (await res.json()) as RoundResponse;
          setRound(r);
        } catch (e) {
          console.error(e);
          setError("Could not load round info.");
        }
      });
    };

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [lobbyId, playerId]);

  // Host starts the game
  async function handleStart(e: React.MouseEvent<HTMLButtonElement>) {
    e.preventDefault();
    if (!canStart || loadingStart) return;

    setLoadingStart(true);

    try {
      const params = new URLSearchParams({
        lobbyId,
        playerId
      });

      const res = await fetch("/api/lobby/start", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params.toString()
      });

      if (!res.ok) throw new Error();
      // Round will load via websocket subscription
    } catch (e) {
      console.error(e);
      setError("Could not start game.");
    } finally {
      setLoadingStart(false);
    }
  }

  function goHome() {
    navigate("/");
  }

  // ERROR VIEW
  if (error) {
    return (
      <div className="lobby-root">
        <LobbyHeader />
        <p className="description">{error}</p>
      </div>
    );
  }

  // LOADING LOBBY
  if (!lobby) {
    return (
      <div className="lobby-root">
        <LobbyHeader />
        <p className="description">Loading lobby...</p>
      </div>
    );
  }

  // ROUND VIEW
  if (roundStarted && round) {
    const {
      category,
      isRoundHost,
      isImpostor,
      impostorKnows,
      visibleWord
    } = round;

    const showHost = isRoundHost;
    const showImpostorView = isImpostor && impostorKnows;
    const showNormalView =
      (!isImpostor && !isRoundHost) || (isImpostor && !impostorKnows);

    return (
      <div className="round-root">
        <div className="card">
          <h1>Dark Decoy</h1>

          {category && (
            <p className="category-label">
              Category: <span>{category}</span>
            </p>
          )}

          {showHost && (
            <>
              <h2>You submitted this round</h2>
              <div className="word">Sit back and watch</div>
              <p>You are the host this round. Enjoy the chaos.</p>
            </>
          )}

          {showImpostorView && (
            <>
              <h2 className="impostor-alert">You are the Impostor</h2>
              <div className="word">{visibleWord}</div>
              <p>Try to blend in. They cannot know you are faking it.</p>
            </>
          )}

          {showNormalView && (
            <>
              <h2>Your Word</h2>
              <div className="word">{visibleWord}</div>
              <p>Find the impostor without revealing the word.</p>
            </>
          )}

          <button className="new-round" onClick={goHome}>
            New Game
          </button>
        </div>

        <footer>Dark Decoy © 2025</footer>
      </div>
    );
  }

 // NORMAL LOBBY VIEW
 return (
   <div className="lobby-root">
     <LobbyHeader />

     <div className="lobby-card">
      <div className="lobby-code-wrapper">
        <div className="lobby-code-label">Game Code</div>
        <div className="lobby-code-display">{lobby.id}</div>
      </div>


       <div className="lobby-details">
         <p className="lobby-mode">
           <strong>Mode:</strong>{" "}
           {lobby.mode === "decoy" ? "With Decoy" : "In the Dark"}
         </p>

         <p className="lobby-description">
           {lobby.mode === "decoy"
             ? "Everyone gets a word, but one player gets a decoy word that is close but not quite right. Try to spot who is faking it."
             : "Everyone gets the same word except one player who gets NOTHING. They are completely in the dark — try to find them."}
         </p>
       </div>

       <h3 className="player-count">{playerCount} players connected</h3>

       {isHost ? (
         <div className="start-container">
           {!canStart ? (
             <button className="disabled-button" disabled>
               Need 4 or more players to start
             </button>
           ) : (
             <button
               className="start-button"
               onClick={handleStart}
               disabled={loadingStart}
             >
               {loadingStart ? "Starting..." : "Start Game"}
             </button>
           )}
         </div>
       ) : (
         <p className="waiting-message">Waiting for host to start...</p>
       )}
     </div>

     <FooterBar />
   </div>
 );



}