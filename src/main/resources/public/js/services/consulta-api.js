// ── consulta-api.js ──────────────────────────────────────────────────────────

function listarConsultasNoJava() {
  return fetch("/consultas", { headers: { "Cache-Control": "no-cache" } }).then(
    (r) => {
      if (!r.ok) throw new Error("Erro ao listar");
      return r.json();
    },
  );
}

function buscarConsultaPorIdNoJava(id) {
  return fetch(`/consultas/${id}`).then((r) => {
    if (!r.ok) throw new Error("Não encontrado");
    return r.json();
  });
}

function salvarConsultaNoJava(consulta) {
  return fetch("/consultas", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(consulta),
  });
}

function atualizarConsultaNoJava(id, consulta) {
  return fetch(`/consultas/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(consulta),
  });
}

function excluirConsultaNoJava(id) {
  return fetch(`/consultas/${id}`, { method: "DELETE" });
}

// ── consulta_procedimentos ────────────────────────────────────────────────────

function listarProcedimentosDaConsultaNoJava(idConsulta) {
  return fetch(`/consulta_procedimentos/consulta/${idConsulta}`).then((r) => {
    if (!r.ok) throw new Error("Erro");
    return r.json();
  });
}

function vincularProcedimentoConsultaNoJava(
  idConsulta,
  idProcedimento,
  observacao,
) {
  return fetch("/consulta_procedimentos", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ idConsulta, idProcedimento, observacao }),
  });
}

function desvincularProcedimentoConsultaNoJava(id) {
  return fetch(`/consulta_procedimentos/${id}`, { method: "DELETE" });
}
