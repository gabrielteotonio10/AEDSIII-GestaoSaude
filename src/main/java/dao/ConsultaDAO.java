package dao;

import model.Consulta;
import java.lang.reflect.Constructor;
import java.util.List;

public class ConsultaDAO {
    private Arquivo<Consulta> arquivo;

    public ConsultaDAO() throws Exception {
        Constructor<Consulta> construtor = Consulta.class.getConstructor();
        this.arquivo = new Arquivo<>("consultas", construtor);
    }

    public int incluir(Consulta c) throws Exception {
        return arquivo.create(c);
    }

    public Consulta buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public List<Consulta> listarTodos() throws Exception {
        return arquivo.readAll();
    }

    public boolean alterar(Consulta c) throws Exception {
        return arquivo.update(c);
    }

    public boolean excluir(int id) throws Exception {
        return arquivo.delete(id);
    }

    /** Retorna todas as consultas de um determinado paciente */
    public List<Consulta> listarPorPaciente(int idPaciente) throws Exception {
        List<Consulta> todas = arquivo.readAll();
        List<Consulta> filtradas = new java.util.ArrayList<>();
        for (Consulta c : todas) {
            if (c.getIdPaciente() == idPaciente)
                filtradas.add(c);
        }
        return filtradas;
    }

    /** Retorna todas as consultas de um determinado médico */
    public List<Consulta> listarPorMedico(int idUsuario) throws Exception {
        List<Consulta> todas = arquivo.readAll();
        List<Consulta> filtradas = new java.util.ArrayList<>();
        for (Consulta c : todas) {
            if (c.getIdUsuario() == idUsuario)
                filtradas.add(c);
        }
        return filtradas;
    }
}