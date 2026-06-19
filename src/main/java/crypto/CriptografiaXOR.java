package crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Criptografia simetrica por XOR com chave repetida (vigenere binario).
 *
 * Funcionamento:
 *  - Cada byte do dado e combinado, via operacao XOR (^), com um byte da chave.
 *  - A chave e curta, entao ela e repetida ciclicamente sobre todo o dado
 *    (indice da chave = posicao % tamanho_da_chave).
 *  - O XOR e simetrico: aplicar a MESMA chave duas vezes devolve o dado
 *    original (a XOR b XOR b = a). Por isso o mesmo metodo cifra e decifra.
 *
 * Como o resultado do XOR pode conter bytes nao imprimiveis, o texto cifrado e
 * codificado em Base64 antes de ser gravado em arquivo. Assim o valor
 * armazenado em disco fica ilegivel (embaralhado) e, ao mesmo tempo, seguro
 * para ser gravado/lido como String.
 *
 * Observacao: XOR com chave fixa e um metodo didatico, adequado ao escopo da
 * disciplina; nao deve ser usado como protecao criptografica real em producao.
 */
public class CriptografiaXOR {

    /** Chave simetrica. Em um sistema real viria de configuracao externa/segredo. */
    private static final byte[] CHAVE = "AEDS3-GestaoSaude".getBytes(StandardCharsets.UTF_8);

    /**
     * Aplica o XOR byte a byte com a chave repetida.
     * Como o XOR e simetrico, este mesmo metodo serve para cifrar e decifrar.
     */
    public static byte[] aplicarXor(byte[] dados) {
        byte[] resultado = new byte[dados.length];
        for (int i = 0; i < dados.length; i++) {
            resultado[i] = (byte) (dados[i] ^ CHAVE[i % CHAVE.length]);
        }
        return resultado;
    }

    /**
     * Cifra um texto puro: aplica XOR sobre os bytes e devolve em Base64.
     * O retorno e o valor "embaralhado" que sera gravado em disco.
     */
    public static String cifrar(String textoPuro) {
        if (textoPuro == null) {
            return null;
        }
        byte[] cifrado = aplicarXor(textoPuro.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cifrado);
    }

    /**
     * Decifra um texto previamente cifrado por {@link #cifrar(String)}:
     * decodifica o Base64 e reaplica o XOR (que e simetrico), recuperando o
     * texto original.
     *
     * Caso o valor recebido nao seja um Base64 valido (por exemplo, dados
     * legados gravados ainda em texto puro), devolve o proprio valor recebido,
     * evitando quebrar a leitura de registros antigos.
     */
    public static String decifrar(String textoCifrado) {
        if (textoCifrado == null) {
            return null;
        }
        try {
            byte[] cifrado = Base64.getDecoder().decode(textoCifrado);
            byte[] puro = aplicarXor(cifrado);
            return new String(puro, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return textoCifrado; // compatibilidade com dados nao cifrados
        }
    }
}
