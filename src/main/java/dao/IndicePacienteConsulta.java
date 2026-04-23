package dao;

import java.util.ArrayList;
import java.util.List;

import model.Consulta;

public class IndicePacienteConsulta {
    private final HashExtensivelIntLong hash;
    private final ListaInvertidaConsulta lista;

    public IndicePacienteConsulta() throws Exception {
        this.hash = new HashExtensivelIntLong("paciente_consulta");
        this.lista = new ListaInvertidaConsulta("paciente_consulta");
    }

    public boolean isNovo() {
        return hash.isNovo();
    }

    public void reconstruir(List<Consulta> consultas) throws Exception {
        for (Consulta consulta : consultas) {
            adicionarConsulta(consulta.getIdPaciente(), consulta.getId());
        }
    }

    public void adicionarConsulta(int idPaciente, int idConsulta) throws Exception {
        long inicio = hash.buscar(idPaciente);
        long novoInicio = lista.inserirNoInicio(idConsulta, inicio);
        hash.inserirOuAtualizar(idPaciente, novoInicio);
    }

    public void removerConsulta(int idPaciente, int idConsulta) throws Exception {
        long inicio = hash.buscar(idPaciente);
        if (inicio == -1) {
            return;
        }

        long novoInicio = lista.remover(inicio, idConsulta);
        if (novoInicio == -1) {
            hash.remover(idPaciente);
        } else {
            List<Integer> ids = lista.listarIds(novoInicio);
            if (ids.isEmpty()) {
                hash.remover(idPaciente);
            } else {
                hash.inserirOuAtualizar(idPaciente, novoInicio);
            }
        }
    }

    public List<Integer> listarConsultasDoPaciente(int idPaciente) throws Exception {
        long inicio = hash.buscar(idPaciente);
        if (inicio == -1) {
            return new ArrayList<>();
        }
        return lista.listarIds(inicio);
    }

    public boolean possuiConsultas(int idPaciente) throws Exception {
        long inicio = hash.buscar(idPaciente);
        if (inicio == -1) {
            return false;
        }
        return !lista.listarIds(inicio).isEmpty();
    }

    public void close() throws Exception {
        hash.close();
        lista.close();
    }
}
