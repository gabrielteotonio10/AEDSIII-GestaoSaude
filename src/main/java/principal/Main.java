package principal;

import java.util.List;
import java.util.Map;

import dao.*;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import model.*;

public class Main {
    public static void main(String[] args) {

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.staticFiles.add("/public", Location.CLASSPATH);
        });

        // ── USUÁRIOS ──────────────────────────────────────────────────────────
        app.post("/usuarios", ctx -> {
            try {
                Usuario u = ctx.bodyAsClass(Usuario.class);
                UsuarioDAO dao = new UsuarioDAO();
                if (dao.buscarPorEmail(u.getEmail()) != null) {
                    ctx.status(409).result("E-mail já cadastrado.");
                    return;
                }
                dao.incluir(u);
                ctx.status(201).result("Usuário criado!");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.post("/login", ctx -> {
            try {
                var cred = ctx.bodyAsClass(Map.class);
                String email = (String) cred.get("email");
                String senha = (String) cred.get("senha");
                UsuarioDAO dao = new UsuarioDAO();
                Usuario u = dao.buscarPorEmail(email);
                if (u == null || !u.getSenha().equals(senha)) {
                    ctx.status(401).result("E-mail ou senha incorretos.");
                    return;
                }
                u.setSenha("");
                ctx.json(u);
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/usuarios", ctx -> {
            try {
                UsuarioDAO dao = new UsuarioDAO();
                List<Usuario> lista = dao.listarTodos();
                lista.forEach(u -> u.setSenha(""));
                ctx.json(lista);
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/usuarios/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                UsuarioDAO dao = new UsuarioDAO();
                Usuario u = dao.buscar(id);
                if (u != null) {
                    u.setSenha("");
                    ctx.json(u);
                } else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.put("/usuarios/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Usuario u = ctx.bodyAsClass(Usuario.class);
                u.setId(id);
                if (u.getSenha() == null || u.getSenha().isEmpty()) {
                    Usuario atual = new UsuarioDAO().buscar(id);
                    if (atual != null)
                        u.setSenha(atual.getSenha());
                }
                boolean ok = new UsuarioDAO().alterar(u);
                if (ok)
                    ctx.status(200).result("Atualizado!");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.delete("/usuarios/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                boolean ok = new UsuarioDAO().excluir(id);
                if (ok)
                    ctx.status(200).result("Excluído.");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        // ── PACIENTES ─────────────────────────────────────────────────────────
        app.post("/pacientes", ctx -> {
            try {
                Paciente p = ctx.bodyAsClass(Paciente.class);
                int id = new PacienteDAO().incluir(p);
                ctx.status(201).result("Paciente criado com ID " + id);
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/pacientes", ctx -> {
            try {
                ctx.json(new PacienteDAO().listarTodos());
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/pacientes/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Paciente p = new PacienteDAO().buscar(id);
                if (p != null)
                    ctx.json(p);
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.put("/pacientes/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Paciente p = ctx.bodyAsClass(Paciente.class);
                p.setId(id);
                boolean ok = new PacienteDAO().alterar(p);
                if (ok)
                    ctx.status(200).result("Atualizado!");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.delete("/pacientes/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                ConsultaDAO consultaDAO = new ConsultaDAO();
                if (consultaDAO.pacientePossuiConsultas(id)) {
                    ctx.status(409).result("Paciente possui consultas vinculadas.");
                    return;
                }
                boolean ok = new PacienteDAO().excluir(id);
                if (ok)
                    ctx.status(200).result("Excluído.");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        // ── PROCEDIMENTOS ─────────────────────────────────────────────────────
        app.post("/procedimentos", ctx -> {
            try {
                Procedimento p = ctx.bodyAsClass(Procedimento.class);
                int id = new ProcedimentoDAO().incluir(p);
                ctx.status(201).result("Procedimento criado com ID " + id);
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/procedimentos", ctx -> {
            try {
                ctx.json(new ProcedimentoDAO().listarTodos());
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/procedimentos/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Procedimento p = new ProcedimentoDAO().buscar(id);
                if (p != null)
                    ctx.json(p);
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.put("/procedimentos/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Procedimento p = ctx.bodyAsClass(Procedimento.class);
                p.setId(id);
                boolean ok = new ProcedimentoDAO().alterar(p);
                if (ok)
                    ctx.status(200).result("Atualizado!");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.delete("/procedimentos/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                boolean ok = new ProcedimentoDAO().excluir(id);
                if (ok)
                    ctx.status(200).result("Excluído.");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        // ── CONSULTAS ─────────────────────────────────────────────────────────
        // IMPORTANTE: rota específica /paciente/{id} ANTES da genérica /{id}
        app.post("/consultas", ctx -> {
            try {
                Consulta c = ctx.bodyAsClass(Consulta.class);
                if (new PacienteDAO().buscar(c.getIdPaciente()) == null) {
                    ctx.status(404).result("Paciente não encontrado.");
                    return;
                }
                if (new UsuarioDAO().buscar(c.getIdUsuario()) == null) {
                    ctx.status(404).result("Usuário não encontrado.");
                    return;
                }
                int id = new ConsultaDAO().incluir(c);
                ctx.status(201).result("Consulta criada com ID " + id);
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/consultas", ctx -> {
            try {
                ctx.json(new ConsultaDAO().listarTodos());
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        // específica ANTES da genérica
        app.get("/consultas/paciente/{idPaciente}", ctx -> {
            try {
                int idPaciente = Integer.parseInt(ctx.pathParam("idPaciente"));
                ctx.json(new ConsultaDAO().listarPorPaciente(idPaciente));
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/consultas/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Consulta c = new ConsultaDAO().buscar(id);
                if (c != null)
                    ctx.json(c);
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.put("/consultas/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Consulta c = ctx.bodyAsClass(Consulta.class);
                c.setId(id);
                if (new PacienteDAO().buscar(c.getIdPaciente()) == null) {
                    ctx.status(404).result("Paciente não encontrado.");
                    return;
                }
                if (new UsuarioDAO().buscar(c.getIdUsuario()) == null) {
                    ctx.status(404).result("Usuário não encontrado.");
                    return;
                }
                boolean ok = new ConsultaDAO().alterar(c);
                if (ok)
                    ctx.status(200).result("Atualizado!");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.delete("/consultas/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                boolean ok = new ConsultaDAO().excluir(id);
                if (ok) {
                    new ConsultaProcedimentoDAO().excluirPorConsulta(id);
                    ctx.status(200).result("Excluído.");
                } else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        // ── CONSULTA_PROCEDIMENTOS ────────────────────────────────────────────
        // IMPORTANTE: rota específica /consulta/{id} ANTES da genérica /{id}
        app.post("/consulta_procedimentos", ctx -> {
            try {
                ConsultaProcedimento cp = ctx.bodyAsClass(ConsultaProcedimento.class);
                int id = new ConsultaProcedimentoDAO().incluir(cp);
                ctx.status(201).result("Vínculo criado com ID " + id);
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/consulta_procedimentos", ctx -> {
            try {
                ctx.json(new ConsultaProcedimentoDAO().listarTodos());
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        // específica ANTES da genérica
        app.get("/consulta_procedimentos/consulta/{idConsulta}", ctx -> {
            try {
                int idConsulta = Integer.parseInt(ctx.pathParam("idConsulta"));
                ctx.json(new ConsultaProcedimentoDAO().listarPorConsulta(idConsulta));
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.get("/consulta_procedimentos/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                ConsultaProcedimento cp = new ConsultaProcedimentoDAO().buscar(id);
                if (cp != null)
                    ctx.json(cp);
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.put("/consulta_procedimentos/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                ConsultaProcedimento cp = ctx.bodyAsClass(ConsultaProcedimento.class);
                cp.setId(id);
                boolean ok = new ConsultaProcedimentoDAO().alterar(cp);
                if (ok)
                    ctx.status(200).result("Atualizado!");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.delete("/consulta_procedimentos/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                boolean ok = new ConsultaProcedimentoDAO().excluir(id);
                if (ok)
                    ctx.status(200).result("Excluído.");
                else
                    ctx.status(404).result("Não encontrado.");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        app.start(8080);
        System.out.println("Servidor iniciado! Acesse: http://localhost:8080");
    }
}
