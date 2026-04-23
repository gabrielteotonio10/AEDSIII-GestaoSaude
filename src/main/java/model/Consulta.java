package model;

import java.io.*;

public class Consulta implements Registro {
    private int id;
    private long dataHora; // milissegundos epoch
    private String status; // "Marcada", "Realizada", "Cancelada"
    private int idPaciente;
    private int idUsuario; // médico
    private byte lapide;

    public Consulta(long dataHora, String status, int idPaciente, int idUsuario) {
        this.id = -1;
        this.dataHora = dataHora;
        this.status = status;
        this.idPaciente = idPaciente;
        this.idUsuario = idUsuario;
        this.lapide = ' ';
    }

    public Consulta() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDataHora() {
        return dataHora;
    }

    public void setDataHora(long dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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
        dos.writeLong(this.dataHora);
        dos.writeUTF(this.status);
        dos.writeInt(this.idPaciente);
        dos.writeInt(this.idUsuario);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.dataHora = dis.readLong();
        this.status = dis.readUTF();
        this.idPaciente = dis.readInt();
        this.idUsuario = dis.readInt();
    }

    @Override
    public String toString() {
        return "\n--- CONSULTA ---" +
                "\nID: " + id +
                "\nData/Hora: " + dataHora +
                "\nStatus: " + status +
                "\nPaciente ID: " + idPaciente +
                "\nMédico ID: " + idUsuario;
    }
}