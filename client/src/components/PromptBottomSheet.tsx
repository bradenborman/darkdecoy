import React, { useEffect, useState } from "react";

interface PromptBottomSheetProps {
  isOpen: boolean;
  onClose: () => void;
  onAccept: (real: string, decoy: string) => void;
}

interface AutoSuggestionResponse {
  real?: string;
  decoy?: string;
  category?: string;
}

export default function PromptBottomSheet({
  isOpen,
  onClose,
  onAccept
}: PromptBottomSheetProps) {
  const [category, setCategory] = useState("");
  const [customCategory, setCustomCategory] = useState("");
  const [status, setStatus] = useState("");
  const [isError, setIsError] = useState(false);
  const [loading, setLoading] = useState(false);
  const [real, setReal] = useState<string | null>(null);
  const [decoy, setDecoy] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen) {
      setCategory("");
      setCustomCategory("");
      setStatus("");
      setIsError(false);
      setLoading(false);
      setReal(null);
      setDecoy(null);
    }
  }, [isOpen]);

  function getChosenCategory() {
    if (category === "Custom category") {
      return customCategory.trim();
    }
    return category.trim();
  }

  async function requestSuggestion() {
    const chosen = getChosenCategory();
    if (!chosen) {
      setStatus("Choose a category first.");
      setIsError(false);
      return;
    }

    if (loading) return;

    setLoading(true);
    setStatus("Thinking");
    setIsError(false);
    let dots = 0;
    const interval = window.setInterval(() => {
      dots = (dots + 1) % 4;
      setStatus("Thinking" + ".".repeat(dots));
    }, 500);

    try {
      const res = await fetch(`/api/auto?category=${encodeURIComponent(chosen)}`);
      if (!res.ok) {
        throw new Error("Bad response");
      }
      const data = (await res.json()) as AutoSuggestionResponse;
      const realWord = data.real || "Unknown";
      const decoyWord = data.decoy || "Unknown";

      setReal(realWord);
      setDecoy(decoyWord);
      setStatus("If you like it, accept it or generate again.");
      setIsError(false);
    } catch (err) {
      setReal(null);
      setDecoy(null);
      setStatus("Could not generate right now. Try again.");
      setIsError(true);
    } finally {
      window.clearInterval(interval);
      setLoading(false);
    }
  }

  function handleAccept() {
    if (!real || !decoy) return;
    onAccept(real, decoy);
    onClose();
  }

  if (!isOpen) return null;

  const showCustom = category === "Custom category";

  return (
    <>
      <div className="sheet-overlay open" onClick={onClose} />
      <div className="bottom-sheet open" aria-hidden="false">
        <div className="sheet-grabber" />
        <div className="sheet-header">
          <h4>Let us pick your round</h4>
          <p>Choose a category or type your own and we will generate a main and decoy word.</p>
        </div>
        <div className="sheet-body">
          <div className="sheet-row">
            <span className="sheet-label">Quick categories</span>
            <select
              className="sheet-select"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="">Choose a category</option>
              <option value="Custom category">Custom category</option>
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

          {showCustom && (
            <div className="sheet-row">
              <span className="sheet-label">Or type a custom category</span>
              <input
                className="sheet-input"
                placeholder="Example: 90s sitcom characters"
                value={customCategory}
                onChange={(e) => setCustomCategory(e.target.value)}
              />
            </div>
          )}

          <div className="sheet-row">
            <button
              type="button"
              id="sheetGenerateBtn"
              onClick={requestSuggestion}
              disabled={loading}
            >
              {loading ? "Generating..." : "Generate suggestion"}
            </button>
            <div className={`sheet-status ${isError ? "error" : ""}`}>{status}</div>
          </div>

          {real && decoy && (
            <div className="sheet-result">
              <div className="sheet-result-title">Suggested round</div>
              <div className="sheet-result-item">Main: {real}</div>
              <div className="sheet-result-item">Decoy: {decoy}</div>
            </div>
          )}

          <div className="sheet-buttons">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Close
            </button>
            <button type="button" className="btn-secondary" onClick={requestSuggestion} disabled={loading}>
              Try again
            </button>
            <button type="button" onClick={handleAccept} disabled={!real || !decoy}>
              Accept
            </button>
          </div>
        </div>
      </div>
    </>
  );
}