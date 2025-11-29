import React from "react";
import { Player } from "@types/api";

interface PlayerListProps {
  players: Player[];
}

export default function PlayerList({ players }: PlayerListProps) {
  if (!players || players.length === 0) {
    return <p>No players yet. Share the code and wait for friends to join.</p>;
  }

  return (
    <ul className="player-list">
      {players.map((p) => (
        <li key={p.id}>
          <span className="player-name">{p.name}</span>
        </li>
      ))}
    </ul>
  );
}
