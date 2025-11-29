import { Routes, Route } from "react-router-dom";
import HomePage from "@pages/HomePage";
import LobbyPage from "@pages/LobbyPage";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/lobby" element={<LobbyPage />} />
    </Routes>
  );
}