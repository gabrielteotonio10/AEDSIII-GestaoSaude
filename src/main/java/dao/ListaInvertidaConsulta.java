package dao;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ListaInvertidaConsulta {
    private static final int TAM_NO = 1 + 4 + 8;

    private final RandomAccessFile arquivo;

    public ListaInvertidaConsulta(String nomeArquivo) throws Exception {
        File pasta = new File("./data/indices");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }
        this.arquivo = new RandomAccessFile("./data/indices/" + nomeArquivo + ".lst", "rw");
    }

    public long inserirNoInicio(int idConsulta, long proximo) throws Exception {
        long endereco = arquivo.length();
        arquivo.seek(endereco);
        arquivo.writeByte(' ');
        arquivo.writeInt(idConsulta);
        arquivo.writeLong(proximo);
        return endereco;
    }

    public List<Integer> listarIds(long enderecoInicial) throws Exception {
        List<Integer> ids = new ArrayList<>();
        long atual = enderecoInicial;
        while (atual != -1) {
            arquivo.seek(atual);
            byte lapide = arquivo.readByte();
            int idConsulta = arquivo.readInt();
            long proximo = arquivo.readLong();
            if (lapide == ' ') {
                ids.add(idConsulta);
            }
            atual = proximo;
        }
        return ids;
    }

    public long remover(long enderecoInicial, int idConsulta) throws Exception {
        long anterior = -1;
        long atual = enderecoInicial;

        while (atual != -1) {
            arquivo.seek(atual);
            byte lapide = arquivo.readByte();
            int idAtual = arquivo.readInt();
            long proximo = arquivo.readLong();

            if (lapide == ' ' && idAtual == idConsulta) {
                arquivo.seek(atual);
                arquivo.writeByte('*');
                if (anterior == -1) {
                    return proximo;
                }
                arquivo.seek(anterior + 1 + 4);
                arquivo.writeLong(proximo);
                return enderecoInicial;
            }

            anterior = atual;
            atual = proximo;
        }

        return enderecoInicial;
    }

    public void close() throws Exception {
        arquivo.close();
    }
}
