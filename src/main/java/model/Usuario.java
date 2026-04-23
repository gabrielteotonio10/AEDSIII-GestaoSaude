package model;

import java.io.*;

public class Usuario implements Registro {

    // Variáveis principais
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private String papel;
    private String especialidade; // "" Se não for médico
    private byte lapide;

    // Construtor
    public Usuario(String nome, String cpf, String email, String senha, String papel, String especialidade) {
        this.id = -1;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.papel = papel;
        this.especialidade = especialidade;
        this.lapide = ' ';
    }

    // Construtor vazio
    public Usuario() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public byte getLapide() {
        return lapide;
    }

    public void setLapide(byte lapide) {
        this.lapide = lapide;
    }

    // Pegar dados e colocar em um array de bytes para salvar no arquivo
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.cpf);
        dos.writeUTF(this.email);
        dos.writeUTF(this.senha);
        dos.writeUTF(this.papel);
        dos.writeUTF(this.especialidade);

        return baos.toByteArray();
    }

    // Pegar o array de bytes do arquivo e transformar de volta em variáveis
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.nome = dis.readUTF();
        this.cpf = dis.readUTF();
        this.email = dis.readUTF();
        this.senha = dis.readUTF();
        this.papel = dis.readUTF();
        this.especialidade = dis.readUTF();
    }

    // Método para imprimir os dados do paciente
    @Override
    public String toString() {
        return "\n--- DADOS DO PACIENTE ---" +
                "\nID........: " + this.id +
                "\nNome......: " + this.nome +
                "\nCPF.......: " + this.cpf +
                "\nEMAIL.......: " + this.email +
                "\nSENHA.......: " + this.senha +
                "\nPAPEL.......: " + this.papel +
                "\nStatus....: " + (this.lapide == 0 ? "Ativo" : "Excluído");
    }

}