package dao;

import java.io.File;
import java.io.RandomAccessFile;

public class HashExtensivelIntLong {
    private static final int TAM_BUCKET = 4;
    private static final int TAM_CABECALHO_DIRETORIO = 4;
    private static final int TAM_ENTRADA_BUCKET = 12;
    private static final int TAM_BUCKET_BYTES = 1 + 2 + (TAM_BUCKET * TAM_ENTRADA_BUCKET);

    private final RandomAccessFile diretorio;
    private final RandomAccessFile buckets;
    private final String caminhoDiretorio;
    private final String caminhoBuckets;
    private boolean novo;

    public HashExtensivelIntLong(String nomeBase) throws Exception {
        File pasta = new File("./data/indices");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        this.caminhoDiretorio = "./data/indices/" + nomeBase + ".dir";
        this.caminhoBuckets = "./data/indices/" + nomeBase + ".bkt";
        this.diretorio = new RandomAccessFile(this.caminhoDiretorio, "rw");
        this.buckets = new RandomAccessFile(this.caminhoBuckets, "rw");
        this.novo = this.diretorio.length() == 0 || this.buckets.length() == 0;

        if (this.novo) {
            inicializar();
        }
    }

    private void inicializar() throws Exception {
        diretorio.setLength(0);
        buckets.setLength(0);

        long bucket0 = criarBucket((byte) 1);
        long bucket1 = criarBucket((byte) 1);

        diretorio.seek(0);
        diretorio.writeInt(1);
        diretorio.writeLong(bucket0);
        diretorio.writeLong(bucket1);
    }

    public boolean isNovo() {
        return novo;
    }

    public long buscar(int chave) throws Exception {
        Bucket bucket = lerBucketPelaChave(chave);
        for (int i = 0; i < bucket.quantidade; i++) {
            if (bucket.chaves[i] == chave) {
                return bucket.valores[i];
            }
        }
        return -1;
    }

    public void inserirOuAtualizar(int chave, long valor) throws Exception {
        while (true) {
            int profundidadeGlobal = getProfundidadeGlobal();
            int indiceDiretorio = hash(chave, profundidadeGlobal);
            long enderecoBucket = getEnderecoBucket(indiceDiretorio);
            Bucket bucket = lerBucket(enderecoBucket);

            for (int i = 0; i < bucket.quantidade; i++) {
                if (bucket.chaves[i] == chave) {
                    bucket.valores[i] = valor;
                    escreverBucket(enderecoBucket, bucket);
                    return;
                }
            }

            if (bucket.quantidade < TAM_BUCKET) {
                bucket.chaves[bucket.quantidade] = chave;
                bucket.valores[bucket.quantidade] = valor;
                bucket.quantidade++;
                escreverBucket(enderecoBucket, bucket);
                return;
            }

            dividirBucket(indiceDiretorio, enderecoBucket, bucket);
        }
    }

    public boolean remover(int chave) throws Exception {
        int indiceDiretorio = hash(chave, getProfundidadeGlobal());
        long enderecoBucket = getEnderecoBucket(indiceDiretorio);
        Bucket bucket = lerBucket(enderecoBucket);

        for (int i = 0; i < bucket.quantidade; i++) {
            if (bucket.chaves[i] == chave) {
                for (int j = i; j < bucket.quantidade - 1; j++) {
                    bucket.chaves[j] = bucket.chaves[j + 1];
                    bucket.valores[j] = bucket.valores[j + 1];
                }
                bucket.quantidade--;
                bucket.chaves[bucket.quantidade] = 0;
                bucket.valores[bucket.quantidade] = 0;
                escreverBucket(enderecoBucket, bucket);
                return true;
            }
        }
        return false;
    }

    public void close() throws Exception {
        diretorio.close();
        buckets.close();
    }

    private Bucket lerBucketPelaChave(int chave) throws Exception {
        int indiceDiretorio = hash(chave, getProfundidadeGlobal());
        return lerBucket(getEnderecoBucket(indiceDiretorio));
    }

    private void dividirBucket(int indiceDiretorio, long enderecoBucket, Bucket bucketAntigo) throws Exception {
        int profundidadeGlobal = getProfundidadeGlobal();
        if (bucketAntigo.profundidadeLocal == profundidadeGlobal) {
            duplicarDiretorio();
            profundidadeGlobal = getProfundidadeGlobal();
            indiceDiretorio = hash(indiceDiretorio, profundidadeGlobal);
        }

        byte novaProfundidadeLocal = (byte) (bucketAntigo.profundidadeLocal + 1);
        Bucket bucketNovo = new Bucket(novaProfundidadeLocal);
        long enderecoBucketNovo = criarBucket(novaProfundidadeLocal);

        bucketAntigo.profundidadeLocal = novaProfundidadeLocal;
        int mascaraBit = 1 << (novaProfundidadeLocal - 1);
        int tamanhoDiretorio = 1 << getProfundidadeGlobal();

        for (int i = 0; i < tamanhoDiretorio; i++) {
            if (getEnderecoBucket(i) == enderecoBucket && (i & mascaraBit) != 0) {
                setEnderecoBucket(i, enderecoBucketNovo);
            }
        }

        int[] chaves = new int[bucketAntigo.quantidade];
        long[] valores = new long[bucketAntigo.quantidade];
        for (int i = 0; i < bucketAntigo.quantidade; i++) {
            chaves[i] = bucketAntigo.chaves[i];
            valores[i] = bucketAntigo.valores[i];
        }

        bucketAntigo.quantidade = 0;
        for (int i = 0; i < TAM_BUCKET; i++) {
            bucketAntigo.chaves[i] = 0;
            bucketAntigo.valores[i] = 0;
            bucketNovo.chaves[i] = 0;
            bucketNovo.valores[i] = 0;
        }

        for (int i = 0; i < chaves.length; i++) {
            int indice = hash(chaves[i], getProfundidadeGlobal());
            if (getEnderecoBucket(indice) == enderecoBucket) {
                adicionarNoBucket(bucketAntigo, chaves[i], valores[i]);
            } else {
                adicionarNoBucket(bucketNovo, chaves[i], valores[i]);
            }
        }

        escreverBucket(enderecoBucket, bucketAntigo);
        escreverBucket(enderecoBucketNovo, bucketNovo);
    }

    private void duplicarDiretorio() throws Exception {
        int profundidadeGlobal = getProfundidadeGlobal();
        int tamanhoAtual = 1 << profundidadeGlobal;
        long[] enderecos = new long[tamanhoAtual];

        diretorio.seek(TAM_CABECALHO_DIRETORIO);
        for (int i = 0; i < tamanhoAtual; i++) {
            enderecos[i] = diretorio.readLong();
        }

        diretorio.seek(0);
        diretorio.writeInt(profundidadeGlobal + 1);
        for (int i = 0; i < tamanhoAtual; i++) {
            diretorio.writeLong(enderecos[i]);
        }
        for (int i = 0; i < tamanhoAtual; i++) {
            diretorio.writeLong(enderecos[i]);
        }
    }

    private void adicionarNoBucket(Bucket bucket, int chave, long valor) {
        bucket.chaves[bucket.quantidade] = chave;
        bucket.valores[bucket.quantidade] = valor;
        bucket.quantidade++;
    }

    private int getProfundidadeGlobal() throws Exception {
        diretorio.seek(0);
        return diretorio.readInt();
    }

    private long getEnderecoBucket(int indice) throws Exception {
        diretorio.seek(TAM_CABECALHO_DIRETORIO + (long) indice * 8);
        return diretorio.readLong();
    }

    private void setEnderecoBucket(int indice, long enderecoBucket) throws Exception {
        diretorio.seek(TAM_CABECALHO_DIRETORIO + (long) indice * 8);
        diretorio.writeLong(enderecoBucket);
    }

    private long criarBucket(byte profundidadeLocal) throws Exception {
        long endereco = buckets.length();
        Bucket bucket = new Bucket(profundidadeLocal);
        escreverBucket(endereco, bucket);
        return endereco;
    }

    private Bucket lerBucket(long endereco) throws Exception {
        buckets.seek(endereco);
        Bucket bucket = new Bucket(buckets.readByte());
        bucket.quantidade = buckets.readShort();
        for (int i = 0; i < TAM_BUCKET; i++) {
            bucket.chaves[i] = buckets.readInt();
            bucket.valores[i] = buckets.readLong();
        }
        return bucket;
    }

    private void escreverBucket(long endereco, Bucket bucket) throws Exception {
        buckets.seek(endereco);
        buckets.writeByte(bucket.profundidadeLocal);
        buckets.writeShort(bucket.quantidade);
        for (int i = 0; i < TAM_BUCKET; i++) {
            buckets.writeInt(bucket.chaves[i]);
            buckets.writeLong(bucket.valores[i]);
        }
    }

    private int hash(int chave, int profundidade) {
        int mascara = (1 << profundidade) - 1;
        return (chave & 0x7fffffff) & mascara;
    }

    private static class Bucket {
        byte profundidadeLocal;
        short quantidade;
        int[] chaves = new int[TAM_BUCKET];
        long[] valores = new long[TAM_BUCKET];

        Bucket(byte profundidadeLocal) {
            this.profundidadeLocal = profundidadeLocal;
            this.quantidade = 0;
        }
    }
}
