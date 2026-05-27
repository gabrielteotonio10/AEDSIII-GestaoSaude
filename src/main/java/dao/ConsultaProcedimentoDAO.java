package dao;

import model.ConsultaProcedimento;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ConsultaProcedimentoDAO {
    private Arquivo<ConsultaProcedimento> arquivo;
    private IndiceConsultaProcedimento indice;

    public ConsultaProcedimentoDAO() throws Exception {
        Constructor<ConsultaProcedimento> construtor = ConsultaProcedimento.class.getConstructor();
        this.arquivo = new Arquivo<>("consulta_procedimentos", construtor);
        this.indice = new IndiceConsultaProcedimento();
        this.indice.reconstruir(this.arquivo.readAll());
    }

    public int incluir(ConsultaProcedimento cp) throws Exception {
        if (indice.buscarIdVinculo(cp.getIdConsulta(), cp.getIdProcedimento()) != -1) {
            throw new IllegalArgumentException("Procedimento ja vinculado a esta consulta.");
        }
        int id = arquivo.create(cp);
        indice.adicionar(cp);
        return id;
    }

    public ConsultaProcedimento buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public ConsultaProcedimento buscarPorChaveComposta(int idConsulta, int idProcedimento) throws Exception {
        long id = indice.buscarIdVinculo(idConsulta, idProcedimento);
        if (id == -1) {
            return null;
        }
        return arquivo.read((int) id);
    }

    public List<ConsultaProcedimento> listarTodos() throws Exception {
        return arquivo.readAll();
    }

    public boolean alterar(ConsultaProcedimento cp) throws Exception {
        ConsultaProcedimento antigo = arquivo.read(cp.getId());
        if (antigo == null) {
            return false;
        }

        long idExistente = indice.buscarIdVinculo(cp.getIdConsulta(), cp.getIdProcedimento());
        if (idExistente != -1 && idExistente != cp.getId()) {
            throw new IllegalArgumentException("Procedimento ja vinculado a esta consulta.");
        }

        boolean ok = arquivo.update(cp);
        if (ok) {
            indice.remover(antigo);
            indice.adicionar(cp);
        }
        return ok;
    }

    public boolean excluir(int id) throws Exception {
        ConsultaProcedimento cp = arquivo.read(id);
        if (cp == null) {
            return false;
        }
        boolean ok = arquivo.delete(id);
        if (ok) {
            indice.remover(cp);
        }
        return ok;
    }

    public boolean excluirPorChaveComposta(int idConsulta, int idProcedimento) throws Exception {
        ConsultaProcedimento cp = buscarPorChaveComposta(idConsulta, idProcedimento);
        return cp != null && excluir(cp.getId());
    }

    public List<ConsultaProcedimento> listarPorConsulta(int idConsulta) throws Exception {
        List<ConsultaProcedimento> filtrados = new ArrayList<>();
        for (int idVinculo : indice.listarIdsPorConsulta(idConsulta)) {
            ConsultaProcedimento cp = arquivo.read(idVinculo);
            if (cp != null) {
                filtrados.add(cp);
            }
        }
        return filtrados;
    }

    public List<ConsultaProcedimento> listarPorProcedimento(int idProcedimento) throws Exception {
        List<ConsultaProcedimento> filtrados = new ArrayList<>();
        for (int idVinculo : indice.listarIdsPorProcedimento(idProcedimento)) {
            ConsultaProcedimento cp = arquivo.read(idVinculo);
            if (cp != null) {
                filtrados.add(cp);
            }
        }
        return filtrados;
    }

    public boolean procedimentoPossuiConsultas(int idProcedimento) throws Exception {
        return indice.procedimentoPossuiConsultas(idProcedimento);
    }

    public void excluirPorConsulta(int idConsulta) throws Exception {
        List<ConsultaProcedimento> lista = listarPorConsulta(idConsulta);
        for (ConsultaProcedimento cp : lista) {
            excluir(cp.getId());
        }
    }
}
