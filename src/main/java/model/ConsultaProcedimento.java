package model;

import java.io.*;

public class ConsultaProcedimento implements Registro {
    private int id; // id próprio para compatibilidade com Arquivo<T>
    private int idConsulta;
    private int idProcedimento;
    private String observacao;
    private byte lapide;

    public ConsultaProcedimento(int idConsulta, int idProcedimento, String observacao) {
        this.id = -1;
        this.idConsulta = idConsulta;
        this.idProcedimento = idProcedimento;
        this.observacao = observacao;
        this.lapide = ' ';
    }

    public ConsultaProcedimento() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(int idConsulta) {
        this.idConsulta = idConsulta;
    }

    public int getIdProcedimento() {
        return idProcedimento;
    }

    public void setIdProcedimento(int idProcedimento) {
        this.idProcedimento = idProcedimento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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
        dos.writeInt(this.idConsulta);
        dos.writeInt(this.idProcedimento);
        dos.writeUTF(this.observacao == null ? "" : this.observacao);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.idConsulta = dis.readInt();
        this.idProcedimento = dis.readInt();
        this.observacao = dis.readUTF();
    }

    @Override
    public String toString() {
        return "\n--- CONSULTA_PROCEDIMENTO ---" +
                "\nID: " + id +
                "\nConsulta ID: " + idConsulta +
                "\nProcedimento ID: " + idProcedimento +
                "\nObservação: " + observacao;
    }
}