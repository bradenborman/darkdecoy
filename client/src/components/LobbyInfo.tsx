import { Lobby } from "@types/api";

export default function LobbyInfo({ lobby }: { lobby: Lobby }) {
  const modeDisplay = lobby.mode === "dark" ? "In the Dark" : "With Decoy";
  const modeDescription =
    lobby.mode === "dark"
      ? "One player will be completely in the dark. They will not get the prompt. Their goal is to blend in without being caught."
      : "Everyone gets a word, but one player gets a decoy word that is close but not quite right. Try to spot who is faking it.";

  return (
    <div className="game-info">
      <div className="game-code">Game Code: {lobby.id}</div>
      <div className="mode">Mode: {modeDisplay}</div>
      <p className="description">{modeDescription}</p>
    </div>
  );
}