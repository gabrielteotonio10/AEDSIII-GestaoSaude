package dao;

import model.Procedimento;
import java.lang.reflect.Constructor;
import java.util.List;

public class ProcedimentoDAO {
    private Arquivo<Procedimento> arquivo;
    private ArvoreBMaisProcedimentoNome indiceNome;

    public ProcedimentoDAO() throws Exception {
        Constructor<Procedimento> construtor = Procedimento.class.getConstructor();
        this.arquivo = new Arquivo<>("procedimentos", construtor);
        this.indiceNome = new ArvoreBMaisProcedimentoNome();
        if (this.indiceNome.isNovo()) {
            reconstruirIndiceNome();
        }
    }

    public int incluir(Procedimento p) throws Exception {
        int id = arquivo.create(p);
        reconstruirIndiceNome();
        return id;
    }

    public Procedimento buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public List<Procedimento> listarTodos() throws Exception {
        return arquivo.readAll();
    }

    public List<Procedimento> listarOrdenadoPorNome() throws Exception {
        List<Procedimento> ordenados = new java.util.ArrayList<>();
        for (int id : indiceNome.listarIdsOrdenados()) {
            Procedimento p = arquivo.read(id);
            if (p != null) {
                ordenados.add(p);
            }
        }
        return ordenados;
    }

    public boolean alterar(Procedimento p) throws Exception {
        boolean ok = arquivo.update(p);
        if (ok) {
            reconstruirIndiceNome();
        }
        return ok;
    }

    public boolean excluir(int id) throws Exception {
        boolean ok = arquivo.delete(id);
        if (ok) {
            reconstruirIndiceNome();
        }
        return ok;
    }

    private void reconstruirIndiceNome() throws Exception {
        indiceNome.reconstruir(arquivo.readAll());
    }
}
