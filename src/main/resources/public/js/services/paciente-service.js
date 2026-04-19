// Captura do paciente do formulário e envio para o Java
document.getElementById("formPaciente").addEventListener("submit", (e) => {
  e.preventDefault();
  // Cria um obj
  const paciente = {
    nome: document.getElementById("nome").value,
    cpf: document.getElementById("cpf").value,
    alergias: listaAlergiasTemporaria,
  };
  // Envia ao Java usando Fetch API
  fetch("/pacientes", {
    method: "POST", 
    headers: {
      "Content-Type": "application/json", // JSON
    },
    body: JSON.stringify(paciente), // Transforma o objeto JS em texto (JSON)
  })
    .then((resposta) => {
     if (resposta.ok) {
       console.log("Paciente cadastrado com sucesso!");
       // Verificamos se a função existe antes de chamar para não quebrar o código
       if (typeof fecharModalCadastro === "function") {
         fecharModalCadastro();
       } else {
         console.warn("Atenção: A função fecharModal não foi encontrada.");
       }
     } else {
       alert("O servidor respondeu com erro.");
     }
    })
    .catch((erro) => {
      console.error("Erro na conexão:", erro);
      alert("O servidor Java está desligado? Verifique o terminal.");
    });
});
