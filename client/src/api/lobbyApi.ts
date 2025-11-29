export async function createLobby(
  name,
  mode,
  prompt,
  decoyPrompt,
  impostorKnows
) {
  const params = new URLSearchParams();
  params.append("name", name);
  params.append("mode", mode);
  params.append("prompt", prompt);
  params.append("impostorKnows", impostorKnows ? "true" : "false");

  if (decoyPrompt?.trim()) {
    params.append("decoyPrompt", decoyPrompt.trim());
  }

  const res = await fetch("/api/lobby/create", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: params.toString()
  });

  if (!res.ok) throw new Error("Create lobby failed");

  return res.json();
}

export async function joinLobby(name, lobbyId) {
  const params = new URLSearchParams();
  params.append("name", name);
  params.append("lobbyId", lobbyId);

  const res = await fetch("/api/lobby/join", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: params.toString()
  });

  if (!res.ok) throw new Error("Join failed");

  return res.json();
}