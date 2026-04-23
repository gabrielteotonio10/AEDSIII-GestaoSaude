package dao;

import model.Procedimento;
import java.lang.reflect.Constructor;
import java.util.List;

public class ProcedimentoDAO {
    private Arquivo<Procedimento> arquivo;

    public ProcedimentoDAO() throws Exception {
        Constructor<Procedimento> construtor = Procedimento.class.getConstructor();
        this.arquivo = new Arquivo<>("procedimentos", construtor);
    }

    public int incluir(Procedimento p) throws Exception {
        return arquivo.create(p);
    }

    public Procedimento buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public List<Procedimento> listarTodos() throws Exception {
        return arquivo.readAll();
    }

    public boolean alterar(Procedimento p) throws Exception {
        return arquivo.update(p);
    }

    public boolean excluir(int id) throws Exception {
        return arquivo.delete(id);
    }
}