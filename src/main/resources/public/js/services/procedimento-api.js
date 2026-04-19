// ── procedimento-api.js ──────────────────────────────────────────────────────

function listarProcedimentosNoJava() {
  return fetch("/procedimentos", {
    headers: { "Cache-Control": "no-cache" },
  }).then((r) => {
    if (!r.ok) throw new Error("Erro ao listar");
    return r.json();
  });
}

function buscarProcedimentoPorIdNoJava(id) {
  return fetch(`/procedimentos/${id}`).then((r) => {
    if (!r.ok) throw new Error("Não encontrado");
    return r.json();
  });
}

function salvarProcedimentoNoJava(proc) {
  return fetch("/procedimentos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(proc),
  });
}

function atualizarProcedimentoNoJava(id, proc) {
  return fetch(`/procedimentos/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(proc),
  });
}

function excluirProcedimentoNoJava(id) {
  return fetch(`/procedimentos/${id}`, { method: "DELETE" });
}
