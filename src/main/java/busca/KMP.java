package busca;

import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmo de casamento de padroes KMP (Knuth-Morris-Pratt).
 
 */
public class KMP {

    /**
     * Constroi a tabela de falha (funcao de prefixo) do padrao.
     * falha[i] = tamanho do maior prefixo proprio de padrao[0..i] que tambem
     * e sufixo de padrao[0..i].
     */
    public static int[] tabelaDeFalha(String padrao) {
        int m = padrao.length();
        int[] falha = new int[m];
        falha[0] = 0;
        int k = 0; // tamanho do prefixo que casa ate o momento
        for (int i = 1; i < m; i++) {
            // recua k enquanto o caractere atual nao estende o prefixo
            while (k > 0 && padrao.charAt(i) != padrao.charAt(k)) {
                k = falha[k - 1];
            }
            if (padrao.charAt(i) == padrao.charAt(k)) {
                k++;
            }
            falha[i] = k;
        }
        return falha;
    }

    /**
     * Procura TODAS as ocorrencias de {@code padrao} dentro de {@code texto}.
     * Retorna as posicoes iniciais (base 0) e o numero de comparacoes feitas.
     */
    public static ResultadoBusca buscar(String texto, String padrao) {
        List<Integer> ocorrencias = new ArrayList<>();
        long comparacoes = 0;

        if (texto == null || padrao == null || padrao.isEmpty() || texto.length() < padrao.length()) {
            return new ResultadoBusca(ocorrencias, comparacoes);
        }

        int n = texto.length();
        int m = padrao.length();
        int[] falha = tabelaDeFalha(padrao);

        int q = 0; // quantos caracteres do padrao ja casaram
        for (int i = 0; i < n; i++) {
            // se o caractere atual nao casa, desliza o padrao pela tabela de falha
            while (q > 0 && texto.charAt(i) != padrao.charAt(q)) {
                comparacoes++;
                q = falha[q - 1];
            }
            comparacoes++;
            if (texto.charAt(i) == padrao.charAt(q)) {
                q++;
            }
            if (q == m) {
                // padrao completo encontrado terminando em i
                ocorrencias.add(i - m + 1);
                q = falha[q - 1]; // continua procurando proximas ocorrencias
            }
        }

        return new ResultadoBusca(ocorrencias, comparacoes);
    }

    /** Conveniencia: informa apenas se o padrao ocorre no texto. */
    public static boolean contem(String texto, String padrao) {
        return buscar(texto, padrao).encontrou();
    }
}
