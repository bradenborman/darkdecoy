import PlayerList from "@components/PlayerList";
import { Lobby } from "@types/api";

export default function PlayerListSection({ lobby }: { lobby: Lobby }) {
  return (
    <section>
      <h3>Players Connected: {lobby.players.length}</h3>
      <PlayerList players={lobby.players} />
    </section>
  );
}
