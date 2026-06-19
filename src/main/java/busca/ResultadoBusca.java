package busca;

import java.util.List;

/**
 * Resultado de uma busca por casamento de padroes.
 *
 * Carrega a lista das posicoes (indices, base 0) onde o padrao foi encontrado
 * no texto e o numero de comparacoes de caracteres realizadas pelo algoritmo.
 * O contador de comparacoes serve para evidenciar, de forma didatica, a
 * diferenca de eficiencia entre o KMP e o Boyer-Moore sobre a mesma entrada.
 */
public class ResultadoBusca {
    public final List<Integer> ocorrencias;
    public final long comparacoes;

    public ResultadoBusca(List<Integer> ocorrencias, long comparacoes) {
        this.ocorrencias = ocorrencias;
        this.comparacoes = comparacoes;
    }

    public boolean encontrou() {
        return ocorrencias != null && !ocorrencias.isEmpty();
    }
}
