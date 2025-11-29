import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "@styles/error.scss";

interface ErrorPageProps {
  message?: string;
}

export default function ErrorPage({ message }: ErrorPageProps) {
  const navigate = useNavigate();
  const location = useLocation();

  const stateMessage =
    (location.state as { message?: string } | null)?.message;

  const finalMessage = message || stateMessage || "Something went wrong.";

  function goHome() {
    navigate("/");
  }

  return (
    <div className="error-root">
      <div className="error-box">
        <h1>Oops!</h1>
        <p>{finalMessage}</p>
        <button onClick={goHome}>Return Home</button>
      </div>
    </div>
  );
}
