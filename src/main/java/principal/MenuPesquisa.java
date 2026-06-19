package principal;

import java.util.List;
import java.util.Scanner;

import busca.BoyerMoore;
import busca.KMP;
import busca.ResultadoBusca;
import dao.PacienteDAO;
import model.Paciente;

/**
 * Interface de pesquisa por casamento de padroes (Fase V).
 *
 * Fluxo exigido no trabalho:
 *  1. menu com a opcao "Pesquisar por padrao (KMP / BM)";
 *  2. o usuario escolhe o algoritmo (KMP ou Boyer-Moore);
 *  3. o usuario informa o padrao (string);
 *  4. o sistema retorna os registros encontrados.
 *
 * A busca e aplicada sobre o campo textual NOME dos pacientes. Alem dos
 * registros, a tela exibe as posicoes em que o padrao foi encontrado e o
 * total de comparacoes de caracteres, evidenciando o funcionamento de cada
 * algoritmo.
 */
public class MenuPesquisa {
    private final PacienteDAO pacienteDAO;
    private final Scanner console;

    public MenuPesquisa(Scanner console) throws Exception {
        this.pacienteDAO = new PacienteDAO();
        this.console = console;
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\n🔎 Pesquisar por padrão (KMP / BM)");
            System.out.println("----------------------------------");
            System.out.println("> Início > Pesquisar por padrão");
            System.out.println("\nEscolha o algoritmo de casamento de padrões:");
            System.out.println("1 - KMP (Knuth-Morris-Pratt)");
            System.out.println("2 - Boyer-Moore (heurística do mau caractere)");
            System.out.println("0 - Voltar");
            System.out.print("\nOpção: ");

            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1:
                    pesquisar("KMP");
                    break;
                case 2:
                    pesquisar("BM");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 0);
    }

    private void pesquisar(String algoritmo) {
        String nomeAlgoritmo = "BM".equals(algoritmo) ? "Boyer-Moore" : "KMP";

        System.out.print("\nDigite o padrão a procurar no nome do paciente: ");
        String padrao = console.nextLine();
        if (padrao == null || padrao.trim().isEmpty()) {
            System.out.println("Padrão vazio. Operação cancelada.");
            return;
        }

        try {
            List<Paciente> todos = pacienteDAO.listarTodos();
            String alvo = padrao.toLowerCase();
            int totalEncontrados = 0;
            long totalComparacoes = 0;

            System.out.println("\n=== Resultado da busca com " + nomeAlgoritmo + " ===");
            System.out.println("Padrão procurado : \"" + padrao + "\"");
            System.out.println("Registros na base: " + todos.size());
            System.out.println("--------------------------------------------------");

            for (Paciente p : todos) {
                String nome = p.getNome() == null ? "" : p.getNome();
                ResultadoBusca r = "BM".equals(algoritmo)
                        ? BoyerMoore.buscar(nome.toLowerCase(), alvo)
                        : KMP.buscar(nome.toLowerCase(), alvo);
                totalComparacoes += r.comparacoes;
                if (r.encontrou()) {
                    totalEncontrados++;
                    System.out.println("• ID " + p.getId() + " - " + p.getNome()
                            + "  (posições: " + r.ocorrencias + ")");
                    System.out.println("    CPF: " + p.getCpf() + "  | Alergias: "
                            + (p.getAlergias().isEmpty() ? "Nenhuma" : String.join(", ", p.getAlergias())));
                }
            }

            System.out.println("--------------------------------------------------");
            if (totalEncontrados == 0) {
                System.out.println("Nenhum registro encontrado para o padrão \"" + padrao + "\".");
            } else {
                System.out.println("Total de registros encontrados: " + totalEncontrados);
            }
            System.out.println("Total de comparações de caracteres (" + nomeAlgoritmo + "): " + totalComparacoes);
        } catch (Exception e) {
            System.out.println("Erro na pesquisa: " + e.getMessage());
        }
    }
}
