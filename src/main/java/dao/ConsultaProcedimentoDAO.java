package dao;

import model.ConsultaProcedimento;
import java.lang.reflect.Constructor;
import java.util.List;

public class ConsultaProcedimentoDAO {
    private Arquivo<ConsultaProcedimento> arquivo;

    public ConsultaProcedimentoDAO() throws Exception {
        Constructor<ConsultaProcedimento> construtor = ConsultaProcedimento.class.getConstructor();
        this.arquivo = new Arquivo<>("consulta_procedimentos", construtor);
    }

    public int incluir(ConsultaProcedimento cp) throws Exception {
        return arquivo.create(cp);
    }

    public ConsultaProcedimento buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public List<ConsultaProcedimento> listarTodos() throws Exception {
        return arquivo.readAll();
    }

    public boolean alterar(ConsultaProcedimento cp) throws Exception {
        return arquivo.update(cp);
    }

    public boolean excluir(int id) throws Exception {
        return arquivo.delete(id);
    }

    /** Lista todos os procedimentos de uma consulta */
    public List<ConsultaProcedimento> listarPorConsulta(int idConsulta) throws Exception {
        List<ConsultaProcedimento> todos = arquivo.readAll();
        List<ConsultaProcedimento> filtrados = new java.util.ArrayList<>();
        for (ConsultaProcedimento cp : todos) {
            if (cp.getIdConsulta() == idConsulta)
                filtrados.add(cp);
        }
        return filtrados;
    }

    /** Remove todos os vínculos de uma consulta (usado ao excluir consulta) */
    public void excluirPorConsulta(int idConsulta) throws Exception {
        List<ConsultaProcedimento> lista = listarPorConsulta(idConsulta);
        for (ConsultaProcedimento cp : lista) {
            arquivo.delete(cp.getId());
        }
    }
}