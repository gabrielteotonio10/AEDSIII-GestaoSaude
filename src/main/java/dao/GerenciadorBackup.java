package dao;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GerenciadorBackup {

    private static final String DIR_DATA = "./data";
    private static final String MAGIC_HUFFMAN = "HUFF";
    private static final String MAGIC_LZW = "LZW_";

    public static ResultadoCompressao gerarBackupHuffman() throws Exception {
        return gerarBackup(new HuffmanCompressor(), MAGIC_HUFFMAN, "huffman");
    }

    public static ResultadoCompressao gerarBackupLZW() throws Exception {
        return gerarBackup(new LZWCompressor(), MAGIC_LZW, "lzw");
    }

    public static void restaurar(String caminhoBackup) throws Exception {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(caminhoBackup)))) {

            byte[] magicBytes = new byte[4];
            dis.readFully(magicBytes);
            String magic = new String(magicBytes, "ASCII");

            Object compressor;
            if (magic.equals(MAGIC_HUFFMAN)) {
                compressor = new HuffmanCompressor();
            } else if (magic.equals(MAGIC_LZW)) {
                compressor = new LZWCompressor();
            } else {
                throw new IOException("Arquivo de backup inválido (magic desconhecido: " + magic + ").");
            }

            int numArquivos = dis.readInt();

            for (int i = 0; i < numArquivos; i++) {
                int nomeLen = dis.readShort() & 0xFFFF;
                byte[] nomeBytes = new byte[nomeLen];
                dis.readFully(nomeBytes);
                String nomeRelativo = new String(nomeBytes, "UTF-8");

                long tamanhoOriginal = dis.readLong();
                long tamanhoComprimido = dis.readLong();
                byte[] comprimido = new byte[(int) tamanhoComprimido];
                dis.readFully(comprimido);

                byte[] dados;
                if (compressor instanceof HuffmanCompressor) {
                    dados = ((HuffmanCompressor) compressor).descomprimir(comprimido);
                } else {
                    dados = ((LZWCompressor) compressor).descomprimir(comprimido);
                }

                if (dados.length != tamanhoOriginal) {
                    throw new IOException("Erro ao restaurar '" + nomeRelativo +
                            "': tamanho esperado " + tamanhoOriginal + ", obtido " + dados.length);
                }

                File destino = new File(DIR_DATA + File.separator + nomeRelativo.replace("/", File.separator));
                destino.getParentFile().mkdirs();
                Files.write(destino.toPath(), dados);
            }
        }
    }

    public static List<String> listarBackups() {
        List<String> backups = new ArrayList<>();
        File dir = new File(".");
        File[] arquivos = dir.listFiles((f, name) -> name.startsWith("backup_") && name.endsWith(".bak"));
        if (arquivos != null) {
            Arrays.sort(arquivos, Comparator.comparing(File::getName).reversed());
            for (File f : arquivos) backups.add(f.getName());
        }
        return backups;
    }

    private static ResultadoCompressao gerarBackup(Object compressor, String magic, String sufixo) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String caminho = "./backup_" + sufixo + "_" + timestamp + ".bak";

        File dirData = new File(DIR_DATA);
        if (!dirData.exists()) {
            throw new IOException("Diretório de dados não encontrado: " + DIR_DATA);
        }

        List<File> arquivos = listarArquivosRecursivo(dirData);
        if (arquivos.isEmpty()) {
            throw new IOException("Nenhum arquivo de dados encontrado em " + DIR_DATA);
        }

        long totalOriginal = 0;
        long totalComprimido = 0;

        ByteArrayOutputStream conteudoArquivos = new ByteArrayOutputStream();
        DataOutputStream dosArquivos = new DataOutputStream(conteudoArquivos);

        for (File arquivo : arquivos) {
            String nomeRelativo = dirData.toURI().relativize(arquivo.toURI()).getPath();
            byte[] dados = Files.readAllBytes(arquivo.toPath());
            byte[] comprimido;

            if (compressor instanceof HuffmanCompressor) {
                comprimido = ((HuffmanCompressor) compressor).comprimir(dados);
            } else {
                comprimido = ((LZWCompressor) compressor).comprimir(dados);
            }

            totalOriginal += dados.length;
            totalComprimido += comprimido.length;

            byte[] nomeBytes = nomeRelativo.getBytes("UTF-8");
            dosArquivos.writeShort(nomeBytes.length);
            dosArquivos.write(nomeBytes);
            dosArquivos.writeLong(dados.length);
            dosArquivos.writeLong(comprimido.length);
            dosArquivos.write(comprimido);
        }
        dosArquivos.flush();

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(caminho)))) {
            dos.writeBytes(magic);
            dos.writeInt(arquivos.size());
            dos.write(conteudoArquivos.toByteArray());
        }

        long tamanhoArquivoFinal = new File(caminho).length();
        return new ResultadoCompressao(caminho, arquivos.size(), totalOriginal, tamanhoArquivoFinal);
    }

    private static List<File> listarArquivosRecursivo(File diretorio) {
        List<File> arquivos = new ArrayList<>();
        File[] conteudo = diretorio.listFiles();
        if (conteudo != null) {
            Arrays.sort(conteudo, Comparator.comparing(File::getName));
            for (File f : conteudo) {
                if (f.isDirectory()) {
                    arquivos.addAll(listarArquivosRecursivo(f));
                } else {
                    arquivos.add(f);
                }
            }
        }
        return arquivos;
    }

    public static class ResultadoCompressao {
        public final String caminhoBackup;
        public final int quantidadeArquivos;
        public final long tamanhoOriginalTotal;
        public final long tamanhoComprimidoTotal;
        public final double taxaCompressao;

        public ResultadoCompressao(String caminhoBackup, int quantidadeArquivos,
                long tamanhoOriginalTotal, long tamanhoComprimidoTotal) {
            this.caminhoBackup = caminhoBackup;
            this.quantidadeArquivos = quantidadeArquivos;
            this.tamanhoOriginalTotal = tamanhoOriginalTotal;
            this.tamanhoComprimidoTotal = tamanhoComprimidoTotal;
            this.taxaCompressao = tamanhoOriginalTotal > 0
                    ? (1.0 - (double) tamanhoComprimidoTotal / tamanhoOriginalTotal) * 100.0
                    : 0.0;
        }
    }
}
