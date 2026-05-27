package dao;

import java.util.ArrayList;
import java.util.List;

import model.ConsultaProcedimento;

public class IndiceConsultaProcedimento {
    private final HashExtensivelLongLong chaveComposta;
    private final HashExtensivelIntLong porConsulta;
    private final HashExtensivelIntLong porProcedimento;
    private final ListaInvertidaConsulta listaPorConsulta;
    private final ListaInvertidaConsulta listaPorProcedimento;

    public IndiceConsultaProcedimento() throws Exception {
        this.chaveComposta = new HashExtensivelLongLong("consulta_procedimento_pk_composta");
        this.porConsulta = new HashExtensivelIntLong("consulta_procedimento_por_consulta");
        this.porProcedimento = new HashExtensivelIntLong("consulta_procedimento_por_procedimento");
        this.listaPorConsulta = new ListaInvertidaConsulta("consulta_procedimento_por_consulta");
        this.listaPorProcedimento = new ListaInvertidaConsulta("consulta_procedimento_por_procedimento");
    }

    public boolean precisaReconstruir() {
        return chaveComposta.isNovo() || porConsulta.isNovo() || porProcedimento.isNovo();
    }

    public void reconstruir(List<ConsultaProcedimento> vinculos) throws Exception {
        chaveComposta.resetar();
        porConsulta.resetar();
        porProcedimento.resetar();
        listaPorConsulta.resetar();
        listaPorProcedimento.resetar();
        for (ConsultaProcedimento cp : vinculos) {
            adicionar(cp);
        }
    }

    public long buscarIdVinculo(int idConsulta, int idProcedimento) throws Exception {
        return chaveComposta.buscar(chaveComposta(idConsulta, idProcedimento));
    }

    public void adicionar(ConsultaProcedimento cp) throws Exception {
        chaveComposta.inserirOuAtualizar(chaveComposta(cp.getIdConsulta(), cp.getIdProcedimento()), cp.getId());
        adicionarNaLista(porConsulta, listaPorConsulta, cp.getIdConsulta(), cp.getId());
        adicionarNaLista(porProcedimento, listaPorProcedimento, cp.getIdProcedimento(), cp.getId());
    }

    public void remover(ConsultaProcedimento cp) throws Exception {
        chaveComposta.remover(chaveComposta(cp.getIdConsulta(), cp.getIdProcedimento()));
        removerDaLista(porConsulta, listaPorConsulta, cp.getIdConsulta(), cp.getId());
        removerDaLista(porProcedimento, listaPorProcedimento, cp.getIdProcedimento(), cp.getId());
    }

    public List<Integer> listarIdsPorConsulta(int idConsulta) throws Exception {
        return listarIds(porConsulta, listaPorConsulta, idConsulta);
    }

    public List<Integer> listarIdsPorProcedimento(int idProcedimento) throws Exception {
        return listarIds(porProcedimento, listaPorProcedimento, idProcedimento);
    }

    public boolean procedimentoPossuiConsultas(int idProcedimento) throws Exception {
        return !listarIdsPorProcedimento(idProcedimento).isEmpty();
    }

    public void close() throws Exception {
        chaveComposta.close();
        porConsulta.close();
        porProcedimento.close();
        listaPorConsulta.close();
        listaPorProcedimento.close();
    }

    public static long chaveComposta(int idConsulta, int idProcedimento) {
        return ((long) idConsulta << 32) | (idProcedimento & 0xffffffffL);
    }

    private void adicionarNaLista(HashExtensivelIntLong hash, ListaInvertidaConsulta lista, int chave, int idVinculo)
            throws Exception {
        long inicio = hash.buscar(chave);
        long novoInicio = lista.inserirNoInicio(idVinculo, inicio);
        hash.inserirOuAtualizar(chave, novoInicio);
    }

    private void removerDaLista(HashExtensivelIntLong hash, ListaInvertidaConsulta lista, int chave, int idVinculo)
            throws Exception {
        long inicio = hash.buscar(chave);
        if (inicio == -1) {
            return;
        }

        long novoInicio = lista.remover(inicio, idVinculo);
        if (novoInicio == -1 || lista.listarIds(novoInicio).isEmpty()) {
            hash.remover(chave);
        } else {
            hash.inserirOuAtualizar(chave, novoInicio);
        }
    }

    private List<Integer> listarIds(HashExtensivelIntLong hash, ListaInvertidaConsulta lista, int chave) throws Exception {
        long inicio = hash.buscar(chave);
        if (inicio == -1) {
            return new ArrayList<>();
        }
        return lista.listarIds(inicio);
    }
}
