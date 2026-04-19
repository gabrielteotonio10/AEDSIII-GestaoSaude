// ── consulta-controller.js ────────────────────────────────────────────────────

let cachePacientes = [];
let cacheMedicos = [];
let cacheProcedimentosDisponiveis = [];
let listaProcedimentosSelecionados = [];

// ── Helpers ───────────────────────────────────────────────────────────────────
function formatarDataHora(epoch) {
  if (!epoch) return "—";
  const d = new Date(epoch);
  return (
    d.toLocaleDateString("pt-BR") +
    " " +
    d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })
  );
}

function statusBadge(status) {
  const map = {
    Marcada: "status-marcada",
    Realizada: "status-realizada",
    Cancelada: "status-cancelada",
  };
  return (
    '<span class="status-badge ' +
    (map[status] || "") +
    '">' +
    status +
    "</span>"
  );
}

// ── Carrega selects ───────────────────────────────────────────────────────────
async function carregarSelects() {
  try {
    const [pacs, usrs, procs] = await Promise.all([
      fetch("/pacientes").then(function (r) {
        return r.json();
      }),
      fetch("/usuarios").then(function (r) {
        return r.json();
      }),
      fetch("/procedimentos").then(function (r) {
        return r.json();
      }),
    ]);

    cachePacientes = pacs;
    cacheMedicos = usrs.filter(function (u) {
      return u.papel === "Médico";
    });
    cacheProcedimentosDisponiveis = procs;

    const selPaciente = document.getElementById("consultaPaciente");
    if (selPaciente) {
      selPaciente.innerHTML =
        '<option value="" disabled selected>Selecione o paciente</option>';
      cachePacientes.forEach(function (p) {
        selPaciente.innerHTML +=
          '<option value="' +
          p.id +
          '">' +
          p.nome +
          " (CPF: " +
          p.cpf +
          ")</option>";
      });
    }

    const selMedico = document.getElementById("consultaMedico");
    if (selMedico) {
      selMedico.innerHTML =
        '<option value="" disabled selected>Selecione o médico</option>';
      cacheMedicos.forEach(function (m) {
        const esp = m.especialidade ? " — " + m.especialidade : "";
        selMedico.innerHTML +=
          '<option value="' + m.id + '">' + m.nome + esp + "</option>";
      });
    }

    const selProc = document.getElementById("consultaProcSelect");
    if (selProc) {
      selProc.innerHTML =
        '<option value="" disabled selected>Selecionar procedimento</option>';
      cacheProcedimentosDisponiveis.forEach(function (p) {
        selProc.innerHTML +=
          '<option value="' +
          p.id +
          '" data-nome="' +
          p.nomeExame +
          '">' +
          p.nomeExame +
          " — R$ " +
          parseFloat(p.preco).toFixed(2).replace(".", ",") +
          "</option>";
      });
    }
  } catch (err) {
    console.error("Erro ao carregar selects:", err);
  }
}

// ── Procedimentos no modal ────────────────────────────────────────────────────
document
  .getElementById("btnAdicionarProcConsulta")
  ?.addEventListener("click", function () {
    const sel = document.getElementById("consultaProcSelect");
    const obs = document.getElementById("consultaProcObs");
    if (!sel || !sel.value) {
      mostrarNotificacao("Selecione um procedimento.", "aviso");
      return;
    }

    const idProc = parseInt(sel.value);
    const nomeExame = sel.options[sel.selectedIndex].dataset.nome;
    const observacao = obs ? obs.value.trim() : "";

    if (
      listaProcedimentosSelecionados.find(function (p) {
        return p.idProcedimento === idProc;
      })
    ) {
      mostrarNotificacao("Procedimento já adicionado.", "aviso");
      return;
    }

    listaProcedimentosSelecionados.push({
      idProcedimento: idProc,
      nomeExame: nomeExame,
      observacao: observacao,
    });
    renderizarListaProcs();
    sel.value = "";
    if (obs) obs.value = "";
  });

function renderizarListaProcs() {
  const container = document.getElementById("procsSelecionados");
  if (!container) return;
  container.innerHTML = "";

  if (listaProcedimentosSelecionados.length === 0) {
    container.innerHTML =
      '<p class="text-muted" style="font-size:.85rem;">Nenhum procedimento adicionado.</p>';
    return;
  }

  listaProcedimentosSelecionados.forEach(function (p, i) {
    const tag = document.createElement("div");
    tag.className = "tag";
    tag.innerHTML =
      "<span><strong>" +
      p.nomeExame +
      "</strong>" +
      (p.observacao ? " — " + p.observacao : "") +
      "</span>" +
      '<i class="fa-solid fa-xmark" onclick="removerProcConsulta(' +
      i +
      ')"></i>';
    container.appendChild(tag);
  });
}

window.removerProcConsulta = function (i) {
  listaProcedimentosSelecionados.splice(i, 1);
  renderizarListaProcs();
};

// ── Modal ─────────────────────────────────────────────────────────────────────
function fecharModalConsulta() {
  document.getElementById("modalConsulta")?.classList.remove("active");
  document.getElementById("formConsulta")?.reset();
  document.getElementById("consultaId").value = "";
  listaProcedimentosSelecionados = [];
  renderizarListaProcs();
  document.getElementById("modalConsultaTitle").innerHTML =
    '<i class="fa-solid fa-calendar-plus"></i> Nova Consulta';
}

document
  .getElementById("btnFecharModalConsulta")
  ?.addEventListener("click", fecharModalConsulta);
document
  .getElementById("btnCancelarModalConsulta")
  ?.addEventListener("click", fecharModalConsulta);

document
  .getElementById("btnNovaConsulta")
  ?.addEventListener("click", async function () {
    fecharModalConsulta();
    await carregarSelects();
    document.getElementById("modalConsulta")?.classList.add("active");
  });

document
  .getElementById("btnRecarregarConsulta")
  ?.addEventListener("click", atualizarTabelaConsultas);

// ── Tabela ────────────────────────────────────────────────────────────────────
async function atualizarTabelaConsultas() {
  const corpo = document.getElementById("listaConsultas");
  if (!corpo) return;

  try {
    const [consultas, pacientes, usuarios] = await Promise.all([
      fetch("/consultas").then(function (r) {
        return r.json();
      }),
      fetch("/pacientes").then(function (r) {
        return r.json();
      }),
      fetch("/usuarios").then(function (r) {
        return r.json();
      }),
    ]);

    consultas.reverse();
    corpo.innerHTML = "";

    if (consultas.length === 0) {
      corpo.innerHTML =
        '<tr><td colspan="6" style="text-align:center">Nenhuma consulta cadastrada.</td></tr>';
    } else {
      const mapPac = {};
      pacientes.forEach(function (p) {
        mapPac[p.id] = p.nome;
      });
      const mapUsr = {};
      usuarios.forEach(function (u) {
        mapUsr[u.id] = u.nome;
      });

      consultas.forEach(function (c) {
        const tr = document.createElement("tr");
        tr.innerHTML =
          "<td>" +
          c.id +
          "</td>" +
          "<td>" +
          formatarDataHora(c.dataHora) +
          "</td>" +
          "<td>" +
          (mapPac[c.idPaciente] || "ID " + c.idPaciente) +
          "</td>" +
          "<td>" +
          (mapUsr[c.idUsuario] || "ID " + c.idUsuario) +
          "</td>" +
          "<td>" +
          statusBadge(c.status) +
          "</td>" +
          '<td><div class="actions">' +
          '<button class="btn-icon" onclick="verConsulta(' +
          c.id +
          ')" title="Ver"><i class="fa-solid fa-eye"></i></button>' +
          '<button class="btn-icon" onclick="editarConsulta(' +
          c.id +
          ')" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>' +
          '<button class="btn-icon delete" onclick="excluirConsulta(' +
          c.id +
          ')" title="Excluir"><i class="fa-solid fa-trash"></i></button>' +
          "</div></td>";
        corpo.appendChild(tr);
      });
    }

    const el = function (id) {
      return document.getElementById(id);
    };
    if (el("statTotalConsultas"))
      el("statTotalConsultas").innerText = consultas.length;
    if (el("statMarcadas"))
      el("statMarcadas").innerText = consultas.filter(function (c) {
        return c.status === "Marcada";
      }).length;
    if (el("statRealizadas"))
      el("statRealizadas").innerText = consultas.filter(function (c) {
        return c.status === "Realizada";
      }).length;
  } catch (err) {
    mostrarNotificacao("Erro ao carregar consultas.", "erro");
  }
}

// ── Criar / Editar ────────────────────────────────────────────────────────────
document
  .getElementById("formConsulta")
  ?.addEventListener("submit", async function (e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    const idInput = document.getElementById("consultaId").value;
    const dataInput = document.getElementById("consultaDataHora").value;
    const dataHora = dataInput ? new Date(dataInput).getTime() : Date.now();

    const consulta = {
      dataHora: dataHora,
      status: document.getElementById("consultaStatus").value,
      idPaciente: parseInt(document.getElementById("consultaPaciente").value),
      idUsuario: parseInt(document.getElementById("consultaMedico").value),
    };

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...';

    try {
      let consultaId = parseInt(idInput);

      if (!idInput) {
        // Criar nova
        const r = await salvarConsultaNoJava(consulta);
        if (!r.ok) {
          mostrarNotificacao("Erro ao cadastrar consulta.", "erro");
          return;
        }
        const msg = await r.text();
        consultaId = parseInt(msg.split("ID ")[1]);
      } else {
        // Atualizar existente
        const r = await atualizarConsultaNoJava(idInput, consulta);
        if (!r.ok) {
          mostrarNotificacao("Erro ao atualizar consulta.", "erro");
          return;
        }
        // Remove vínculos antigos
        const cpsAntigos =
          await listarProcedimentosDaConsultaNoJava(consultaId);
        await Promise.all(
          cpsAntigos.map(function (cp) {
            return desvincularProcedimentoConsultaNoJava(cp.id);
          }),
        );
      }

      // Salva novos vínculos
      await Promise.all(
        listaProcedimentosSelecionados.map(function (p) {
          return vincularProcedimentoConsultaNoJava(
            consultaId,
            p.idProcedimento,
            p.observacao,
          );
        }),
      );

      const msgFinal = idInput
        ? "Consulta atualizada!"
        : "Consulta cadastrada!";
      fecharModalConsulta();
      await atualizarTabelaConsultas();
      mostrarNotificacao(msgFinal, "sucesso");
    } catch (err) {
      mostrarNotificacao("Erro de conexão.", "erro");
    } finally {
      btn.disabled = false;
      btn.innerHTML = '<i class="fa-solid fa-save"></i> Gravar no Arquivo';
    }
  });

// ── Editar ────────────────────────────────────────────────────────────────────
window.editarConsulta = async function (id) {
  await carregarSelects();
  try {
    const consulta = await buscarConsultaPorIdNoJava(id);
    document.getElementById("consultaId").value = consulta.id;

    const dt = new Date(consulta.dataHora);
    const pad = function (n) {
      return String(n).padStart(2, "0");
    };
    document.getElementById("consultaDataHora").value =
      dt.getFullYear() +
      "-" +
      pad(dt.getMonth() + 1) +
      "-" +
      pad(dt.getDate()) +
      "T" +
      pad(dt.getHours()) +
      ":" +
      pad(dt.getMinutes());
    document.getElementById("consultaStatus").value = consulta.status;
    document.getElementById("consultaPaciente").value = consulta.idPaciente;
    document.getElementById("consultaMedico").value = consulta.idUsuario;

    const cps = await listarProcedimentosDaConsultaNoJava(id);
    listaProcedimentosSelecionados = cps.map(function (cp) {
      const proc = cacheProcedimentosDisponiveis.find(function (p) {
        return p.id === cp.idProcedimento;
      });
      return {
        idProcedimento: cp.idProcedimento,
        nomeExame: proc ? proc.nomeExame : "Procedimento " + cp.idProcedimento,
        observacao: cp.observacao || "",
      };
    });
    renderizarListaProcs();

    document.getElementById("modalConsultaTitle").innerHTML =
      '<i class="fa-solid fa-pen"></i> Editar Consulta';
    document.getElementById("modalConsulta")?.classList.add("active");
  } catch (err) {
    mostrarNotificacao("Erro ao carregar consulta.", "erro");
  }
};

// ── Ver detalhes ──────────────────────────────────────────────────────────────
window.verConsulta = async function (id) {
  try {
    const [consulta, pacientes, usuarios, procs] = await Promise.all([
      buscarConsultaPorIdNoJava(id),
      fetch("/pacientes").then(function (r) {
        return r.json();
      }),
      fetch("/usuarios").then(function (r) {
        return r.json();
      }),
      fetch("/procedimentos").then(function (r) {
        return r.json();
      }),
    ]);
    const cps = await listarProcedimentosDaConsultaNoJava(id);
    const mapProcs = {};
    procs.forEach(function (p) {
      mapProcs[p.id] = p.nomeExame;
    });

    const nomePaciente =
      (
        pacientes.find(function (p) {
          return p.id === consulta.idPaciente;
        }) || {}
      ).nome || "—";
    const nomeMedico =
      (
        usuarios.find(function (u) {
          return u.id === consulta.idUsuario;
        }) || {}
      ).nome || "—";

    const procsHtml =
      cps.length > 0
        ? cps
            .map(function (cp) {
              return (
                "<li><strong>" +
                (mapProcs[cp.idProcedimento] || cp.idProcedimento) +
                "</strong>" +
                (cp.observacao ? ": " + cp.observacao : "") +
                "</li>"
              );
            })
            .join("")
        : "<li>Nenhum procedimento vinculado.</li>";

    document.getElementById("verConsultaConteudo").innerHTML =
      '<div class="detalhe-grid">' +
      '<div><span class="label">ID</span><span class="valor">' +
      consulta.id +
      "</span></div>" +
      '<div><span class="label">Data/Hora</span><span class="valor">' +
      formatarDataHora(consulta.dataHora) +
      "</span></div>" +
      '<div><span class="label">Status</span><span class="valor">' +
      statusBadge(consulta.status) +
      "</span></div>" +
      '<div><span class="label">Paciente</span><span class="valor">' +
      nomePaciente +
      "</span></div>" +
      '<div><span class="label">Médico</span><span class="valor">' +
      nomeMedico +
      "</span></div>" +
      "</div>" +
      '<div style="margin-top:16px"><p class="label" style="margin-bottom:8px">Procedimentos:</p>' +
      '<ul style="padding-left:20px;line-height:2">' +
      procsHtml +
      "</ul></div>";

    document.getElementById("modalVerConsulta")?.classList.add("active");
  } catch (err) {
    mostrarNotificacao("Erro ao carregar detalhes.", "erro");
  }
};

document
  .getElementById("btnFecharVerConsulta")
  ?.addEventListener("click", function () {
    document.getElementById("modalVerConsulta")?.classList.remove("active");
  });

// ── Excluir ───────────────────────────────────────────────────────────────────
let consultaIdParaExcluir = null;
const modalConfirmConsulta = document.getElementById(
  "modalConfirmacaoConsulta",
);

window.excluirConsulta = function (id) {
  consultaIdParaExcluir = id;
  modalConfirmConsulta?.classList.add("active");
};

document
  .getElementById("btnCancelarExclusaoConsulta")
  ?.addEventListener("click", function () {
    modalConfirmConsulta?.classList.remove("active");
    consultaIdParaExcluir = null;
  });

document
  .getElementById("btnConfirmarExclusaoConsulta")
  ?.addEventListener("click", async function () {
    if (consultaIdParaExcluir === null) return;
    const btn = this;
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Excluindo...';

    try {
      const r = await excluirConsultaNoJava(consultaIdParaExcluir);
      modalConfirmConsulta?.classList.remove("active");
      consultaIdParaExcluir = null;

      if (r.ok) {
        await atualizarTabelaConsultas();
        mostrarNotificacao("Consulta excluída.", "sucesso");
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

// ── Filtro por status ─────────────────────────────────────────────────────────
document
  .getElementById("filtroStatusConsulta")
  ?.addEventListener("change", async function (e) {
    const filtro = e.target.value;
    const corpo = document.getElementById("listaConsultas");

    try {
      const [consultas, pacientes, usuarios] = await Promise.all([
        fetch("/consultas").then(function (r) {
          return r.json();
        }),
        fetch("/pacientes").then(function (r) {
          return r.json();
        }),
        fetch("/usuarios").then(function (r) {
          return r.json();
        }),
      ]);
      const mapPac = {};
      pacientes.forEach(function (p) {
        mapPac[p.id] = p.nome;
      });
      const mapUsr = {};
      usuarios.forEach(function (u) {
        mapUsr[u.id] = u.nome;
      });

      const filtradas = filtro
        ? consultas.filter(function (c) {
            return c.status === filtro;
          })
        : consultas;
      filtradas.reverse();
      corpo.innerHTML = "";

      filtradas.forEach(function (c) {
        const tr = document.createElement("tr");
        tr.innerHTML =
          "<td>" +
          c.id +
          "</td>" +
          "<td>" +
          formatarDataHora(c.dataHora) +
          "</td>" +
          "<td>" +
          (mapPac[c.idPaciente] || "ID " + c.idPaciente) +
          "</td>" +
          "<td>" +
          (mapUsr[c.idUsuario] || "ID " + c.idUsuario) +
          "</td>" +
          "<td>" +
          statusBadge(c.status) +
          "</td>" +
          '<td><div class="actions">' +
          '<button class="btn-icon" onclick="verConsulta(' +
          c.id +
          ')"><i class="fa-solid fa-eye"></i></button>' +
          '<button class="btn-icon" onclick="editarConsulta(' +
          c.id +
          ')"><i class="fa-solid fa-pen-to-square"></i></button>' +
          '<button class="btn-icon delete" onclick="excluirConsulta(' +
          c.id +
          ')"><i class="fa-solid fa-trash"></i></button>' +
          "</div></td>";
        corpo.appendChild(tr);
      });
    } catch (err) {
      mostrarNotificacao("Erro ao filtrar consultas.", "erro");
    }
  });
