package dao;

import model.Paciente;
import java.lang.reflect.Constructor;
import java.util.List;

public class PacienteDAO {
    private Arquivo<Paciente> arquivo;

    public PacienteDAO() throws Exception {
        // Pega o construtor vazio da classe Paciente
        Constructor<Paciente> construtor = Paciente.class.getConstructor();
        // Inicializa o motor: nome da pasta/arquivo será "pacientes"
        this.arquivo = new Arquivo<>("pacientes", construtor);
    }

    public List<Paciente> listarTodos() throws Exception {
        // O método readAll percorre o arquivo binário, pula os registros com lápide
        return arquivo.readAll();
    }

    // Métodos que apenas repassam a ordem para Arquivo.java
    public int incluir(Paciente p) throws Exception {
        return arquivo.create(p);
    }

    public Paciente buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public boolean alterar(Paciente p) throws Exception {
        return arquivo.update(p);
    }

    public boolean excluir(int id) throws Exception {
        return arquivo.delete(id);
    }
}