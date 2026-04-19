// ── layout.js ─────────────────────────────────────────────────────────────────
// Navegação lateral: troca de seção ao clicar nos itens do menu

document.addEventListener("DOMContentLoaded", () => {
  const navItems = document.querySelectorAll(
    ".sidebar-nav .nav-item[data-secao]",
  );

  navItems.forEach((item) => {
    item.addEventListener("click", (e) => {
      e.preventDefault();
      const idSecao = item.dataset.secao;
      if (!idSecao) return;

      // Remove active de todos os itens e seções
      document
        .querySelectorAll(".sidebar .nav-item")
        .forEach((n) => n.classList.remove("active"));
      document.querySelectorAll(".crud-section").forEach((s) => {
        s.classList.remove("active");
        s.style.display = "none";
      });

      // Ativa o item clicado
      item.classList.add("active");

      // Mostra a seção correspondente
      const secao = document.getElementById(idSecao);
      if (secao) {
        secao.classList.add("active");
        secao.style.display = "block";
      }

      // Atualiza o breadcrumb
      const nomeSecao = item.textContent.trim();
      const breadcrumb = document.getElementById("breadcrumbAtual");
      if (breadcrumb) breadcrumb.textContent = nomeSecao;

      // Carrega os dados da seção se necessário
      carregarSecao(idSecao);
    });
  });

  // Garante que só a seção ativa está visível no início
  document.querySelectorAll(".crud-section").forEach((s) => {
    if (!s.classList.contains("active")) {
      s.style.display = "none";
    }
  });
});

function carregarSecao(idSecao) {
  switch (idSecao) {
    case "section-pacientes":
      if (typeof atualizarTabelaPacientes === "function")
        atualizarTabelaPacientes();
      break;
    case "section-consultas":
      if (typeof atualizarTabelaConsultas === "function")
        atualizarTabelaConsultas();
      break;
    case "section-procedimentos":
      if (typeof atualizarTabelaProcedimentos === "function")
        atualizarTabelaProcedimentos();
      break;
    case "section-usuarios":
      if (typeof atualizarTabelaUsuarios === "function")
        atualizarTabelaUsuarios();
      break;
  }
}

// Logout pelo botão do dropdown também
document.addEventListener("click", (e) => {
  if (e.target.closest("#btnLogoutDropdown")) {
    e.preventDefault();
    if (confirm("Deseja sair do sistema?")) {
      localStorage.removeItem("usuarioHealthGest");
      const dropdown = document.getElementById("perfilDropdown");
      if (dropdown) dropdown.classList.remove("active");
      if (typeof verificarAutenticacao === "function") verificarAutenticacao();
    }
  }
});
