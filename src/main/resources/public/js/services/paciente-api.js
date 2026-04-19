// Salvar um paciente
function salvarPacienteNoJava(paciente) {
  return fetch("/pacientes", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(paciente),
  });
}

// Buscar todos os pacientes
function buscarPacientesNoJava() {
  return fetch("/pacientes", {
    method: "GET",
    headers: { "Cache-Control": "no-cache" },
  }).then((resposta) => {
    if (!resposta.ok) throw new Error("Erro ao buscar dados");
    return resposta.json();
  });
}

// Buscar um paciente específico pelo ID
function buscarPacientePorIdNoJava(id) {
  return fetch(`/pacientes/${id}`).then((res) => {
    if (!res.ok) throw new Error("Não encontrado");
    return res.json();
  });
}

// Atualizar paciente existente
function atualizarPacienteNoJava(id, paciente) {
  return fetch(`/pacientes/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(paciente),
  });
}

// Excluir paciente (Exclusão Lógica)
function excluirPacienteNoJava(id) {
  return fetch(`/pacientes/${id}`, {
    method: "DELETE",
  });
}
