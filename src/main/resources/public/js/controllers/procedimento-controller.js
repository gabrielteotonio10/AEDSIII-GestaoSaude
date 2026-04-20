// ── procedimento-controller.js ────────────────────────────────────────────────

// ── Modal ─────────────────────────────────────────────────────────────────────
function fecharModalProcedimento() {
  document.getElementById("modalProcedimento")?.classList.remove("active");
  document.getElementById("formProcedimento")?.reset();
  document.getElementById("procedimentoId").value = "";
  document.getElementById("modalProcTitle").innerHTML =
    '<i class="fa-solid fa-syringe"></i> Cadastro de Procedimento';
}

document
  .getElementById("btnNovoProcedimento")
  ?.addEventListener("click", function () {
    fecharModalProcedimento();
    document.getElementById("modalProcedimento")?.classList.add("active");
  });
document
  .getElementById("btnFecharModalProc")
  ?.addEventListener("click", fecharModalProcedimento);
document
  .getElementById("btnCancelarModalProc")
  ?.addEventListener("click", fecharModalProcedimento);

document
  .getElementById("btnRecarregarProc")
  ?.addEventListener("click", function () {
    document.getElementById("inputBuscaProc").value = "";
    atualizarTabelaProcedimentos();
  });

// ── Tabela ────────────────────────────────────────────────────────────────────
async function atualizarTabelaProcedimentos() {
  const corpo = document.getElementById("listaProcedimentos");
  if (!corpo) return;

  try {
    const procedimentos = await listarProcedimentosNoJava();
    procedimentos.reverse();
    corpo.innerHTML = "";

    if (procedimentos.length === 0) {
      corpo.innerHTML =
        '<tr><td colspan="4" style="text-align:center">Nenhum procedimento cadastrado.</td></tr>';
      return;
    }

    procedimentos.forEach(function (p) {
      const tr = document.createElement("tr");
      tr.innerHTML =
        "<td>" +
        p.id +
        "</td>" +
        "<td><strong>" +
        p.nomeExame +
        "</strong></td>" +
        "<td>R$ " +
        parseFloat(p.preco).toFixed(2).replace(".", ",") +
        "</td>" +
        '<td><div class="actions">' +
        '<button class="btn-icon" onclick="editarProcedimento(' +
        p.id +
        ')" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>' +
        '<button class="btn-icon delete" onclick="excluirProcedimento(' +
        p.id +
        ')" title="Excluir"><i class="fa-solid fa-trash"></i></button>' +
        "</div></td>";
      corpo.appendChild(tr);
    });

    const totalEl = document.getElementById("statTotalProcedimentos");
    if (totalEl) totalEl.innerText = procedimentos.length;

    const mediaEl = document.getElementById("statMediaPreco");
    if (mediaEl && procedimentos.length > 0) {
      const media =
        procedimentos.reduce(function (a, p) {
          return a + p.preco;
        }, 0) / procedimentos.length;
      mediaEl.innerText = "R$ " + media.toFixed(2).replace(".", ",");
    }
  } catch (err) {
    mostrarNotificacao("Erro ao carregar procedimentos.", "erro");
  }
}

// ── Criar / Editar ────────────────────────────────────────────────────────────
document
  .getElementById("formProcedimento")
  ?.addEventListener("submit", async function (e) {
    e.preventDefault();
    const btn =
      document.getElementById("btnSalvarProcedimento") ||
      e.target.querySelector('button[type="submit"]');
    const idInput = document.getElementById("procedimentoId").value;
    const proc = {
      nomeExame: document.getElementById("procNome").value,
      preco: parseFloat(document.getElementById("procPreco").value),
    };

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...';

    try {
      let r;
      if (idInput === "") {
        r = await salvarProcedimentoNoJava(proc);
      } else {
        r = await atualizarProcedimentoNoJava(idInput, proc);
      }

      if (r.ok) {
        const msg =
          idInput === ""
            ? "Procedimento cadastrado com sucesso!"
            : "Procedimento atualizado com sucesso!";
        fecharModalProcedimento();
        await atualizarTabelaProcedimentos();
        mostrarNotificacao(msg, "sucesso");
      } else {
        mostrarNotificacao("Erro ao salvar procedimento.", "erro");
      }
    } catch (err) {
      mostrarNotificacao("Erro de conexão.", "erro");
    } finally {
      btn.disabled = false;
      btn.innerHTML = '<i class="fa-solid fa-save"></i> Gravar no Arquivo';
    }
  });

// ── Editar ────────────────────────────────────────────────────────────────────
window.editarProcedimento = async function (id) {
  try {
    const proc = await buscarProcedimentoPorIdNoJava(id);
    document.getElementById("procedimentoId").value = proc.id;
    document.getElementById("procNome").value = proc.nomeExame;
    document.getElementById("procPreco").value = proc.preco;
    document.getElementById("modalProcTitle").innerHTML =
      '<i class="fa-solid fa-pen"></i> Editar Procedimento';
    document.getElementById("modalProcedimento")?.classList.add("active");
  } catch (err) {
    mostrarNotificacao("Erro ao buscar procedimento.", "erro");
  }
};

// ── Excluir ───────────────────────────────────────────────────────────────────
let procIdParaExcluir = null;
const modalConfirmProc = document.getElementById("modalConfirmacaoProc");

window.excluirProcedimento = function (id) {
  procIdParaExcluir = id;
  modalConfirmProc?.classList.add("active");
};

document
  .getElementById("btnCancelarExclusaoProc")
  ?.addEventListener("click", function () {
    modalConfirmProc?.classList.remove("active");
    procIdParaExcluir = null;
  });

document
  .getElementById("btnConfirmarExclusaoProc")
  ?.addEventListener("click", async function () {
    if (procIdParaExcluir === null) return;
    const btn = this;
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Excluindo...';

    try {
      const r = await excluirProcedimentoNoJava(procIdParaExcluir);
      modalConfirmProc?.classList.remove("active");
      procIdParaExcluir = null;

      if (r.ok) {
        await atualizarTabelaProcedimentos();
        mostrarNotificacao("Procedimento excluído.", "sucesso");
      } else {
        mostrarNotificacao("Erro ao excluir.", "erro");
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
  .getElementById("btnBuscarProc")
  ?.addEventListener("click", async function () {
    const idBusca = document.getElementById("inputBuscaProc").value;
    const corpo = document.getElementById("listaProcedimentos");
    if (!idBusca) {
      await atualizarTabelaProcedimentos();
      return;
    }

    try {
      const p = await buscarProcedimentoPorIdNoJava(idBusca);
      corpo.innerHTML =
        "<tr><td>" +
        p.id +
        "</td><td><strong>" +
        p.nomeExame +
        "</strong></td>" +
        "<td>R$ " +
        parseFloat(p.preco).toFixed(2).replace(".", ",") +
        "</td>" +
        '<td><div class="actions">' +
        '<button class="btn-icon" onclick="editarProcedimento(' +
        p.id +
        ')"><i class="fa-solid fa-pen-to-square"></i></button>' +
        '<button class="btn-icon delete" onclick="excluirProcedimento(' +
        p.id +
        ')"><i class="fa-solid fa-trash"></i></button>' +
        "</div></td></tr>";
      mostrarNotificacao("Procedimento localizado!", "sucesso");
    } catch (err) {
      corpo.innerHTML =
        '<tr><td colspan="4" style="text-align:center">Nenhum procedimento encontrado.</td></tr>';
      mostrarNotificacao("ID não encontrado.", "aviso");
    }
  });
