// ── usuario-api.js ────────────────────────────────────────────────────────────

function cadastrarUsuarioNoJava(usuario) {
  return fetch("/usuarios", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(usuario),
  });
}

function logarNoJava(email, senha) {
  return fetch("/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: email, senha: senha }),
  });
}

function listarUsuariosNoJava() {
  return fetch("/usuarios", { headers: { "Cache-Control": "no-cache" } }).then(
    function (r) {
      if (!r.ok) throw new Error("Erro ao listar");
      return r.json();
    },
  );
}

function buscarUsuarioPorIdNoJava(id) {
  return fetch("/usuarios/" + id).then(function (r) {
    if (!r.ok) throw new Error("Não encontrado");
    return r.json();
  });
}

function atualizarUsuarioNoJava(id, usuario) {
  return fetch("/usuarios/" + id, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(usuario),
  });
}

function excluirUsuarioNoJava(id) {
  return fetch("/usuarios/" + id, { method: "DELETE" });
}
