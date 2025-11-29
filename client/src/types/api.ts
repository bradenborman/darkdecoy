export type GameMode = "dark" | "decoy";

export interface Player {
  id: string;
  name: string;
  impostor: boolean;
}

export interface Lobby {
  id: string;
  hostId: string;
  players: Player[];
  mode: GameMode;
  gameStarted: boolean;
  prompt?: string | null;
  decoyPrompt?: string | null;
  impostorKnows: boolean;
}

export interface RoundResponse {
  lobby: Lobby;
  player: Player;
  isRoundHost: boolean;
  isImpostor: boolean;
  impostorKnows: boolean;
  visibleWord: string;
  roleMessage: string;
  category?: string | null;
}

export interface LobbyJoinResponse {
  lobby: Lobby;
  playerId: string;
}