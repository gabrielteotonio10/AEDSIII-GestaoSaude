// ── toast.js ──────────────────────────────────────────────────────────────────
function mostrarNotificacao(mensagem, tipo) {
  tipo = tipo || "sucesso";
  var container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    container.className = "toast-container";
    document.body.appendChild(container);
  }
  var toast = document.createElement("div");
  toast.className = "toast " + tipo;
  var icones = {
    sucesso: "fa-circle-check",
    erro: "fa-circle-exclamation",
    aviso: "fa-triangle-exclamation",
  };
  toast.innerHTML =
    '<i class="fa-solid ' +
    (icones[tipo] || "fa-circle-check") +
    '"></i> <span>' +
    mensagem +
    "</span>";
  container.appendChild(toast);
  setTimeout(function () {
    toast.remove();
  }, 3500);
}
