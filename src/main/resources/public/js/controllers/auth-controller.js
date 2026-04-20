// ── auth-controller.js ───────────────────────────────────────────────────────

var formLogin = document.getElementById("formLogin");
var formCadastro = document.getElementById("formCadastro");
var formRecuperar = document.getElementById("formRecuperar");

// ── Alternância de telas ──────────────────────────────────────────────────────
document.getElementById("btnIrCadastro").addEventListener("click", function () {
  switchView(formCadastro);
});
document
  .getElementById("btnEsqueciSenha")
  .addEventListener("click", function () {
    switchView(formRecuperar);
  });
document
  .getElementById("btnVoltarLogin")
  .addEventListener("click", function () {
    switchView(formLogin);
  });

document
  .getElementById("btnCancelarCadastro")
  .addEventListener("click", function () {
    var nome = document.getElementById("cadNome").value;
    if (nome !== "") {
      if (
        confirm("Tem certeza que deseja cancelar? Os dados serão perdidos.")
      ) {
        formCadastro.reset();
        document.getElementById("divEspecialidade").style.display = "none";
        switchView(formLogin);
      }
    } else {
      switchView(formLogin);
    }
  });

function switchView(viewToShow) {
  document.querySelectorAll(".auth-form").forEach(function (f) {
    f.classList.remove("active");
  });
  viewToShow.classList.add("active");
}

// Especialidade só para médico
document.getElementById("cadPapel").addEventListener("change", function (e) {
  var divEsp = document.getElementById("divEspecialidade");
  if (e.target.value === "Médico") {
    divEsp.style.display = "block";
    document.getElementById("cadEspecialidade").required = true;
  } else {
    divEsp.style.display = "none";
    document.getElementById("cadEspecialidade").required = false;
    document.getElementById("cadEspecialidade").value = "";
  }
});

// ── Autenticação ──────────────────────────────────────────────────────────────
function verificarAutenticacao() {
  var usuario = JSON.parse(localStorage.getItem("usuarioHealthGest") || "null");
  var telaLogin = document.getElementById("telaLogin");
  var telaApp = document.getElementById("telaApp");

  if (usuario) {
    telaLogin.style.display = "none";
    telaApp.style.display = "block";
    preencherDadosUsuario(usuario);
    if (typeof atualizarTabelaPacientes === "function")
      atualizarTabelaPacientes();
  } else {
    telaLogin.style.display = "block";
    telaApp.style.display = "none";
  }
}

function preencherDadosUsuario(usuario) {
  var el = function (id) {
    return document.getElementById(id);
  };

  if (el("topbarNome")) el("topbarNome").textContent = usuario.nome || "—";
  if (el("topbarPapel")) el("topbarPapel").textContent = usuario.papel || "—";
  // Preenche o dropdown também
  if (el("dropdownNome")) el("dropdownNome").textContent = usuario.nome || "—";
  if (el("dropdownPapel"))
    el("dropdownPapel").textContent = usuario.papel || "—";

  // Avatar com inicial do nome
  var avatarEls = document.querySelectorAll(".avatar, .avatar-lg");
  if (usuario.nome) {
    var inicial =
      '<span style="font-size:1.1rem;font-weight:700;">' +
      usuario.nome.charAt(0).toUpperCase() +
      "</span>";
    avatarEls.forEach(function (av) {
      av.innerHTML = inicial;
    });
  }
}

document.addEventListener("DOMContentLoaded", verificarAutenticacao);

// ── LOGIN ─────────────────────────────────────────────────────────────────────
formLogin.addEventListener("submit", async function (e) {
  e.preventDefault();
  var btn = formLogin.querySelector('button[type="submit"]');
  btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Autenticando...';
  btn.disabled = true;

  try {
    var response = await logarNoJava(
      document.getElementById("loginEmail").value,
      document.getElementById("loginSenha").value,
    );
    if (response.ok) {
      var usuarioLogado = await response.json();
      localStorage.setItem("usuarioHealthGest", JSON.stringify(usuarioLogado));
      mostrarNotificacao("Login aprovado! Entrando...", "sucesso");
      setTimeout(function () {
        verificarAutenticacao();
      }, 1200);
    } else {
      mostrarNotificacao("E-mail ou senha incorretos.", "erro");
    }
  } catch (err) {
    mostrarNotificacao("Erro ao conectar com o servidor.", "erro");
  } finally {
    btn.innerHTML = 'Entrar <i class="fa-solid fa-arrow-right"></i>';
    btn.disabled = false;
  }
});

// ── CADASTRO ──────────────────────────────────────────────────────────────────
formCadastro.addEventListener("submit", async function (e) {
  e.preventDefault();
  var btn = formCadastro.querySelector('button[type="submit"]');
  var senha = document.getElementById("cadSenha").value;
  var confirmaSenha = document.getElementById("cadConfirmaSenha").value;

  if (senha !== confirmaSenha) {
    mostrarNotificacao("As senhas não coincidem.", "erro");
    return;
  }

  btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...';
  btn.disabled = true;

  var novoUsuario = {
    nome: document.getElementById("cadNome").value,
    cpf: document.getElementById("cadCpf").value,
    email: document.getElementById("cadEmail").value,
    senha: senha,
    papel: document.getElementById("cadPapel").value,
    especialidade: document.getElementById("cadEspecialidade").value || "",
  };

  try {
    var response = await cadastrarUsuarioNoJava(novoUsuario);
    if (response.ok) {
      mostrarNotificacao("Cadastro realizado! Faça seu login.", "sucesso");
      formCadastro.reset();
      document.getElementById("divEspecialidade").style.display = "none";
      switchView(formLogin);
    } else if (response.status === 409) {
      mostrarNotificacao("Este e-mail já está cadastrado.", "erro");
    } else {
      mostrarNotificacao("Erro ao criar usuário.", "erro");
    }
  } catch (err) {
    mostrarNotificacao("Erro de conexão.", "erro");
  } finally {
    btn.innerHTML = 'Criar Conta <i class="fa-solid fa-user-plus"></i>';
    btn.disabled = false;
  }
});

// ── RECUPERAR SENHA ───────────────────────────────────────────────────────────
formRecuperar.addEventListener("submit", async function (e) {
  e.preventDefault();
  var btn = formRecuperar.querySelector('button[type="submit"]');
  btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Verificando...';
  btn.disabled = true;

  mostrarNotificacao(
    "Se este e-mail existir, enviaremos as instruções.",
    "sucesso",
  );
  formRecuperar.reset();

  setTimeout(function () {
    switchView(formLogin);
    btn.innerHTML = 'Enviar Instruções <i class="fa-solid fa-paper-plane"></i>';
    btn.disabled = false;
  }, 2000);
});

// ── LOGOUT ────────────────────────────────────────────────────────────────────
document.addEventListener("click", function (e) {
  var btnLogout =
    e.target.closest("#btnLogout") || e.target.closest("#btnLogoutDropdown");
  if (btnLogout) {
    e.preventDefault();
    if (confirm("Deseja sair do sistema?")) {
      localStorage.removeItem("usuarioHealthGest");
      var dropdown = document.getElementById("perfilDropdown");
      if (dropdown) dropdown.classList.remove("active");
      verificarAutenticacao();
    }
  }
});

// ── DROPDOWN DE PERFIL ────────────────────────────────────────────────────────
document.addEventListener("click", function (e) {
  var perfilBtn = e.target.closest("#btnPerfil");
  var dropdown = document.getElementById("perfilDropdown");
  if (!dropdown) return;

  if (perfilBtn) {
    dropdown.classList.toggle("active");
    // Toggle chevron
    var chevron = document.querySelector(".chevron-icon");
    if (chevron)
      chevron.style.transform = dropdown.classList.contains("active")
        ? "rotate(180deg)"
        : "";
  } else if (!e.target.closest("#perfilDropdown")) {
    dropdown.classList.remove("active");
    var chevron = document.querySelector(".chevron-icon");
    if (chevron) chevron.style.transform = "";
  }
});

// ── EDITAR PERFIL ─────────────────────────────────────────────────────────────
document.addEventListener("click", function (e) {
  if (e.target.closest("#btnEditarPerfil")) {
    e.preventDefault();
    var usuario = JSON.parse(
      localStorage.getItem("usuarioHealthGest") || "null",
    );
    if (!usuario) return;
    document.getElementById("perfilDropdown")?.classList.remove("active");
    abrirModalEditarPerfil(usuario);
  }
});

function abrirModalEditarPerfil(usuario) {
  var modal = document.getElementById("modalEditarPerfil");
  if (!modal) return;
  document.getElementById("editPerfilNome").value = usuario.nome || "";
  document.getElementById("editPerfilEmail").value = usuario.email || "";
  document.getElementById("editPerfilCpf").value = usuario.cpf || "";
  document.getElementById("editPerfilSenha").value = "";
  document.getElementById("editPerfilConfirmaSenha").value = "";
  modal.classList.add("active");
}

document
  .getElementById("btnFecharModalPerfil")
  ?.addEventListener("click", function () {
    document.getElementById("modalEditarPerfil")?.classList.remove("active");
  });
document
  .getElementById("btnCancelarPerfil")
  ?.addEventListener("click", function () {
    document.getElementById("modalEditarPerfil")?.classList.remove("active");
  });

document
  .getElementById("formEditarPerfil")
  ?.addEventListener("submit", async function (e) {
    e.preventDefault();
    var btn = document.getElementById("btnSalvarPerfil");
    if (btn) {
      btn.disabled = true;
      btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...';
    }
    var usuario = JSON.parse(
      localStorage.getItem("usuarioHealthGest") || "null",
    );
    if (!usuario) return;

    var novaSenha = document.getElementById("editPerfilSenha").value;
    var confirmaSenha = document.getElementById(
      "editPerfilConfirmaSenha",
    ).value;
    if (novaSenha && novaSenha !== confirmaSenha) {
      mostrarNotificacao("As senhas não coincidem.", "erro");
      return;
    }

    var dadosAtualizados = {
      id: usuario.id,
      nome: document.getElementById("editPerfilNome").value,
      email: document.getElementById("editPerfilEmail").value,
      cpf: document.getElementById("editPerfilCpf").value,
      papel: usuario.papel,
      especialidade: usuario.especialidade || "",
      senha: novaSenha || "",
    };

    try {
      var response = await atualizarUsuarioNoJava(usuario.id, dadosAtualizados);
      if (response.ok) {
        var atualizado = Object.assign({}, dadosAtualizados, {
          senha: undefined,
        });
        localStorage.setItem("usuarioHealthGest", JSON.stringify(atualizado));
        preencherDadosUsuario(atualizado);
        mostrarNotificacao("Perfil atualizado com sucesso!", "sucesso");
        document
          .getElementById("modalEditarPerfil")
          ?.classList.remove("active");
      } else {
        mostrarNotificacao("Erro ao atualizar perfil.", "erro");
      }
    } catch (err) {
      mostrarNotificacao("Erro de conexão.", "erro");
    }
  });

// ── TOGGLE DE SENHA ───────────────────────────────────────────────────────────
window.toggleSenha = function (inputId, btn) {
  var input = document.getElementById(inputId);
  var icon = btn.querySelector("i");
  if (!input) return;
  if (input.type === "password") {
    input.type = "text";
    icon.classList.replace("fa-eye", "fa-eye-slash");
  } else {
    input.type = "password";
    icon.classList.replace("fa-eye-slash", "fa-eye");
  }
};
