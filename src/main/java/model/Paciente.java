package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Paciente implements Registro {
    // Variáveis principais
    private int id;
    private String nome;
    private String cpf;
    private List<String> alergias;
    private byte lapide;

    // Construtor
    public Paciente(String nome, String cpf, List<String> alergias) {
        this.id = -1; // Id atribuido no BD
        this.nome = nome;
        this.cpf = cpf;
        this.alergias = alergias;
        this.lapide = ' '; // ' ' para ativo, '*' para excluído
    }

    // Construtor vazio
    public Paciente() {
        this.alergias = new ArrayList<>();
    }

    // Getters e Setters
    // Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Nome
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // CPF
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    // Alergias
    public List<String> getAlergias() {
        return alergias;
    }

    public void setAlergias(List<String> alergias) {
        this.alergias = alergias;
    }

    // Lapide
    public byte getLapide() {
        return lapide;
    }

    public void setLapide(byte lapide) {
        this.lapide = lapide;
    }

    // Pegar dados e colocar em um array de bytes para salvar no arquivo
    public byte[] toByteArray() throws IOException {
        // Cria um espaço para guardar os bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Tradutor dos tipos para bytes
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.id);
        // Guarda o tamanho (4 bytes) e depois a informação
        dos.writeUTF(this.nome);
        dos.writeUTF(this.cpf);
        // Grava um número inteiro (4 bytes) dizendo quantos itens a lista tem
        dos.writeInt(this.alergias.size());
        // Percorre a lista e grava cada string
        for (String alergia : this.alergias) {
            dos.writeUTF(alergia);
        }

        return baos.toByteArray();
    }

    // Pegar o array de bytes do arquivo e transformar de volta em variáveis
    public void fromByteArray(byte[] b) throws IOException {
        // Pega o pacote de bytes
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        // Lê bytes da esteira e transforma de volta em variáveis
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        // Lê os 2 primeiros bytes para saber o tamanho, depois lê o resto e monta a
        // string
        this.nome = dis.readUTF();
        this.cpf = dis.readUTF();
        // Lê a quantidade depois lê cada string e adiciona na lista
        int quantidadeAlergias = dis.readInt();
        for (int i = 0; i < quantidadeAlergias; i++) {
            this.alergias.add(dis.readUTF());
        }
    }

    // Método para imprimir os dados do paciente
    @Override
    public String toString() {
        return "\n--- DADOS DO PACIENTE ---" +
                "\nID........: " + this.id +
                "\nNome......: " + this.nome +
                "\nCPF.......: " + this.cpf +
                "\nAlergias..: " + (this.alergias.isEmpty() ? "Nenhuma" : String.join(", ", this.alergias)) +
                "\nStatus....: " + (this.lapide == 0 ? "Ativo" : "Excluído");
    }
}