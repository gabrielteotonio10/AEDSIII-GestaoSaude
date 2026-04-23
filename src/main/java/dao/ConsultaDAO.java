package dao;

import model.Consulta;
import java.lang.reflect.Constructor;
import java.util.List;

public class ConsultaDAO {
    private Arquivo<Consulta> arquivo;
    private IndicePacienteConsulta indicePacienteConsulta;

    public ConsultaDAO() throws Exception {
        Constructor<Consulta> construtor = Consulta.class.getConstructor();
        this.arquivo = new Arquivo<>("consultas", construtor);
        this.indicePacienteConsulta = new IndicePacienteConsulta();
        if (this.indicePacienteConsulta.isNovo()) {
            this.indicePacienteConsulta.reconstruir(this.arquivo.readAll());
        }
    }

    public int incluir(Consulta c) throws Exception {
        int id = arquivo.create(c);
        indicePacienteConsulta.adicionarConsulta(c.getIdPaciente(), id);
        return id;
    }

    public Consulta buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public List<Consulta> listarTodos() throws Exception {
        return arquivo.readAll();
    }

    public boolean alterar(Consulta c) throws Exception {
        Consulta antiga = arquivo.read(c.getId());
        if (antiga == null) {
            return false;
        }

        boolean ok = arquivo.update(c);
        if (ok && antiga.getIdPaciente() != c.getIdPaciente()) {
            indicePacienteConsulta.removerConsulta(antiga.getIdPaciente(), c.getId());
            indicePacienteConsulta.adicionarConsulta(c.getIdPaciente(), c.getId());
        }
        return ok;
    }

    public boolean excluir(int id) throws Exception {
        Consulta consulta = arquivo.read(id);
        if (consulta == null) {
            return false;
        }

        boolean ok = arquivo.delete(id);
        if (ok) {
            indicePacienteConsulta.removerConsulta(consulta.getIdPaciente(), id);
        }
        return ok;
    }

    /** Retorna todas as consultas de um determinado paciente */
    public List<Consulta> listarPorPaciente(int idPaciente) throws Exception {
        List<Consulta> filtradas = new java.util.ArrayList<>();
        List<Integer> ids = indicePacienteConsulta.listarConsultasDoPaciente(idPaciente);
        for (int idConsulta : ids) {
            Consulta c = arquivo.read(idConsulta);
            if (c != null) {
                filtradas.add(c);
            }
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

    public boolean pacientePossuiConsultas(int idPaciente) throws Exception {
        return indicePacienteConsulta.possuiConsultas(idPaciente);
    }
}
