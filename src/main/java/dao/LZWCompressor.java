package dao;

import java.io.*;
import java.util.*;

public class LZWCompressor {

    private static final int MAX_BITS = 12;
    private static final int MAX_TABLE_SIZE = 1 << MAX_BITS; // 4096

    public byte[] comprimir(byte[] dados) throws IOException {
        if (dados.length == 0) return new byte[0];

        Map<String, Integer> dicionario = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dicionario.put(String.valueOf((char) i), i);
        }

        List<Integer> codigos = new ArrayList<>();
        int proximoCodigo = 256;
        String w = "";

        for (byte b : dados) {
            String c = String.valueOf((char) (b & 0xFF));
            String wc = w + c;
            if (dicionario.containsKey(wc)) {
                w = wc;
            } else {
                codigos.add(dicionario.get(w));
                if (proximoCodigo < MAX_TABLE_SIZE) {
                    dicionario.put(wc, proximoCodigo++);
                }
                w = c;
            }
        }

        if (!w.isEmpty()) codigos.add(dicionario.get(w));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(codigos.size());

        // Empacotar os códigos em 12 bits
        int buffer = 0;
        int bitsNoBuffer = 0;

        for (int codigo : codigos) {
            buffer = (buffer << MAX_BITS) | codigo;
            bitsNoBuffer += MAX_BITS;
            while (bitsNoBuffer >= 8) {
                bitsNoBuffer -= 8;
                dos.writeByte(buffer >> bitsNoBuffer);
                buffer &= (1 << bitsNoBuffer) - 1;
            }
        }

        if (bitsNoBuffer > 0) {
            dos.writeByte(buffer << (8 - bitsNoBuffer));
        }

        dos.flush();
        return baos.toByteArray();
    }

    public byte[] descomprimir(byte[] comprimido) throws IOException {
        if (comprimido.length == 0) return new byte[0];

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(comprimido));
        int numeroCodigos = dis.readInt();

        byte[] bytesComprimidos = dis.readAllBytes();

        // Desempacotar os códigos de 12 bits
        List<Integer> codigos = new ArrayList<>(numeroCodigos);
        int buffer = 0;
        int bitsNoBuffer = 0;
        int byteIdx = 0;

        while (codigos.size() < numeroCodigos) {
            while (bitsNoBuffer < MAX_BITS && byteIdx < bytesComprimidos.length) {
                buffer = (buffer << 8) | (bytesComprimidos[byteIdx++] & 0xFF);
                bitsNoBuffer += 8;
            }
            if (bitsNoBuffer < MAX_BITS) break;
            bitsNoBuffer -= MAX_BITS;
            codigos.add((buffer >> bitsNoBuffer) & 0xFFF);
            buffer &= (1 << bitsNoBuffer) - 1;
        }

        if (codigos.isEmpty()) return new byte[0];

        Map<Integer, String> dicionario = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dicionario.put(i, String.valueOf((char) i));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int proximoCodigo = 256;

        String anterior = dicionario.get(codigos.get(0));
        for (char ch : anterior.toCharArray()) baos.write(ch);

        for (int i = 1; i < codigos.size(); i++) {
            int codigo = codigos.get(i);
            String entrada;

            if (dicionario.containsKey(codigo)) {
                entrada = dicionario.get(codigo);
            } else if (codigo == proximoCodigo) {
                // Caso especial LZW: código ainda não está no dicionário
                entrada = anterior + anterior.charAt(0);
            } else {
                throw new IOException("Código LZW inválido: " + codigo);
            }

            for (char ch : entrada.toCharArray()) baos.write(ch);

            if (proximoCodigo < MAX_TABLE_SIZE) {
                dicionario.put(proximoCodigo++, anterior + entrada.charAt(0));
            }
            anterior = entrada;
        }

        return baos.toByteArray();
    }
}
