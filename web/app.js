const state = {
  data: null,
  token: "",
  user: null,
  activePanel: "lookupPanel",
  lastCombat: "No duel has been run in this session.",
  lastStructured: null
};

const toolNames = {
  lookupPanel: "Lookup",
  rankingPanel: "Ranking",
  historyPanel: "History",
  recommendPanel: "Recommend",
  combatPanel: "Combat"
};

const lookupPlaceholders = {
  player: "e.g. Li Bai or P001",
  team: "e.g. Chang'an Blades or T001",
  hero: "e.g. Diaochan or H002"
};

const el = (id) => document.getElementById(id);

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

async function api(path, options = {}) {
  const started = performance.now();
  const headers = options.headers ? { ...options.headers } : {};
  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }
  let body = options.body;
  if (body && !(body instanceof URLSearchParams)) {
    body = new URLSearchParams(body);
  }
  if (body instanceof URLSearchParams) {
    headers["Content-Type"] = "application/x-www-form-urlencoded";
  }

  const response = await fetch(path, {
    method: options.method || "GET",
    headers,
    body
  });
  const payload = await response.json();
  payload._durationMs = Math.max(0, Math.round(performance.now() - started));
  if (!payload.ok) {
    const error = new Error(payload.message || "Request failed");
    error.durationMs = payload._durationMs;
    throw error;
  }
  return payload;
}

function setLoading(isLoading) {
  document.body.classList.toggle("loading", isLoading);
  document.querySelectorAll("button:not(.nav-pill)").forEach((button) => {
    button.disabled = isLoading;
  });
  if (!isLoading) {
    updateResultActions();
  }
}

function setStatus(message, tone = "ready") {
  const status = el("statusText");
  status.textContent = message;
  status.dataset.tone = tone;
}

function setReportTitle(title) {
  el("reportTitle").textContent = title;
}

function setLatency(durationMs) {
  el("latencyText").textContent = Number.isFinite(durationMs)
    ? `Loaded in ${durationMs}ms`
    : "No request yet";
}

function showReport(text, tone = "plain") {
  const shell = el("reportShell");
  shell.className = `report-shell ${tone}`;
  el("reportOutput").textContent = text;
  el("reportOutput").classList.remove("hidden");
  el("tableOutput").classList.add("hidden");
  el("tableOutput").innerHTML = "";
  state.lastStructured = null;
  updateResultActions();
}

function showStructuredResult(payload) {
  state.lastStructured = payload;
  const shell = el("reportShell");
  shell.className = "report-shell table";
  el("reportOutput").classList.add("hidden");
  el("tableOutput").classList.remove("hidden");
  el("tableOutput").innerHTML = renderTable(payload);
  updateResultActions();
}

function renderTable(payload) {
  const columns = Array.isArray(payload.columns) ? payload.columns : [];
  const rows = Array.isArray(payload.rows) ? payload.rows : [];
  if (columns.length === 0 || rows.length === 0) {
    return `<div class="empty-table">No rows returned.</div>`;
  }
  return `
    <table>
      <thead>
        <tr>${columns.map((column) => `<th scope="col">${escapeHtml(labelize(column))}</th>`).join("")}</tr>
      </thead>
      <tbody>
        ${rows.map((row) => `
          <tr>${columns.map((column) => `<td>${escapeHtml(formatCell(row[column]))}</td>`).join("")}</tr>
        `).join("")}
      </tbody>
    </table>
  `;
}

function labelize(value) {
  return String(value)
    .replace(/([A-Z])/g, " $1")
    .replace(/^./, (letter) => letter.toUpperCase());
}

function formatCell(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  return value;
}

function updateResultActions() {
  const hasStructuredRows = Boolean(state.lastStructured && Array.isArray(state.lastStructured.rows));
  el("csvExportBtn").disabled = !hasStructuredRows;
}

function showToast(message, type = "info") {
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.textContent = message;
  el("toastRegion").append(toast);
  window.setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(6px)";
    toast.style.transition = "opacity 150ms ease, transform 150ms ease";
  }, 2600);
  window.setTimeout(() => toast.remove(), 2850);
}

async function runReport(label, request) {
  state.lastStructured = null;
  updateResultActions();
  setReportTitle(`${label} Report`);
  setStatus("Running", "running");
  setLatency(null);
  showReport("Loading...", "loading");
  setLoading(true);
  try {
    const payload = await request();
    const output = payload.report || payload.message || "Done.";
    if (Array.isArray(payload.rows)) {
      setReportTitle(payload.title || `${label} Table`);
      showStructuredResult(payload);
    } else {
      showReport(output, output ? "plain" : "empty");
    }
    setStatus("Complete", "complete");
    setLatency(payload._durationMs);
    document.title = `${label} - Honor of Kings IMS`;
    showToast(`${label} complete`, "success");
    return payload;
  } catch (error) {
    showReport(error.message, "error");
    setStatus("Failed", "failed");
    setLatency(error.durationMs);
    showToast(error.message, "error");
    return null;
  } finally {
    setLoading(false);
  }
}

function option(value, label) {
  const item = document.createElement("option");
  item.value = value;
  item.textContent = label;
  return item;
}

function fillSelect(selectId, items, labelFn, valueFn = (item) => item.id) {
  const select = el(selectId);
  select.innerHTML = "";
  items.forEach((item) => select.append(option(valueFn(item), labelFn(item))));
}

function renderCounts() {
  const labels = [
    ["players", "Players", "green"],
    ["teams", "Teams", "blue"],
    ["heroes", "Heroes", "red"],
    ["equipment", "Equipment", "gold"],
    ["matches", "Matches", "blue"]
  ];
  el("countGrid").innerHTML = labels.map(([key, label, tone]) => `
    <div class="count-tile" data-tone="${tone}">
      <strong>${state.data.counts[key]}</strong>
      <span>${escapeHtml(label)}</span>
    </div>
  `).join("");
}

function compactRow(title, meta, tone = "blue") {
  const label = `${title}: ${meta}`;
  return `<div class="compact-row ${tone}" title="${escapeHtml(label)}"><strong>${escapeHtml(title)}</strong><span>${escapeHtml(meta)}</span></div>`;
}

function renderInsights() {
  if (!state.data) {
    return;
  }

  el("insightTitle").textContent = `${toolNames[state.activePanel]} Snapshot`;
  el("contextInsight").innerHTML = contextRows().join("");

  el("recentMatches").innerHTML = state.data.matches.slice(0, 4).map((match) => {
    const teamA = teamName(match.teamAId);
    const teamB = teamName(match.teamBId);
    const winner = teamName(match.winnerTeamId);
    return compactRow(`${match.date} (${timeAgo(match.date)})`, `${teamA} vs ${teamB}, winner ${winner}`);
  }).join("");

  const top = [...state.data.equipment].sort((a, b) => b.score - a.score).slice(0, 4);
  el("topEquipment").innerHTML = top.map((item) => (
    compactRow(item.name, `${item.type} score ${Number(item.score).toFixed(2)}`, "gold")
  )).join("");
}

function contextRows() {
  switch (state.activePanel) {
    case "rankingPanel":
      return rankingContext();
    case "historyPanel":
      return historyContext();
    case "recommendPanel":
      return recommendContext();
    case "combatPanel":
      return combatContext();
    case "lookupPanel":
    default:
      return lookupContext();
  }
}

function lookupContext() {
  const avgLevel = average(state.data.players.map((player) => player.level));
  const topPlayer = [...state.data.players].sort((a, b) => b.winRate - a.winRate)[0];
  const heroTypes = new Set(state.data.heroes.map((hero) => hero.type)).size;
  return [
    compactRow("Average player level", avgLevel.toFixed(1), "green"),
    compactRow("Top win rate", `${topPlayer.name} ${Number(topPlayer.winRate).toFixed(1)}%`, "blue"),
    compactRow("Hero classes", `${heroTypes} indexed types`, "red")
  ];
}

function rankingContext() {
  const topLevel = [...state.data.players].sort((a, b) => b.level - a.level)[0];
  const topWin = [...state.data.players].sort((a, b) => b.winRate - a.winRate)[0];
  const bestEquipment = [...state.data.equipment].sort((a, b) => b.score - a.score)[0];
  return [
    compactRow("Highest level", `${topLevel.name} level ${topLevel.level}`, "green"),
    compactRow("Best win rate", `${topWin.name} ${Number(topWin.winRate).toFixed(1)}%`, "blue"),
    compactRow("Best equipment", `${bestEquipment.name} score ${Number(bestEquipment.score).toFixed(2)}`, "gold")
  ];
}

function historyContext() {
  return state.data.teams.slice(0, 3).map((team) => {
    const stats = teamStats(team.id);
    return compactRow(team.name, `${stats.played} matches, ${stats.winRate.toFixed(1)}% win rate`, "blue");
  });
}

function recommendContext() {
  const player = findPlayer(el("recommendPlayer").value);
  const hero = findHero(el("recommendHero").value);
  return [
    compactRow("Selected player", player ? `${player.name}, ${player.heroIds.length} owned heroes` : "None", "green"),
    compactRow("Selected hero", hero ? `${hero.name}, ${hero.type}` : "None", "red"),
    compactRow("Compatible equipment", hero ? `${hero.compatibleEquipmentIds.length} options` : "None", "gold")
  ];
}

function combatContext() {
  const heroA = findHero(el("combatHeroA").value);
  const heroB = findHero(el("combatHeroB").value);
  return [
    compactRow("Side A", selectedOptionText("combatPlayerA") + (heroA ? ` with ${heroA.name}` : ""), "red"),
    compactRow("Side B", selectedOptionText("combatPlayerB") + (heroB ? ` with ${heroB.name}` : ""), "green"),
    compactRow("Last result", state.lastCombat, "gold")
  ];
}

function average(values) {
  if (values.length === 0) {
    return 0;
  }
  return values.reduce((sum, value) => sum + Number(value), 0) / values.length;
}

function teamStats(teamId) {
  const matches = state.data.matches.filter((match) => match.teamAId === teamId || match.teamBId === teamId);
  const wins = matches.filter((match) => match.winnerTeamId === teamId).length;
  return {
    played: matches.length,
    winRate: matches.length === 0 ? 0 : (wins / matches.length) * 100
  };
}

function selectedOptionText(selectId) {
  const select = el(selectId);
  return select.selectedOptions.length > 0 ? select.selectedOptions[0].textContent : "None";
}

function populateSelectors() {
  const playerLabel = (player) => `${player.name} (${player.id})`;
  const heroLabel = (hero) => `${hero.name} (${hero.id})`;
  const teamLabel = (team) => `${team.name} (${team.id})`;

  fillSelect("historyPlayer", state.data.players, playerLabel);
  fillSelect("historyTeam", state.data.teams, teamLabel);
  fillSelect("recommendPlayer", state.data.players, playerLabel);
  fillSelect("recommendHero", state.data.heroes, heroLabel);
  fillSelect("combatPlayerA", state.data.players, playerLabel);
  fillSelect("combatPlayerB", state.data.players, playerLabel);

  if (state.data.players.length > 5) {
    el("combatPlayerB").value = state.data.players[5].id;
  }
  updateCombatSide("A");
  updateCombatSide("B");
}

function updateCombatSide(side) {
  const player = findPlayer(el(`combatPlayer${side}`).value);
  const heroes = player
    ? player.heroIds.map(findHero).filter(Boolean)
    : state.data.heroes;
  fillSelect(`combatHero${side}`, heroes, (hero) => `${hero.name} (${hero.id})`);
  updateEquipmentSide(side);
  renderInsights();
}

function updateEquipmentSide(side) {
  const hero = findHero(el(`combatHero${side}`).value);
  const select = el(`combatEquipment${side}`);
  select.innerHTML = "";
  select.append(option("", "Auto-pick best compatible"));
  if (!hero) {
    return;
  }
  hero.compatibleEquipmentIds
    .map(findEquipment)
    .filter(Boolean)
    .forEach((item) => select.append(option(item.id, `${item.name} (${item.id})`)));
}

function renderUserState() {
  const loggedIn = Boolean(state.user);
  el("loginForm").classList.toggle("hidden", loggedIn);
  el("userBar").classList.toggle("hidden", !loggedIn);
  if (!loggedIn) {
    return;
  }
  el("userBadge").textContent = `${state.user.name} (${state.user.role})`;
  el("saveBtn").classList.toggle("hidden", state.user.role !== "ADMIN");
}

function findPlayer(id) {
  return state.data.players.find((player) => player.id === id);
}

function findHero(id) {
  return state.data.heroes.find((hero) => hero.id === id);
}

function findEquipment(id) {
  return state.data.equipment.find((item) => item.id === id);
}

function teamName(id) {
  const team = state.data.teams.find((item) => item.id === id);
  return team ? team.name : id;
}

function query(value) {
  return encodeURIComponent(value);
}

function timeAgo(dateString) {
  const date = new Date(`${dateString}T00:00:00`);
  if (Number.isNaN(date.getTime())) {
    return "date unknown";
  }
  const now = new Date();
  const dayMs = 24 * 60 * 60 * 1000;
  const days = Math.max(0, Math.floor((now - date) / dayMs));
  if (days === 0) {
    return "today";
  }
  if (days === 1) {
    return "1 day ago";
  }
  if (days < 30) {
    return `${days} days ago`;
  }
  const months = Math.floor(days / 30);
  return months === 1 ? "1 month ago" : `${months} months ago`;
}

function summarizeCombat(report) {
  const lines = report.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
  return lines.find((line) => line.startsWith("Winner:")) || lines[0] || "Combat complete.";
}

async function copyReport() {
  const text = state.lastStructured
    ? toCsv(state.lastStructured.columns || [], state.lastStructured.rows || [])
    : el("reportOutput").textContent.trim();
  if (!text || text === "Results will appear here.") {
    showToast("No report to copy", "error");
    return;
  }
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
    } else {
      fallbackCopy(text);
    }
    showToast(state.lastStructured ? "Table CSV copied" : "Report copied", "success");
  } catch (error) {
    showToast("Copy failed", "error");
  }
}

function fallbackCopy(text) {
  const area = document.createElement("textarea");
  area.value = text;
  area.setAttribute("readonly", "");
  area.style.position = "fixed";
  area.style.opacity = "0";
  document.body.append(area);
  area.select();
  document.execCommand("copy");
  area.remove();
}

function exportStructuredCsv() {
  if (!state.lastStructured || !Array.isArray(state.lastStructured.rows)) {
    showToast("No table rows to export", "error");
    return;
  }
  const csv = toCsv(state.lastStructured.columns || [], state.lastStructured.rows);
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `${csvFileBase(state.lastStructured.title || "results")}.csv`;
  document.body.append(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
  showToast("CSV exported", "success");
}

function toCsv(columns, rows) {
  const header = columns.map(csvCell).join(",");
  const body = rows.map((row) => columns.map((column) => csvCell(row[column])).join(","));
  return [header, ...body].join("\r\n");
}

function csvCell(value) {
  const text = String(value === null || value === undefined ? "" : value);
  return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
}

function csvFileBase(title) {
  return String(title).toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "") || "results";
}

function updateLookupPlaceholder() {
  el("lookupQuery").placeholder = lookupPlaceholders[el("lookupType").value] || "ID or name";
}

async function bootstrap() {
  setLoading(true);
  setStatus("Loading", "running");
  try {
    state.data = await api("/api/bootstrap");
    renderCounts();
    populateSelectors();
    renderInsights();
    renderUserState();
    showReport("Results will appear here.", "empty");
    setStatus("Ready", "ready");
    setLatency(null);
  } finally {
    setLoading(false);
  }
}

document.querySelectorAll(".nav-pill").forEach((button) => {
  button.addEventListener("click", () => {
    document.querySelectorAll(".nav-pill").forEach((item) => item.classList.remove("active"));
    document.querySelectorAll(".tool-panel").forEach((item) => item.classList.remove("active"));
    button.classList.add("active");
    el(button.dataset.target).classList.add("active");
    state.activePanel = button.dataset.target;
    if (el("reportShell").classList.contains("empty")) {
      setReportTitle(`${toolNames[state.activePanel]} Report`);
    }
    renderInsights();
  });
});

el("loginForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  setLoading(true);
  setStatus("Running", "running");
  try {
    const payload = await api("/api/login", {
      method: "POST",
      body: {
        username: el("username").value,
        password: el("password").value
      }
    });
    state.token = payload.token;
    state.user = payload.user;
    renderUserState();
    setReportTitle("Login Report");
    showReport(`Logged in as ${state.user.name} (${state.user.role}).`);
    setStatus("Complete", "complete");
    setLatency(payload._durationMs);
    document.title = "Logged in - Honor of Kings IMS";
    showToast("Login complete", "success");
  } catch (error) {
    showReport(error.message, "error");
    setStatus("Failed", "failed");
    setLatency(error.durationMs);
    showToast(error.message, "error");
  } finally {
    setLoading(false);
  }
});

el("logoutBtn").addEventListener("click", async () => {
  setLoading(true);
  try {
    await api("/api/logout", { method: "POST", body: {} });
  } catch {
    // Local logout still clears the browser state if the server token is gone.
  }
  state.token = "";
  state.user = null;
  renderUserState();
  setReportTitle("Login Report");
  showReport("Logged out.");
  setStatus("Ready", "ready");
  setLatency(null);
  document.title = "Honor of Kings IMS";
  showToast("Logged out");
  setLoading(false);
});

el("copyReportBtn").addEventListener("click", copyReport);
el("csvExportBtn").addEventListener("click", exportStructuredCsv);

el("lookupType").addEventListener("change", updateLookupPlaceholder);

el("saveBtn").addEventListener("click", () => runReport("Save", async () => {
  const payload = await api("/api/save", { method: "POST", body: {} });
  return { report: payload.message };
}));

el("lookupBtn").addEventListener("click", () => {
  const type = el("lookupType").value;
  const q = query(el("lookupQuery").value);
  runReport("Lookup", () => api(`/api/search/${type}?q=${q}`));
});

el("leaderBtn").addEventListener("click", () => {
  const mode = query(el("leaderMode").value);
  const limit = query(el("leaderLimit").value);
  runReport("Leaderboard", () => api(`/api/leaderboard?mode=${mode}&limit=${limit}&format=json`));
});

el("equipmentBtn").addEventListener("click", () => {
  const limit = query(el("leaderLimit").value);
  runReport("Equipment", () => api(`/api/equipment?limit=${limit}&format=json`));
});

el("historyType").addEventListener("change", () => {
  const isPlayer = el("historyType").value === "player";
  el("historyPlayerWrap").classList.toggle("hidden", !isPlayer);
  el("historyTeamWrap").classList.toggle("hidden", isPlayer);
  renderInsights();
});

el("historyBtn").addEventListener("click", () => {
  const limit = query(el("historyLimit").value);
  if (el("historyType").value === "player") {
    runReport("History", () => api(`/api/matches/player?playerId=${query(el("historyPlayer").value)}&limit=${limit}&format=json`));
  } else {
    runReport("History", () => api(`/api/matches/team?teamId=${query(el("historyTeam").value)}&limit=${limit}&format=json`));
  }
});

el("recommendHeroBtn").addEventListener("click", () => {
  runReport("Hero recommendations", () => api(`/api/recommend/heroes?playerId=${query(el("recommendPlayer").value)}&limit=5`));
});

el("recommendEquipmentBtn").addEventListener("click", () => {
  runReport("Equipment recommendations", () => api(`/api/recommend/equipment?heroId=${query(el("recommendHero").value)}&limit=5`));
});

["recommendPlayer", "recommendHero", "historyPlayer", "historyTeam", "leaderMode"].forEach((id) => {
  el(id).addEventListener("change", renderInsights);
});

["A", "B"].forEach((side) => {
  el(`combatPlayer${side}`).addEventListener("change", () => updateCombatSide(side));
  el(`combatHero${side}`).addEventListener("change", () => {
    updateEquipmentSide(side);
    renderInsights();
  });
  el(`combatEquipment${side}`).addEventListener("change", renderInsights);
});

el("combatBtn").addEventListener("click", async () => {
  const payload = await runReport("Combat", () => api("/api/combat", {
    method: "POST",
    body: {
      playerAId: el("combatPlayerA").value,
      heroAId: el("combatHeroA").value,
      equipmentAId: el("combatEquipmentA").value,
      playerBId: el("combatPlayerB").value,
      heroBId: el("combatHeroB").value,
      equipmentBId: el("combatEquipmentB").value
    }
  }));
  if (payload && payload.report) {
    state.lastCombat = summarizeCombat(payload.report);
    renderInsights();
  }
});

bootstrap().catch((error) => {
  showReport(error.message, "error");
  setStatus("Startup failed", "failed");
  showToast(error.message, "error");
});
