package busca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Algoritmo de casamento de padroes Boyer-Moore (heuristica do mau caractere).
 */
public class BoyerMoore {

    /**
     * Tabela de ultima ocorrencia: para cada caractere presente no padrao,
     * guarda o MAIOR indice em que ele aparece. Caracteres ausentes valem -1
     */
    public static Map<Character, Integer> tabelaUltimaOcorrencia(String padrao) {
        Map<Character, Integer> ultima = new HashMap<>();
        for (int i = 0; i < padrao.length(); i++) {
            ultima.put(padrao.charAt(i), i); // sobrescreve: fica a ultima posicao
        }
        return ultima;
    }

    /**
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
        Map<Character, Integer> ultima = tabelaUltimaOcorrencia(padrao);

        int s = 0; // deslocamento do padrao em relacao ao inicio do texto
        while (s <= n - m) {
            int j = m - 1; // compara do fim do padrao para o inicio
            while (j >= 0) {
                comparacoes++;
                if (padrao.charAt(j) == texto.charAt(s + j)) {
                    j--;
                } else {
                    break;
                }
            }

            if (j < 0) {
                // todos os caracteres casaram: ocorrencia em s
                ocorrencias.add(s);
                s++; // avanca 1 para permitir ocorrencias sobrepostas
            } else {
                // mau caractere encontrado no texto na posicao s + j
                char mauCaractere = texto.charAt(s + j);
                int ult = ultima.getOrDefault(mauCaractere, -1);
                int deslocamento = j - ult; // alinha o mau caractere com sua ultima ocorrencia
                if (deslocamento < 1) {
                    deslocamento = 1; // garante progresso
                }
                s += deslocamento;
            }
        }

        return new ResultadoBusca(ocorrencias, comparacoes);
    }

    /** Conveniencia: informa apenas se o padrao ocorre no texto. */
    public static boolean contem(String texto, String padrao) {
        return buscar(texto, padrao).encontrou();
    }
}
