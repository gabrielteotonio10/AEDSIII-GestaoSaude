package dao;

import model.Usuario;
import java.lang.reflect.Constructor;
import java.util.List;

public class UsuarioDAO {
    private Arquivo<Usuario> arquivo;

    public UsuarioDAO() throws Exception {
        Constructor<Usuario> construtor = Usuario.class.getConstructor();
        this.arquivo = new Arquivo<>("usuarios", construtor);
    }

    public int incluir(Usuario u) throws Exception {
        return arquivo.create(u);
    }

    public Usuario buscar(int id) throws Exception {
        return arquivo.read(id);
    }

    public List<Usuario> listarTodos() throws Exception {
        return arquivo.readAll();
    }

    public boolean alterar(Usuario u) throws Exception {
        return arquivo.update(u);
    }

    public boolean excluir(int id) throws Exception {
        return arquivo.delete(id);
    }

    // Busca por e-mail (para login)
    public Usuario buscarPorEmail(String email) throws Exception {
        List<Usuario> todos = arquivo.readAll();
        for (Usuario u : todos) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }
}