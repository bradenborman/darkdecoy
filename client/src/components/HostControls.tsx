import React from "react";

interface Props {
  lobbyId: string;
  playerId: string;
  lobbyMode: string;
  prompt: string;
  setPrompt: (v: string) => void;
  decoyPrompt: string;
  setDecoyPrompt: (v: string) => void;
  impostorKnows: boolean;
  setImpostorKnows: (v: boolean) => void;
  canStart: boolean;
  loadingStart: boolean;
  onSubmit: (e: React.FormEvent) => void;
  onOpenBottomSheet: () => void;
}

export default function HostControls({
  lobbyId,
  playerId,
  lobbyMode,
  prompt,
  setPrompt,
  decoyPrompt,
  setDecoyPrompt,
  impostorKnows,
  setImpostorKnows,
  canStart,
  loadingStart,
  onSubmit,
  onOpenBottomSheet
}: Props) {
  return (
    <form onSubmit={onSubmit}>
      <input type="hidden" name="lobbyId" value={lobbyId} />
      <input type="hidden" name="playerId" value={playerId} />

      <input
        id="promptInput"
        name="prompt"
        placeholder="Enter main word or prompt"
        required
        value={prompt}
        onChange={(e) => setPrompt(e.target.value)}
      />

      {lobbyMode === "decoy" && (
        <>
          <input
            id="decoyInput"
            name="decoyPrompt"
            placeholder="Enter decoy word for impostor"
            value={decoyPrompt}
            onChange={(e) => setDecoyPrompt(e.target.value)}
          />

          <label className="checkbox-container">
            <input
              type="checkbox"
              name="impostorKnows"
              checked={impostorKnows}
              onChange={(e) => setImpostorKnows(e.target.checked)}
            />
            Impostor knows they are the impostor
          </label>

          {/* AI feature temporarily disabled
          <button
            type="button"
            id="openAutoSheet"
            className="text-button"
            onClick={onOpenBottomSheet}
          >
            Need help thinking of words?
          </button>
          */}
        </>
      )}

      <button id="startButton" type="submit" disabled={!canStart || loadingStart}>
        {canStart ? (loadingStart ? "Starting..." : "Start Game") : "Need 4 or more players to start"}
      </button>
    </form>
  );
}
