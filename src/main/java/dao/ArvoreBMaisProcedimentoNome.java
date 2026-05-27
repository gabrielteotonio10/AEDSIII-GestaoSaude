package dao;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.Procedimento;

public class ArvoreBMaisProcedimentoNome {
    private static final int ORDEM = 8;
    private static final int TAM_CHAVE = 80;
    private static final int TAM_CABECALHO = 8;
    private static final int TAM_ENTRADA = TAM_CHAVE + 4;
    private static final int TAM_FOLHA = 4 + 8 + (ORDEM * TAM_ENTRADA);

    private final RandomAccessFile arquivo;
    private final boolean novo;

    public ArvoreBMaisProcedimentoNome() throws Exception {
        File pasta = new File("./data/indices");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }
        this.arquivo = new RandomAccessFile("./data/indices/procedimentos_nome.bplus", "rw");
        this.novo = arquivo.length() < TAM_CABECALHO;
        if (arquivo.length() < TAM_CABECALHO) {
            arquivo.writeLong(-1);
        }
    }

    public boolean isNovo() {
        return novo;
    }

    public void reconstruir(List<Procedimento> procedimentos) throws Exception {
        List<Entrada> entradas = new ArrayList<>();
        for (Procedimento p : procedimentos) {
            entradas.add(new Entrada(normalizar(p.getNomeExame()), p.getId()));
        }
        entradas.sort(Comparator.comparing((Entrada e) -> e.chave).thenComparingInt(e -> e.id));

        arquivo.setLength(0);
        arquivo.writeLong(entradas.isEmpty() ? -1 : TAM_CABECALHO);

        long enderecoFolha = TAM_CABECALHO;
        for (int i = 0; i < entradas.size(); i += ORDEM) {
            int fim = Math.min(i + ORDEM, entradas.size());
            long proximaFolha = fim < entradas.size() ? enderecoFolha + TAM_FOLHA : -1;
            escreverFolha(enderecoFolha, proximaFolha, entradas.subList(i, fim));
            enderecoFolha += TAM_FOLHA;
        }
    }

    public List<Integer> listarIdsOrdenados() throws Exception {
        List<Integer> ids = new ArrayList<>();
        arquivo.seek(0);
        long folha = arquivo.readLong();

        while (folha != -1) {
            arquivo.seek(folha);
            int quantidade = arquivo.readInt();
            long proxima = arquivo.readLong();
            for (int i = 0; i < ORDEM; i++) {
                lerChave();
                int id = arquivo.readInt();
                if (i < quantidade) {
                    ids.add(id);
                }
            }
            folha = proxima;
        }
        return ids;
    }

    public void close() throws Exception {
        arquivo.close();
    }

    private void escreverFolha(long endereco, long proximaFolha, List<Entrada> entradas) throws Exception {
        arquivo.seek(endereco);
        arquivo.writeInt(entradas.size());
        arquivo.writeLong(proximaFolha);

        for (int i = 0; i < ORDEM; i++) {
            if (i < entradas.size()) {
                escreverChave(entradas.get(i).chave);
                arquivo.writeInt(entradas.get(i).id);
            } else {
                escreverChave("");
                arquivo.writeInt(0);
            }
        }
    }

    private void escreverChave(String chave) throws Exception {
        byte[] origem = chave.getBytes(StandardCharsets.UTF_8);
        byte[] destino = new byte[TAM_CHAVE];
        System.arraycopy(origem, 0, destino, 0, Math.min(origem.length, TAM_CHAVE));
        arquivo.write(destino);
    }

    private String lerChave() throws Exception {
        byte[] bytes = new byte[TAM_CHAVE];
        arquivo.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    private String normalizar(String valor) {
        String texto = valor == null ? "" : valor.toLowerCase().trim();
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static class Entrada {
        String chave;
        int id;

        Entrada(String chave, int id) {
            this.chave = chave;
            this.id = id;
        }
    }
}
