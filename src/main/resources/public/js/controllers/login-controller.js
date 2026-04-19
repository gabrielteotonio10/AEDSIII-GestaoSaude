const URL_API = "http://localhost:8080"; 
// Elementos da UI
const formLogin = document.getElementById("formLogin");
const formCadastro = document.getElementById("formCadastro");
const formRecuperar = document.getElementById("formRecuperar");

// Alternar entre Telas
document
  .getElementById("btnIrCadastro")
  .addEventListener("click", () => switchView(formCadastro));
document
  .getElementById("btnEsqueciSenha")
  .addEventListener("click", () => switchView(formRecuperar));
document
  .getElementById("btnVoltarLogin")
  .addEventListener("click", () => switchView(formLogin));

document.getElementById("btnCancelarCadastro").addEventListener("click", () => {
  // Alerta de confirmação se o usuário quiser cancelar o cadastro no meio
  const nome = document.getElementById("cadNome").value;
  if (nome !== "") {
    if (
      confirm(
        "Tem certeza que deseja cancelar? Os dados digitados serão perdidos.",
      )
    ) {
      formCadastro.reset();
      switchView(formLogin);
    }
  } else {
    switchView(formLogin);
  }
});

function switchView(viewToShow) {
  document
    .querySelectorAll(".auth-form")
    .forEach((form) => form.classList.remove("active"));
  viewToShow.classList.add("active");
}

// Mostrar campo de especialidade apenas se for Médico
document.getElementById("cadPapel").addEventListener("change", (e) => {
  const divEsp = document.getElementById("divEspecialidade");
  if (e.target.value === "Médico") {
    divEsp.style.display = "block";
    document.getElementById("cadEspecialidade").required = true;
  } else {
    divEsp.style.display = "none";
    document.getElementById("cadEspecialidade").required = false;
    document.getElementById("cadEspecialidade").value = "";
  }
});

// ==========================================
// LÓGICA DE LOGIN (Conectando com o Java)
// ==========================================
formLogin.addEventListener("submit", async (e) => {
  e.preventDefault();
  const btn = formLogin.querySelector('button[type="submit"]');
  btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Autenticando...';

  const credenciais = {
    email: document.getElementById("loginEmail").value,
    senha: document.getElementById("loginSenha").value,
  };

  try {
    const response = await fetch(`${URL_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credenciais),
    });

    if (response.ok) {
      const usuarioLogado = await response.json();
      localStorage.setItem("usuarioHealthGest", JSON.stringify(usuarioLogado));
      mostrarNotificacao("Login aprovado! Redirecionando...", "sucesso");

      setTimeout(() => {
        window.location.reload();;
      }, 1500);
    } else {
      mostrarNotificacao("E-mail ou senha incorretos.", "erro");
      btn.innerHTML = 'Entrar <i class="fa-solid fa-arrow-right"></i>';
    }
  } catch (error) {
    mostrarNotificacao("Erro ao conectar com o servidor.", "erro");
    btn.innerHTML = 'Entrar <i class="fa-solid fa-arrow-right"></i>';
  }
});

// ==========================================
// LÓGICA DE CADASTRO
// ==========================================
formCadastro.addEventListener("submit", async (e) => {
  e.preventDefault();

  const novoUsuario = {
    nome: document.getElementById("cadNome").value,
    cpf: document.getElementById("cadCpf").value,
    email: document.getElementById("cadEmail").value,
    senha: document.getElementById("cadSenha").value,
    papel: document.getElementById("cadPapel").value,
    especialidade: document.getElementById("cadEspecialidade").value || "",
  };

  try {
    const response = await fetch(`${URL_API}/usuarios`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(novoUsuario),
    });

    if (response.ok) {
      mostrarNotificacao(
        "Cadastro realizado com sucesso! Faça seu login.",
        "sucesso",
      );
      formCadastro.reset();
      switchView(formLogin);
    } else {
      mostrarNotificacao("Erro ao criar usuário.", "erro");
    }
  } catch (error) {
    mostrarNotificacao("Erro de conexão.", "erro");
  }
});

// ==========================================
// LÓGICA DE RECUPERAR SENHA
// ==========================================
formRecuperar.addEventListener("submit", (e) => {
  e.preventDefault();
  mostrarNotificacao(
    "Instruções de recuperação enviadas para o seu e-mail!",
    "sucesso",
  );
  formRecuperar.reset();
  setTimeout(() => {
    switchView(formLogin);
  }, 2000);
});

// Função Auxiliar de Toast Notification
function mostrarNotificacao(mensagem, tipo) {
  const container = document.getElementById("toastContainer");
  const toast = document.createElement("div");
  toast.className = `toast ${tipo}`;
  const icone =
    tipo === "sucesso" ? "fa-circle-check" : "fa-circle-exclamation";
  toast.innerHTML = `<i class="fa-solid ${icone}"></i> <span>${mensagem}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.remove();
  }, 3500);
}
