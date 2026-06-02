package dao;

import java.io.*;
import java.util.PriorityQueue;

public class HuffmanCompressor {

    private static class No implements Comparable<No> {
        int simbolo;
        long frequencia;
        No esquerda, direita;

        No(int simbolo, long frequencia) {
            this.simbolo = simbolo;
            this.frequencia = frequencia;
        }

        boolean isFolha() {
            return esquerda == null && direita == null;
        }

        @Override
        public int compareTo(No outro) {
            int cmp = Long.compare(this.frequencia, outro.frequencia);
            if (cmp != 0) return cmp;
            return Integer.compare(this.simbolo, outro.simbolo);
        }
    }

    public byte[] comprimir(byte[] dados) throws IOException {
        if (dados.length == 0) return new byte[0];

        long[] freq = new long[256];
        for (byte b : dados) freq[b & 0xFF]++;

        PriorityQueue<No> fila = new PriorityQueue<>();
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) fila.add(new No(i, freq[i]));
        }

        // Caso especial: arquivo com um único valor de byte
        if (fila.size() == 1) {
            No no = fila.poll();
            No raiz = new No(-1, no.frequencia);
            raiz.esquerda = no;
            fila.add(raiz);
        }

        while (fila.size() > 1) {
            No esq = fila.poll();
            No dir = fila.poll();
            No interno = new No(-1, esq.frequencia + dir.frequencia);
            interno.esquerda = esq;
            interno.direita = dir;
            fila.add(interno);
        }

        No raiz = fila.poll();
        String[] codigos = new String[256];
        gerarCodigos(raiz, "", codigos);

        StringBuilder bits = new StringBuilder();
        for (byte b : dados) bits.append(codigos[b & 0xFF]);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int simbolosUnicos = 0;
        for (long f : freq) if (f > 0) simbolosUnicos++;
        dos.writeInt(simbolosUnicos);
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) {
                dos.writeInt(i);
                dos.writeLong(freq[i]);
            }
        }

        dos.writeLong(dados.length);

        int padding = (8 - (bits.length() % 8)) % 8;
        dos.writeByte(padding);

        for (int i = 0; i < bits.length(); i += 8) {
            int fim = Math.min(i + 8, bits.length());
            String byte8 = bits.substring(i, fim);
            while (byte8.length() < 8) byte8 += "0";
            dos.writeByte(Integer.parseInt(byte8, 2));
        }

        dos.flush();
        return baos.toByteArray();
    }

    public byte[] descomprimir(byte[] comprimido) throws IOException {
        if (comprimido.length == 0) return new byte[0];

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(comprimido));

        int simbolosUnicos = dis.readInt();
        long[] freq = new long[256];
        for (int i = 0; i < simbolosUnicos; i++) {
            int simbolo = dis.readInt();
            freq[simbolo] = dis.readLong();
        }

        PriorityQueue<No> fila = new PriorityQueue<>();
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) fila.add(new No(i, freq[i]));
        }

        if (fila.size() == 1) {
            No no = fila.poll();
            No raiz = new No(-1, no.frequencia);
            raiz.esquerda = no;
            fila.add(raiz);
        }

        while (fila.size() > 1) {
            No esq = fila.poll();
            No dir = fila.poll();
            No interno = new No(-1, esq.frequencia + dir.frequencia);
            interno.esquerda = esq;
            interno.direita = dir;
            fila.add(interno);
        }

        No raiz = fila.poll();
        long tamanhoOriginal = dis.readLong();
        int padding = dis.readByte() & 0xFF;

        byte[] bytesComprimidos = dis.readAllBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        No no = raiz;
        long simbolosDecodificados = 0;

        for (int i = 0; i < bytesComprimidos.length && simbolosDecodificados < tamanhoOriginal; i++) {
            int byteAtual = bytesComprimidos[i] & 0xFF;
            int bitsNesteBytes = (i == bytesComprimidos.length - 1) ? (8 - padding) : 8;

            for (int bit = 7; bit >= (8 - bitsNesteBytes) && simbolosDecodificados < tamanhoOriginal; bit--) {
                boolean vaDireita = ((byteAtual >> bit) & 1) == 1;
                no = vaDireita ? no.direita : no.esquerda;

                if (no.isFolha()) {
                    baos.write(no.simbolo);
                    simbolosDecodificados++;
                    no = raiz;
                }
            }
        }

        return baos.toByteArray();
    }

    private void gerarCodigos(No no, String codigo, String[] codigos) {
        if (no.isFolha()) {
            codigos[no.simbolo] = codigo.isEmpty() ? "0" : codigo;
            return;
        }
        if (no.esquerda != null) gerarCodigos(no.esquerda, codigo + "0", codigos);
        if (no.direita != null) gerarCodigos(no.direita, codigo + "1", codigos);
    }
}
