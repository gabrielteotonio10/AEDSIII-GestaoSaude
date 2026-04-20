// ── paciente-controller.js ────────────────────────────────────────────────────

// ── Alergias ──────────────────────────────────────────────────────────────────
let listaAlergiasTemporaria = [];

document
  .getElementById("btnAddAlergia")
  ?.addEventListener("click", adicionarAlergia);
document
  .getElementById("alergiaInput")
  ?.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
      e.preventDefault();
      adicionarAlergia();
    }
  });

function adicionarAlergia() {
  const input = document.getElementById("alergiaInput");
  const alergia = input.value.trim();
  if (!alergia) return;
  listaAlergiasTemporaria.push(alergia);
  renderizarAlergiasNaTela();
  input.value = "";
  input.focus();
}

function renderizarAlergiasNaTela() {
  const container = document.getElementById("alergiasTags");
  if (!container) return;
  container.innerHTML = "";
  listaAlergiasTemporaria.forEach(function (alergia, i) {
    const tag = document.createElement("div");
    tag.className = "tag";
    tag.innerHTML =
      alergia +
      ' <i class="fa-solid fa-xmark" onclick="removerAlergia(' +
      i +
      ')"></i>';
    container.appendChild(tag);
  });
}

window.removerAlergia = function (i) {
  listaAlergiasTemporaria.splice(i, 1);
  renderizarAlergiasNaTela();
};

// ── Modal ─────────────────────────────────────────────────────────────────────
function abrirModalPaciente() {
  document.getElementById("modalPaciente")?.classList.add("active");
}

function fecharModalCadastro() {
  document.getElementById("modalPaciente")?.classList.remove("active");
  document.getElementById("formPaciente")?.reset();
  document.getElementById("pacienteId").value = "";
  document.getElementById("modalTitle").innerHTML =
    '<i class="fa-solid fa-user-plus"></i> Cadastro de Paciente';
  listaAlergiasTemporaria = [];
  renderizarAlergiasNaTela();
}

document
  .getElementById("btnNovoPaciente")
  ?.addEventListener("click", function () {
    fecharModalCadastro();
    abrirModalPaciente();
  });
document
  .getElementById("btnFecharModal")
  ?.addEventListener("click", fecharModalCadastro);
document
  .getElementById("btnCancelarModal")
  ?.addEventListener("click", fecharModalCadastro);

document
  .getElementById("btnRecarregar")
  ?.addEventListener("click", function () {
    const inputBusca = document.getElementById("inputBuscaId");
    if (inputBusca) inputBusca.value = "";
    atualizarTabelaPacientes();
  });

// ── Tabela ────────────────────────────────────────────────────────────────────
async function atualizarTabelaPacientes() {
  const corpo = document.getElementById("listaPacientes");
  if (!corpo) return;

  try {
    const pacientes = await buscarPacientesNoJava();
    pacientes.reverse();
    corpo.innerHTML = "";

    if (pacientes.length === 0) {
      corpo.innerHTML =
        '<tr><td colspan="6" style="text-align:center">Nenhum paciente cadastrado.</td></tr>';
      return;
    }

    pacientes.forEach(function (p) {
      const tr = document.createElement("tr");
      const alergiasTexto =
        p.alergias && p.alergias.length > 0
          ? p.alergias.join(", ")
          : '<span class="text-muted">Nenhuma</span>';
      tr.innerHTML =
        "<td>" +
        p.id +
        "</td>" +
        "<td><strong>" +
        p.nome +
        "</strong></td>" +
        "<td>" +
        p.cpf +
        "</td>" +
        "<td>" +
        alergiasTexto +
        "</td>" +
        '<td><span class="status-badge ativo">Ativo</span></td>' +
        '<td><div class="actions">' +
        '<button class="btn-icon" onclick="editarPaciente(' +
        p.id +
        ')" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>' +
        '<button class="btn-icon delete" onclick="excluirPaciente(' +
        p.id +
        ')" title="Excluir"><i class="fa-solid fa-trash"></i></button>' +
        "</div></td>";
      corpo.appendChild(tr);
    });

    const el = function (id) {
      return document.getElementById(id);
    };
    if (el("statAtivos")) el("statAtivos").innerText = pacientes.length;
    if (el("statAlergias"))
      el("statAlergias").innerText = pacientes.filter(function (p) {
        return p.alergias && p.alergias.length > 0;
      }).length;
    if (el("statUltimoId"))
      el("statUltimoId").innerText =
        pacientes.length > 0
          ? Math.max.apply(
              null,
              pacientes.map(function (p) {
                return p.id;
              }),
            )
          : 0;
  } catch (err) {
    mostrarNotificacao(
      "Não foi possível carregar a lista de pacientes.",
      "erro",
    );
  }
}

// ── Criar / Editar ────────────────────────────────────────────────────────────
document
  .getElementById("formPaciente")
  ?.addEventListener("submit", async function (e) {
    e.preventDefault();
    const btn =
      document.getElementById("btnSalvarPaciente") ||
      e.target.querySelector('button[type="submit"]');
    const idInput = document.getElementById("pacienteId").value;
    const paciente = {
      nome: document.getElementById("nome").value,
      cpf: document.getElementById("cpf").value,
      alergias: listaAlergiasTemporaria,
    };

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...';

    try {
      let resposta;
      if (idInput === "") {
        resposta = await salvarPacienteNoJava(paciente);
      } else {
        resposta = await atualizarPacienteNoJava(idInput, paciente);
      }

      if (resposta.ok) {
        const msg =
          idInput === ""
            ? "Paciente cadastrado com sucesso!"
            : "Paciente atualizado com sucesso!";
        fecharModalCadastro(); // 1. fecha o modal
        await atualizarTabelaPacientes(); // 2. aguarda a tabela atualizar
        mostrarNotificacao(msg, "sucesso"); // 3. só então mostra a notificação
      } else {
        mostrarNotificacao("Erro ao salvar paciente.", "erro");
      }
    } catch (err) {
      mostrarNotificacao("Erro de conexão.", "erro");
    } finally {
      btn.disabled = false;
      btn.innerHTML = '<i class="fa-solid fa-save"></i> Gravar no Arquivo';
      btn.disabled = false;
    }
  });

// ── Editar ────────────────────────────────────────────────────────────────────
window.editarPaciente = async function (id) {
  try {
    const paciente = await buscarPacientePorIdNoJava(id);
    document.getElementById("pacienteId").value = paciente.id;
    document.getElementById("nome").value = paciente.nome;
    document.getElementById("cpf").value = paciente.cpf;
    document.getElementById("modalTitle").innerHTML =
      '<i class="fa-solid fa-pen"></i> Editar Paciente';
    listaAlergiasTemporaria = paciente.alergias ? [...paciente.alergias] : [];
    renderizarAlergiasNaTela();
    abrirModalPaciente();
  } catch (err) {
    mostrarNotificacao("Erro ao buscar dados do paciente.", "erro");
  }
};

// ── Excluir ───────────────────────────────────────────────────────────────────
let pacienteIdParaExcluir = null;
const modalConfirmacaoPac = document.getElementById("modalConfirmacao");

window.excluirPaciente = function (id) {
  pacienteIdParaExcluir = id;
  modalConfirmacaoPac?.classList.add("active");
};

document
  .getElementById("btnCancelarExclusao")
  ?.addEventListener("click", function () {
    modalConfirmacaoPac?.classList.remove("active");
    pacienteIdParaExcluir = null;
  });

document
  .getElementById("btnConfirmarExclusao")
  ?.addEventListener("click", async function () {
    if (pacienteIdParaExcluir === null) return;
    const btn = this;
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Excluindo...';

    try {
      const resposta = await excluirPacienteNoJava(pacienteIdParaExcluir);
      modalConfirmacaoPac?.classList.remove("active"); // 1. fecha modal
      pacienteIdParaExcluir = null;

      if (resposta.ok) {
        await atualizarTabelaPacientes(); // 2. atualiza tabela
        mostrarNotificacao("Paciente excluído logicamente.", "sucesso");
      } else {
        mostrarNotificacao("Erro ao excluir no servidor.", "erro");
      }
    } catch (err) {
      mostrarNotificacao("Erro de conexão.", "erro");
    } finally {
      btn.disabled = false;
      btn.innerHTML = "Sim, excluir";
    }
  });

// ── Busca por ID ──────────────────────────────────────────────────────────────
document
  .getElementById("btnBuscar")
  ?.addEventListener("click", async function () {
    const idBusca = document.getElementById("inputBuscaId").value;
    const corpo = document.getElementById("listaPacientes");
    if (!idBusca) {
      await atualizarTabelaPacientes();
      return;
    }

    try {
      const p = await buscarPacientePorIdNoJava(idBusca);
      const alergiasTexto =
        p.alergias && p.alergias.length > 0 ? p.alergias.join(", ") : "Nenhuma";
      corpo.innerHTML =
        "<tr><td>" +
        p.id +
        "</td><td><strong>" +
        p.nome +
        "</strong></td><td>" +
        p.cpf +
        "</td><td>" +
        alergiasTexto +
        "</td>" +
        '<td><span class="status-badge ativo">Ativo</span></td>' +
        '<td><div class="actions">' +
        '<button class="btn-icon" onclick="editarPaciente(' +
        p.id +
        ')"><i class="fa-solid fa-pen-to-square"></i></button>' +
        '<button class="btn-icon delete" onclick="excluirPaciente(' +
        p.id +
        ')"><i class="fa-solid fa-trash"></i></button>' +
        "</div></td></tr>";
      mostrarNotificacao("Paciente localizado!", "sucesso");
    } catch (err) {
      corpo.innerHTML =
        '<tr><td colspan="6" style="text-align:center">Nenhum paciente encontrado com este ID.</td></tr>';
      mostrarNotificacao("ID não encontrado no arquivo.", "aviso");
    }
  });
