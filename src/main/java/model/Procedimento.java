package model;

import java.io.*;

public class Procedimento implements Registro {
    private int id;
    private String nomeExame;
    private float preco;
    private byte lapide;

    public Procedimento(String nomeExame, float preco) {
        this.id = -1;
        this.nomeExame = nomeExame;
        this.preco = preco;
        this.lapide = ' ';
    }

    public Procedimento() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeExame() {
        return nomeExame;
    }

    public void setNomeExame(String nomeExame) {
        this.nomeExame = nomeExame;
    }

    public float getPreco() {
        return preco;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public byte getLapide() {
        return lapide;
    }

    public void setLapide(byte lapide) {
        this.lapide = lapide;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(this.id);
        dos.writeUTF(this.nomeExame);
        dos.writeFloat(this.preco);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.nomeExame = dis.readUTF();
        this.preco = dis.readFloat();
    }

    @Override
    public String toString() {
        return "\n--- PROCEDIMENTO ---" +
                "\nID: " + id +
                "\nExame: " + nomeExame +
                "\nPreço: R$ " + String.format("%.2f", preco);
    }
}