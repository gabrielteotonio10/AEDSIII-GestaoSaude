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
        long endereco = getDeleted(dados.length);
        if (endereco == -1) {
            arquivo.seek(arquivo.length());
            endereco = arquivo.getFilePointer();
        } else {
            arquivo.seek(endereco);
        }

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
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                lista.add(obj);
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

        long novoEndereco = getDeleted(novosDados.length);
        if (novoEndereco == -1) {
            arquivo.seek(arquivo.length());
            novoEndereco = arquivo.getFilePointer();
        } else {
            arquivo.seek(novoEndereco);
        }

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
