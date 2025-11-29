import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createLobby, joinLobby } from "@api/lobbyApi";
import FooterBar from "@components/FooterBar";
import "@styles/home.scss";

type GameMode = "dark" | "decoy";

export default function HomePage() {
  const [randomName, setRandomName] = useState("");
  const [joinLobbyId, setJoinLobbyId] = useState("");
  const [mode, setMode] = useState<GameMode>("decoy");

  const [showSettings, setShowSettings] = useState(false);

  const [prompt, setPrompt] = useState("");
  const [decoyPrompt, setDecoyPrompt] = useState("");
  const [impostorKnows, setImpostorKnows] = useState(true);

  const [loadingCreate, setLoadingCreate] = useState(false);
  const [loadingJoin, setLoadingJoin] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    setRandomName("Player" + Math.floor(1000 + Math.random() * 9000));
  }, []);

  async function handleCreateLobby(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoadingCreate(true);

    try {
      const { lobby, playerId } = await createLobby(
        randomName,
        mode,
        prompt,
        decoyPrompt,
        impostorKnows
      );

      navigate(
        "/lobby?lobbyId=" +
          encodeURIComponent(lobby.id) +
          "&playerId=" +
          encodeURIComponent(playerId)
      );
    } catch (err) {
      console.error(err);
      setError("Could not create game. Please try again.");
    } finally {
      setLoadingCreate(false);
    }
  }

  async function handleJoinGame(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    if (!joinLobbyId || joinLobbyId.length !== 3) {
      setError("Please enter a 3 character lobby code.");
      return;
    }

    setLoadingJoin(true);

    try {
      const { lobby, playerId } = await joinLobby(randomName, joinLobbyId);

      navigate(
        "/lobby?lobbyId=" +
          encodeURIComponent(lobby.id) +
          "&playerId=" +
          encodeURIComponent(playerId)
      );
    } catch (err) {
      console.error(err);
      setError("Could not join that lobby. Double check the code and try again.");
    } finally {
      setLoadingJoin(false);
    }
  }

  // ------------------------------------------------------------------
  // AI Bottom Sheet Logic
  // ------------------------------------------------------------------

  const [sheetOpen, setSheetOpen] = useState(false);
  const [sheetCategory, setSheetCategory] = useState("");
  const [sheetCustom, setSheetCustom] = useState("");
  const [sheetLoading, setSheetLoading] = useState(false);
  const [sheetStatus, setSheetStatus] = useState("");
  const [sheetReal, setSheetReal] = useState("");
  const [sheetDecoy, setSheetDecoy] = useState("");

  const isCustom = sheetCategory === "Custom";

  function openSheet() {
    setSheetOpen(true);
    setSheetStatus("");
    setSheetReal("");
    setSheetDecoy("");
  }

  function closeSheet() {
    setSheetOpen(false);
  }

  function getCategory() {
    if (isCustom) return sheetCustom.trim();
    return sheetCategory.trim();
  }

  function startThinking() {
    let dots = 0;
    setSheetStatus("Thinking");

    const interval = setInterval(() => {
      dots = (dots + 1) % 4;
      setSheetStatus("Thinking" + ".".repeat(dots));
    }, 500);

    return interval;
  }

  async function generateSuggestion() {
    const category = getCategory();
    if (!category) {
      setSheetStatus("Choose a category first");
      return;
    }

    if (sheetLoading) return;

    setSheetLoading(true);
    setSheetStatus("");
    setSheetReal("");
    setSheetDecoy("");

    const thinking = startThinking();

    try {
      const res = await fetch("/api/auto?category=" + encodeURIComponent(category));
      if (!res.ok) throw new Error();

      const data = await res.json();

      setSheetReal(data.real || "Unknown");
      setSheetDecoy(data.decoy || "Unknown");
      setSheetStatus("If you like it, Accept it or generate again");
    } catch {
      setSheetStatus("Could not generate right now. Try again");
    } finally {
      clearInterval(thinking);
      setSheetLoading(false);
    }
  }

  function acceptSuggestion() {
    if (sheetReal) setPrompt(sheetReal);
    if (sheetDecoy) setDecoyPrompt(sheetDecoy);
    closeSheet();
  }

  return (
    <div className="home-root">
      <header className="home-header">
        <h1>Dark Decoy</h1>
        <div className="header-accent"></div>
      </header>

      <main>
        {/* CREATE GAME */}
        <div
          className={`card create-card ${
            showSettings ? "move-center" : ""
          }`}
        >
          <h2>Create a Game</h2>

          {!showSettings && (
            <form
              onSubmit={(e) => {
                e.preventDefault();
                setShowSettings(true);
              }}
            >
              <label>Game Mode</label>
              <select
                value={mode}
                onChange={(e) => setMode(e.target.value as GameMode)}
              >
                <option value="dark">In the Dark</option>
                <option value="decoy">With Decoy</option>
              </select>

              <button type="submit">Select Game</button>
            </form>
          )}

          {showSettings && (
            <form onSubmit={handleCreateLobby}>
              <label>Real Prompt</label>
              <input
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                placeholder="Most people will get this word"
                required
              />

              {mode === "decoy" && (
                <>
                  <label>Decoy Prompt</label>
                  <input
                    value={decoyPrompt}
                    onChange={(e) => setDecoyPrompt(e.target.value)}
                    placeholder="Odd person out gets this word"
                  />

                  <div className="checkbox-container">
                    <input
                      type="checkbox"
                      checked={impostorKnows}
                      onChange={(e) => setImpostorKnows(e.target.checked)}
                    />
                    <span>Impostor knows the word</span>
                  </div>

                  <button
                    type="button"
                    onClick={openSheet}
                    className="text-button"
                  >
                    Need help thinking of words?
                  </button>
                </>
              )}

              <button type="submit" disabled={loadingCreate}>
                {loadingCreate ? "Creating..." : "Create Game"}
              </button>
            </form>
          )}
        </div>

        {/* JOIN GAME */}
        <div
          className={`card join-card ${showSettings ? "fade-out" : ""}`}
        >
          <h2>Join a Game</h2>
          <form onSubmit={handleJoinGame}>
            <label>Game Code</label>
            <input
              placeholder="ENTER 3 CHARACTER CODE"
              maxLength={3}
              minLength={3}
              value={joinLobbyId}
              onChange={(e) => setJoinLobbyId(e.target.value.toUpperCase())}
              required
            />

            <button type="submit" disabled={loadingJoin}>
              {loadingJoin ? "Joining..." : "Join Game"}
            </button>
          </form>

          <p className="help-text">
            Ask your friend for the code and jump in to deceive or deduce.
          </p>
        </div>
      </main>

      {error && <div className="home-error">{error}</div>}

      <FooterBar />

      {/* Bottom Sheet */}
      {sheetOpen && (
        <>
          <div className="sheet-overlay open" onClick={closeSheet}></div>
          <div className="bottom-sheet open">
            <div className="sheet-grabber"></div>

            <div className="sheet-header">
              <h4>Let us pick your round</h4>
              <p>Choose a category or type your own.</p>
            </div>

            <div className="sheet-body">
              <div className="sheet-row">
                <span className="sheet-label">Quick categories</span>
                <select
                  className="sheet-select"
                  value={sheetCategory}
                  onChange={(e) => setSheetCategory(e.target.value)}
                >
                  <option value="">Choose a category</option>
                  <option value="Custom">Custom category</option>
                  <option>Animals</option>
                  <option>Countries</option>
                  <option>US States</option>
                  <option>NFL Players</option>
                  <option>NBA Players</option>
                  <option>MLB Players</option>
                  <option>NHL Players</option>
                  <option>Famous Actors</option>
                  <option>Famous Singers</option>
                  <option>Famous Duos</option>
                  <option>Breakfast Foods</option>
                  <option>Vehicles</option>
                  <option>Brands</option>
                  <option>Cartoon Characters</option>
                  <option>Superheroes</option>
                  <option>Cities</option>
                  <option>Video Games</option>
                  <option>Animals With Fur</option>
                  <option>Sports Teams</option>
                </select>
              </div>

              {isCustom && (
                <div className="sheet-row">
                  <span className="sheet-label">Custom category</span>
                  <input
                    className="sheet-input"
                    placeholder="Example: 90s sitcom characters"
                    value={sheetCustom}
                    onChange={(e) => setSheetCustom(e.target.value)}
                  />
                </div>
              )}

              <div className="sheet-row">
                <button
                  type="button"
                  onClick={generateSuggestion}
                  disabled={sheetLoading}
                >
                  {sheetLoading ? "Generating..." : "Generate suggestion"}
                </button>
                <div className="sheet-status">{sheetStatus}</div>
              </div>

              {sheetReal && (
                <div className="sheet-result">
                  <div className="sheet-result-title">Suggested round</div>
                  <div>Main: {sheetReal}</div>
                  <div>Decoy: {sheetDecoy}</div>
                </div>
              )}

              <div className="sheet-buttons">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={closeSheet}
                >
                  Close
                </button>
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={generateSuggestion}
                  disabled={!sheetReal}
                >
                  Try again
                </button>
                <button
                  type="button"
                  onClick={acceptSuggestion}
                  disabled={!sheetReal}
                >
                  Accept
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}