package dao;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import model.Registro;

public class Arquivo<T extends Registro> {
    private static final int TAM_CABECALHO = 12;

    private final RandomAccessFile arquivo;
    private final String nomeArquivo;
    private final Constructor<T> construtor;
    private final HashExtensivelIntLong indicePrimario;

    public Arquivo(String nomeArquivo, Constructor<T> construtor) throws Exception {
        File diretorio = new File("./data");
        if (!diretorio.exists()) {
            diretorio.mkdir();
        }

        diretorio = new File("./data/" + nomeArquivo);
        if (!diretorio.exists()) {
            diretorio.mkdir();
        }

        this.nomeArquivo = "./data/" + nomeArquivo + "/" + nomeArquivo + ".db";
        this.construtor = construtor;
        this.arquivo = new RandomAccessFile(this.nomeArquivo, "rw");
        this.indicePrimario = new HashExtensivelIntLong(nomeArquivo + "_pk");

        if (arquivo.length() < TAM_CABECALHO) {
            arquivo.writeInt(0);
            arquivo.writeLong(-1);
        }

        if (this.indicePrimario.isNovo()) {
            reconstruirIndicePrimario();
        }
    }

    public int create(T obj) throws Exception {
        arquivo.seek(0);
        int novoID = arquivo.readInt() + 1;
        arquivo.seek(0);
        arquivo.writeInt(novoID);
        obj.setId(novoID);

        byte[] dados = obj.toByteArray();
        arquivo.seek(arquivo.length());
        long endereco = arquivo.getFilePointer();

        arquivo.writeByte(' ');
        arquivo.writeShort(dados.length);
        arquivo.write(dados);
        indicePrimario.inserirOuAtualizar(obj.getId(), endereco);
        return obj.getId();
    }

    public T read(int id) throws Exception {
        long endereco = indicePrimario.buscar(id);
        if (endereco < TAM_CABECALHO || endereco >= arquivo.length()) {
            return null;
        }

        arquivo.seek(endereco);
        byte lapide = arquivo.readByte();
        short tamanho = arquivo.readShort();
        if (tamanho <= 0 || arquivo.getFilePointer() + tamanho > arquivo.length()) {
            return null;
        }
        byte[] dados = new byte[tamanho];
        arquivo.read(dados);
        if (lapide != ' ') {
            return null;
        }

        T obj = construtor.newInstance();
        obj.fromByteArray(dados);
        return obj.getId() == id ? obj : null;
    }

    public java.util.List<T> readAll() throws Exception {
        java.util.List<T> lista = new java.util.ArrayList<>();
        java.util.Set<Integer> idsLidos = new java.util.HashSet<>();

        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            if (arquivo.length() - posicao < 3) {
                break;
            }
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            if (tamanho <= 0 || arquivo.getFilePointer() + tamanho > arquivo.length()) {
                break;
            }
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (idsLidos.add(obj.getId())) {
                    lista.add(obj);
                }
            }
        }

        // Recupera registros ainda acessiveis pelo indice primario quando o arquivo
        // contem sobras de reaproveitamento antigo que impedem a varredura completa.
        arquivo.seek(0);
        int ultimoID = arquivo.readInt();
        for (int id = 1; id <= ultimoID; id++) {
            if (!idsLidos.contains(id)) {
                T obj = read(id);
                if (obj != null && idsLidos.add(obj.getId())) {
                    lista.add(obj);
                }
            }
        }
        return lista;
    }

    public boolean delete(int id) throws Exception {
        long posicao = indicePrimario.buscar(id);
        if (posicao < TAM_CABECALHO || posicao >= arquivo.length()) {
            return false;
        }

        arquivo.seek(posicao);
        byte lapide = arquivo.readByte();
        short tamanho = arquivo.readShort();
        if (lapide != ' ') {
            return false;
        }

        arquivo.seek(posicao);
        arquivo.writeByte('*');
        addDeleted(tamanho, posicao);
        indicePrimario.remover(id);
        return true;
    }

    public boolean update(T novoObj) throws Exception {
        long posicao = indicePrimario.buscar(novoObj.getId());
        if (posicao < TAM_CABECALHO || posicao >= arquivo.length()) {
            return false;
        }

        arquivo.seek(posicao);
        byte lapide = arquivo.readByte();
        short tamanho = arquivo.readShort();
        if (lapide != ' ') {
            return false;
        }

        byte[] novosDados = novoObj.toByteArray();
        short novoTam = (short) novosDados.length;
        if (novoTam <= tamanho) {
            arquivo.seek(posicao + 3);
            arquivo.write(novosDados);
            return true;
        }

        arquivo.seek(posicao);
        arquivo.writeByte('*');
        addDeleted(tamanho, posicao);

        arquivo.seek(arquivo.length());
        long novoEndereco = arquivo.getFilePointer();

        arquivo.writeByte(' ');
        arquivo.writeShort(novoTam);
        arquivo.write(novosDados);
        indicePrimario.inserirOuAtualizar(novoObj.getId(), novoEndereco);
        return true;
    }

    private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong();
        long proximo;

        if (endereco == -1) {
            arquivo.seek(4);
            arquivo.writeLong(enderecoEspaco);
            arquivo.seek(enderecoEspaco + 3);
            arquivo.writeLong(-1);
        } else {
            do {
                arquivo.seek(endereco + 1);
                int tamanho = arquivo.readShort();
                proximo = arquivo.readLong();

                if (tamanho > tamanhoEspaco) {
                    if (posicao == 4) {
                        arquivo.seek(posicao);
                    } else {
                        arquivo.seek(posicao + 3);
                    }
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(endereco);
                    break;
                }

                if (proximo == -1) {
                    arquivo.seek(endereco + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(-1);
                    break;
                }

                posicao = endereco;
                endereco = proximo;
            } while (endereco != -1);
        }
    }

    private long getDeleted(int tamanhoNecessario) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong();
        long proximo;
        int tamanho;

        while (endereco != -1) {
            arquivo.seek(endereco + 1);
            tamanho = arquivo.readShort();
            proximo = arquivo.readLong();
            if (tamanho >= tamanhoNecessario) {
                if (posicao == 4) {
                    arquivo.seek(posicao);
                } else {
                    arquivo.seek(posicao + 3);
                }
                arquivo.writeLong(proximo);
                return endereco;
            }
            posicao = endereco;
            endereco = proximo;
        }
        return -1;
    }

    private void reconstruirIndicePrimario() throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long endereco = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                indicePrimario.inserirOuAtualizar(obj.getId(), endereco);
            }
        }
    }

    public void close() throws Exception {
        indicePrimario.close();
        arquivo.close();
    }
}
