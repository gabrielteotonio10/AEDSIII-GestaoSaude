package principal;

import dao.HuffmanCompressor;
import dao.LZWCompressor;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Utilitário para medir as taxas de compressão de Huffman e LZW
 * em dados binários representativos do sistema.
 */
public class TesteCompressao {

    public static void main(String[] args) throws Exception {
        // Simula conteúdo binário semelhante ao gerado pelos arquivos .db do sistema
        byte[] dadosSinteticos = gerarDadosSinteticos();

        System.out.println("=== TESTE DE COMPRESSÃO ===");
        System.out.println("Tamanho dos dados de teste: " + dadosSinteticos.length + " bytes");
        System.out.println();

        testarHuffman(dadosSinteticos);
        System.out.println();
        testarLZW(dadosSinteticos);
    }

    private static void testarHuffman(byte[] dados) throws Exception {
        HuffmanCompressor huff = new HuffmanCompressor();
        byte[] comprimido = huff.comprimir(dados);
        byte[] restaurado = huff.descomprimir(comprimido);

        boolean integro = restaurado.length == dados.length;
        for (int i = 0; i < dados.length && integro; i++) {
            if (dados[i] != restaurado[i]) integro = false;
        }

        double taxa = (1.0 - (double) comprimido.length / dados.length) * 100.0;

        System.out.println("--- HUFFMAN ---");
        System.out.println("Tamanho original : " + dados.length + " bytes");
        System.out.println("Tamanho comprimido: " + comprimido.length + " bytes");
        System.out.printf("Taxa de compressão: %.2f%%\n", taxa);
        System.out.println("Integridade OK   : " + integro);
    }

    private static void testarLZW(byte[] dados) throws Exception {
        LZWCompressor lzw = new LZWCompressor();
        byte[] comprimido = lzw.comprimir(dados);
        byte[] restaurado = lzw.descomprimir(comprimido);

        boolean integro = restaurado.length == dados.length;
        for (int i = 0; i < dados.length && integro; i++) {
            if (dados[i] != restaurado[i]) integro = false;
        }

        double taxa = (1.0 - (double) comprimido.length / dados.length) * 100.0;

        System.out.println("--- LZW ---");
        System.out.println("Tamanho original : " + dados.length + " bytes");
        System.out.println("Tamanho comprimido: " + comprimido.length + " bytes");
        System.out.printf("Taxa de compressão: %.2f%%\n", taxa);
        System.out.println("Integridade OK   : " + integro);
    }

    /**
     * Gera dados binários sintéticos com a mesma estrutura produzida pelos
     * arquivos .db do sistema (cabeçalho + registros com lápide, tamanho e payload
     * serializado via DataOutputStream.writeUTF).
     */
    private static byte[] gerarDadosSinteticos() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Cabeçalho do arquivo (TAM_CABECALHO = 12 bytes)
        dos.writeInt(20);   // último ID gerado
        dos.writeLong(-1);  // cabeça da lista de espaços livres

        String[] nomes = {
            "Ana Silva", "Bruno Costa", "Carla Mendes", "Diego Rocha",
            "Elena Souza", "Fabio Lima", "Gabriela Nunes", "Henrique Alves",
            "Isabela Martins", "João Ferreira", "Karla Oliveira", "Lucas Pereira",
            "Maria Santos", "Nicolas Cardoso", "Olivia Castro", "Paulo Dias",
            "Quenia Ramos", "Rafael Gomes", "Samanta Torres", "Thiago Barbosa"
        };
        String[] cpfs = {
            "111.111.111-11", "222.222.222-22", "333.333.333-33", "444.444.444-44",
            "555.555.555-55", "666.666.666-66", "777.777.777-77", "888.888.888-88",
            "999.999.999-99", "000.000.000-00", "123.456.789-01", "234.567.890-12",
            "345.678.901-23", "456.789.012-34", "567.890.123-45", "678.901.234-56",
            "789.012.345-67", "890.123.456-78", "901.234.567-89", "012.345.678-90"
        };

        for (int i = 0; i < 20; i++) {
            ByteArrayOutputStream recBaos = new ByteArrayOutputStream();
            DataOutputStream recDos = new DataOutputStream(recBaos);
            recDos.writeInt(i + 1);
            recDos.writeUTF(nomes[i]);
            recDos.writeUTF(cpfs[i]);
            recDos.writeInt(0); // sem alergias
            recDos.flush();

            byte[] registro = recBaos.toByteArray();
            dos.writeByte(' ');
            dos.writeShort(registro.length);
            dos.write(registro);
        }

        // Simula arquivo de índice Hash Extensível (diretório + buckets)
        dos.writeInt(1); // profundidade global
        for (int b = 0; b < 2; b++) {
            long enderecoBucket = 4L + 16L + b * (1 + 2 + 4 * 12);
            dos.writeLong(enderecoBucket);
        }
        for (int b = 0; b < 2; b++) {
            dos.writeByte(1); // profundidade local
            dos.writeShort(Math.min(10, 4)); // quantidade
            for (int k = 0; k < 4; k++) {
                dos.writeInt(b * 4 + k + 1);
                dos.writeLong(12L + (b * 4 + k) * 50L);
            }
        }

        dos.flush();

        // Duplicar o bloco para simular múltiplos arquivos do sistema
        byte[] bloco = baos.toByteArray();
        ByteArrayOutputStream total = new ByteArrayOutputStream();
        for (int rep = 0; rep < 5; rep++) {
            total.write(bloco);
        }
        return total.toByteArray();
    }
}
