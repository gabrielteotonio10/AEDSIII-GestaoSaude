package principal;

import model.Paciente;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import dao.PacienteDAO;

public class MenuPacientes {
    private PacienteDAO pacienteDAO;
    private Scanner console = new Scanner(System.in);

    public MenuPacientes() throws Exception {
        pacienteDAO = new PacienteDAO();
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\n🏥 Gestão de Saúde");
            System.out.println("------------------");
            System.out.println("> Início > Pacientes");
            System.out.println("\n1 - Buscar Paciente");
            System.out.println("2 - Incluir Paciente");
            System.out.println("3 - Alterar Paciente");
            System.out.println("4 - Excluir Paciente");
            System.out.println("0 - Voltar");
            System.out.print("\nOpção: ");

            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            while (opcao <= 0 && opcao >= 4) {
                System.out.println("\nErro, opção inválida! Tente novamente: ");
                try {
                    opcao = Integer.valueOf(console.nextLine());
                } catch (NumberFormatException e) {
                    opcao = -1;
                }
            }

            switch (opcao) {
                case 1:
                    buscarPaciente();
                    break;
                case 2:
                    incluirPaciente();
                    break;
                case 3:
                    alterarPaciente();
                    break;
                case 4:
                    excluirPaciente();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void buscarPaciente() {
        System.out.print("\nID do paciente: ");
        int id = console.nextInt();
        console.nextLine(); 

        try {
            Paciente paciente = pacienteDAO.buscar(id);
            if (paciente != null) {
                System.out.println(paciente); 
            } else {
                System.out.println("Paciente não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar paciente: " + e.getMessage());
        }
    }

    private void incluirPaciente() {
        System.out.println("\n--- Inclusão de Paciente ---");
        System.out.print("Nome: ");
        String nome = console.nextLine();

        System.out.print("CPF (11 dígitos): ");
        String cpf = console.nextLine();

        // --- LÓGICA DO CAMPO MULTIVALORADO (ALERGIAS) ---
        List<String> alergias = new ArrayList<>();
        System.out.println("Digite as alergias do paciente (ou digite 'fim' para encerrar):");
        while (true) {
            System.out.print("> ");
            String alergia = console.nextLine();
            if (alergia.equalsIgnoreCase("fim")) {
                break; // Sai do loop se digitar fim
            }
            if (!alergia.trim().isEmpty()) {
                alergias.add(alergia); // Adiciona na lista
            }
        }

        try {
            Paciente paciente = new Paciente(nome, cpf, alergias);
            int idGerado = pacienteDAO.incluir(paciente);
            System.out.println("Paciente incluído com sucesso! ID Gerado: " + idGerado);
        } catch (Exception e) {
            System.out.println("Erro ao incluir paciente: " + e.getMessage());
        }
    }

    private void alterarPaciente() {
        System.out.print("\nID do paciente a ser alterado: ");
        int id = console.nextInt();
        console.nextLine(); 

        try {
            Paciente paciente = pacienteDAO.buscar(id);
            if (paciente == null) {
                System.out.println("Paciente não encontrado.");
                return;
            }

            // Exibe os dados atuais
            System.out.println("Dados atuais: " + paciente);

            System.out.print("\nNovo nome (vazio para manter o atual): ");
            String nome = console.nextLine();
            if (!nome.isEmpty())
                paciente.setNome(nome);

            System.out.print("Novo CPF (vazio para manter o atual): ");
            String cpf = console.nextLine();
            if (!cpf.isEmpty())
                paciente.setCpf(cpf);

            // Pergunta se quer sobrescrever as alergias
            System.out.print("Deseja alterar a lista de alergias? (S/N): ");
            String resp = console.nextLine();

            if (resp.equalsIgnoreCase("S")) {
                List<String> novasAlergias = new ArrayList<>();
                System.out.println("Digite as NOVAS alergias (ou digite 'fim' para encerrar):");
                while (true) {
                    System.out.print("> ");
                    String alergia = console.nextLine();
                    if (alergia.equalsIgnoreCase("fim"))
                        break;
                    if (!alergia.trim().isEmpty())
                        novasAlergias.add(alergia);
                }
                paciente.setAlergias(novasAlergias);
            }

            if (pacienteDAO.alterar(paciente)) {
                System.out.println("Paciente alterado com sucesso.");
            } else {
                System.out.println("Erro ao alterar paciente.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao alterar paciente: " + e.getMessage());
        }
    }

    private void excluirPaciente() {
        System.out.print("\nID do paciente a ser excluído: ");
        int id = console.nextInt();
        console.nextLine();

        try {
            Paciente paciente = pacienteDAO.buscar(id);
            if (paciente == null) {
                System.out.println("Paciente não encontrado.");
                return;
            }

            System.out.println(paciente); // Mostra quem vai ser apagado
            System.out.print("Confirma exclusão lógica? (S/N): ");
            String resp = console.nextLine();

            if (resp.equalsIgnoreCase("S")) {
                if (pacienteDAO.excluir(id)) {
                    System.out.println("Paciente excluído com sucesso (Lápide marcada).");
                } else {
                    System.out.println("Erro ao excluir paciente.");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao excluir paciente: " + e.getMessage());
        }
    }
}