package dao;

import model.Paciente;
import busca.BoyerMoore;
import busca.KMP;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
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

    /**
     * Pesquisa pacientes cujo NOME contem o padrao informado, aplicando um
     * algoritmo de casamento de padroes (KMP ou Boyer-Moore) sobre o campo
     * textual nome. A comparacao e feita em caixa baixa (case-insensitive).
     *
     * @param padrao    texto a ser procurado dentro do nome
     * @param algoritmo "BM" para Boyer-Moore; qualquer outro valor usa KMP
     * @return lista de pacientes cujo nome contem o padrao
     */
    public List<Paciente> pesquisarPorNome(String padrao, String algoritmo) throws Exception {
        List<Paciente> encontrados = new ArrayList<>();
        if (padrao == null || padrao.isEmpty()) {
            return encontrados;
        }
        boolean usarBM = "BM".equalsIgnoreCase(algoritmo) || "boyer-moore".equalsIgnoreCase(algoritmo);
        String alvo = padrao.toLowerCase();
        for (Paciente p : arquivo.readAll()) {
            String nome = (p.getNome() == null ? "" : p.getNome()).toLowerCase();
            boolean achou = usarBM ? BoyerMoore.contem(nome, alvo) : KMP.contem(nome, alvo);
            if (achou) {
                encontrados.add(p);
            }
        }
        return encontrados;
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