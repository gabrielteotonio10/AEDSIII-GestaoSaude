// ── usuario-controller.js ─────────────────────────────────────────────────────

// ── Modal ─────────────────────────────────────────────────────────────────────
function fecharModalUsuario() {
  document.getElementById("modalUsuario")?.classList.remove("active");
  document.getElementById("formUsuario")?.reset();
  document.getElementById("usuarioId").value = "";
  document.getElementById("divEspUsuario").style.display = "none";
  const dica = document.getElementById("senhaDica");
  if (dica) dica.style.display = "none";
  document.getElementById("modalUsuarioTitle").innerHTML =
    '<i class="fa-solid fa-user-plus"></i> Novo Usuário';
}

document
  .getElementById("btnNovoUsuario")
  ?.addEventListener("click", function () {
    fecharModalUsuario();
    document.getElementById("modalUsuario")?.classList.add("active");
  });
document
  .getElementById("btnFecharModalUsuario")
  ?.addEventListener("click", fecharModalUsuario);
document
  .getElementById("btnCancelarModalUsuario")
  ?.addEventListener("click", fecharModalUsuario);

document
  .getElementById("usuarioPapel")
  ?.addEventListener("change", function (e) {
    document.getElementById("divEspUsuario").style.display =
      e.target.value === "Médico" ? "block" : "none";
  });

document
  .getElementById("btnRecarregarUsuario")
  ?.addEventListener("click", function () {
    document.getElementById("inputBuscaUsuario").value = "";
    atualizarTabelaUsuarios();
  });

// ── Tabela ────────────────────────────────────────────────────────────────────
async function atualizarTabelaUsuarios() {
  const corpo = document.getElementById("listaUsuarios");
  if (!corpo) return;

  try {
    const usuarios = await listarUsuariosNoJava();
    usuarios.reverse();
    corpo.innerHTML = "";

    if (usuarios.length === 0) {
      corpo.innerHTML =
        '<tr><td colspan="6" style="text-align:center">Nenhum usuário cadastrado.</td></tr>';
      return;
    }

    const usuarioLogado = JSON.parse(
      localStorage.getItem("usuarioHealthGest") || "null",
    );

    usuarios.forEach(function (u) {
      const ehLogado = usuarioLogado && u.id === usuarioLogado.id;
      const tr = document.createElement("tr");
      tr.innerHTML =
        "<td>" +
        u.id +
        (ehLogado ? ' <span class="badge-voce">você</span>' : "") +
        "</td>" +
        "<td><strong>" +
        u.nome +
        "</strong></td>" +
        "<td>" +
        u.email +
        "</td>" +
        '<td><span class="papel-badge papel-' +
        (u.papel || "").toLowerCase().replace("é", "e") +
        '">' +
        u.papel +
        "</span></td>" +
        "<td>" +
        (u.especialidade || '<span class="text-muted">—</span>') +
        "</td>" +
        '<td><div class="actions">' +
        '<button class="btn-icon" onclick="editarUsuario(' +
        u.id +
        ')" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>' +
        '<button class="btn-icon delete" onclick="excluirUsuario(' +
        u.id +
        ')" title="Excluir"' +
        (ehLogado ? ' disabled style="opacity:.4;cursor:not-allowed"' : "") +
        '><i class="fa-solid fa-trash"></i></button>' +
        "</div></td>";
      corpo.appendChild(tr);
    });

    const el = function (id) {
      return document.getElementById(id);
    };
    if (el("statTotalUsuarios"))
      el("statTotalUsuarios").innerText = usuarios.length;
    if (el("statMedicos"))
      el("statMedicos").innerText = usuarios.filter(function (u) {
        return u.papel === "Médico";
      }).length;
    if (el("statAdmins"))
      el("statAdmins").innerText = usuarios.filter(function (u) {
        return u.papel === "Administrador";
      }).length;
  } catch (err) {
    mostrarNotificacao("Erro ao carregar usuários.", "erro");
  }
}

// ── Criar / Editar ────────────────────────────────────────────────────────────
document
  .getElementById("formUsuario")
  ?.addEventListener("submit", async function (e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    const idInput = document.getElementById("usuarioId").value;
    const usuario = {
      nome: document.getElementById("usuarioNome").value,
      cpf: document.getElementById("usuarioCpf").value,
      email: document.getElementById("usuarioEmail").value,
      senha: document.getElementById("usuarioSenha").value || "",
      papel: document.getElementById("usuarioPapel").value,
      especialidade:
        document.getElementById("usuarioEspecialidade").value || "",
    };

    if (idInput === "" && !usuario.senha) {
      mostrarNotificacao("Informe uma senha para o novo usuário.", "aviso");
      return;
    }

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...';

    try {
      let r;
      if (idInput === "") {
        r = await cadastrarUsuarioNoJava(usuario);
      } else {
        r = await atualizarUsuarioNoJava(idInput, usuario);
      }

      if (r.ok) {
        const msg =
          idInput === ""
            ? "Usuário cadastrado com sucesso!"
            : "Usuário atualizado com sucesso!";
        fecharModalUsuario();
        await atualizarTabelaUsuarios();
        mostrarNotificacao(msg, "sucesso");
      } else if (r.status === 409) {
        mostrarNotificacao("E-mail já cadastrado.", "erro");
      } else {
        mostrarNotificacao("Erro ao salvar usuário.", "erro");
      }
    } catch (err) {
      mostrarNotificacao("Erro de conexão.", "erro");
    } finally {
      btn.disabled = false;
      btn.innerHTML = '<i class="fa-solid fa-save"></i> Gravar no Arquivo';
    }
  });

// ── Editar ────────────────────────────────────────────────────────────────────
window.editarUsuario = async function (id) {
  try {
    const u = await buscarUsuarioPorIdNoJava(id);
    document.getElementById("usuarioId").value = u.id;
    document.getElementById("usuarioNome").value = u.nome;
    document.getElementById("usuarioCpf").value = u.cpf;
    document.getElementById("usuarioEmail").value = u.email;
    document.getElementById("usuarioPapel").value = u.papel;
    document.getElementById("usuarioEspecialidade").value =
      u.especialidade || "";
    document.getElementById("usuarioSenha").value = "";
    const dica = document.getElementById("senhaDica");
    if (dica) dica.style.display = "inline";

    document.getElementById("divEspUsuario").style.display =
      u.papel === "Médico" ? "block" : "none";
    document.getElementById("modalUsuarioTitle").innerHTML =
      '<i class="fa-solid fa-pen"></i> Editar Usuário';
    document.getElementById("modalUsuario")?.classList.add("active");
  } catch (err) {
    mostrarNotificacao("Erro ao buscar usuário.", "erro");
  }
};

// ── Excluir ───────────────────────────────────────────────────────────────────
let usuarioIdParaExcluir = null;
const modalConfirmUsuario = document.getElementById("modalConfirmacaoUsuario");

window.excluirUsuario = function (id) {
  const logado = JSON.parse(
    localStorage.getItem("usuarioHealthGest") || "null",
  );
  if (logado && id === logado.id) {
    mostrarNotificacao("Não é possível excluir a própria conta.", "aviso");
    return;
  }
  usuarioIdParaExcluir = id;
  modalConfirmUsuario?.classList.add("active");
};

document
  .getElementById("btnCancelarExclusaoUsuario")
  ?.addEventListener("click", function () {
    modalConfirmUsuario?.classList.remove("active");
    usuarioIdParaExcluir = null;
  });

document
  .getElementById("btnConfirmarExclusaoUsuario")
  ?.addEventListener("click", async function () {
    if (usuarioIdParaExcluir === null) return;
    const btn = this;
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Excluindo...';

    try {
      const r = await excluirUsuarioNoJava(usuarioIdParaExcluir);
      modalConfirmUsuario?.classList.remove("active");
      usuarioIdParaExcluir = null;

      if (r.ok) {
        await atualizarTabelaUsuarios();
        mostrarNotificacao("Usuário excluído.", "sucesso");
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
  .getElementById("btnBuscarUsuario")
  ?.addEventListener("click", async function () {
    const idBusca = document.getElementById("inputBuscaUsuario").value;
    const corpo = document.getElementById("listaUsuarios");
    if (!idBusca) {
      await atualizarTabelaUsuarios();
      return;
    }

    try {
      const u = await buscarUsuarioPorIdNoJava(idBusca);
      corpo.innerHTML =
        "<tr><td>" +
        u.id +
        "</td><td><strong>" +
        u.nome +
        "</strong></td><td>" +
        u.email +
        "</td>" +
        '<td><span class="papel-badge">' +
        u.papel +
        "</span></td>" +
        "<td>" +
        (u.especialidade || "—") +
        "</td>" +
        '<td><div class="actions">' +
        '<button class="btn-icon" onclick="editarUsuario(' +
        u.id +
        ')"><i class="fa-solid fa-pen-to-square"></i></button>' +
        '<button class="btn-icon delete" onclick="excluirUsuario(' +
        u.id +
        ')"><i class="fa-solid fa-trash"></i></button>' +
        "</div></td></tr>";
      mostrarNotificacao("Usuário localizado!", "sucesso");
    } catch (err) {
      corpo.innerHTML =
        '<tr><td colspan="6" style="text-align:center">Usuário não encontrado.</td></tr>';
      mostrarNotificacao("ID não encontrado.", "aviso");
    }
  });
