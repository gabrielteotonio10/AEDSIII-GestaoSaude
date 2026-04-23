package dao;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import model.Registro;

public class Arquivo<T extends Registro> {
    private static final int TAM_CABECALHO = 12; // Primeiro pedaço do arquivo tem 4 bytes
    private RandomAccessFile arquivo; // Objeto que manipula os bytes
    private String nomeArquivo;
    private Constructor<T> construtor; // Guarda a forma de como criar um objeto T a partir de bytes

    // Recebe o nome do arquivo e o construtor da classe T
    public Arquivo(String nomeArquivo, Constructor<T> construtor) throws Exception {
        File diretorio = new File("./data");
        if (!diretorio.exists()) // Se não existir cria pasta
            diretorio.mkdir();
        diretorio = new File("./data/" + nomeArquivo);
        if (!diretorio.exists()) // Se não existir cria aarquivo
            diretorio.mkdir();
        this.nomeArquivo = "./data/" + nomeArquivo + "/" + nomeArquivo + ".db"; // Caminho do arquivo
        this.construtor = construtor;
        this.arquivo = new RandomAccessFile(this.nomeArquivo, "rw"); // Abre o arquivo para leitura e escrita (rw)
        // Se o arquivo for novo, escreve o cabeçalho
        if (arquivo.length() < TAM_CABECALHO) {
            arquivo.writeInt(0); // Último ID usado
            arquivo.writeLong(-1); // Lista de registros excluídos
        }
    }

    // Create
    public int create(T obj) throws Exception {
        arquivo.seek(0); // Ponteiro para o ínicio
        int novoID = arquivo.readInt() + 1; // Lê o último ID usado, soma 1 e guarda como novo ID
        arquivo.seek(0);
        arquivo.writeInt(novoID); // Escreve o novo ID no início do arquivo para atualizar o último ID usado
        obj.setId(novoID);
        byte[] dados = obj.toByteArray(); // Transforma o paciente em bytes
        // Tenta reutilizar um espaço de um registro excluído, se não encontrar, escreve
        // no final do arquivo
        long endereco = getDeleted(dados.length);
        if (endereco == -1) { // Não excluido
            arquivo.seek(arquivo.length());
            endereco = arquivo.getFilePointer();
            arquivo.writeByte(' '); // Lápide
            arquivo.writeShort(dados.length); // Tamanho da fita de dados
            arquivo.write(dados); // Grava a fita de dados completa
        } else {
            arquivo.seek(endereco); // Vai para o endereço do registro excluído
            arquivo.writeByte(' '); // Remove a lápide (*)
            arquivo.writeShort(dados.length); // Atualiza o tamanho do registro
            arquivo.write(dados);
        }
        return obj.getId(); // Devolve o id usado
    }

    // Read
    public T read(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO); // Pula o id
        // Loop até o fim do arquivo
        while (arquivo.getFilePointer() < arquivo.length()) {
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados); // Preenche o array de bytes com os dados do registro
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) { // Se o id for o que procura retorna
                    return obj;
                }
            }
        }
        return null;
    }

    // Retorna uma lista com todos os registros ativos
    public java.util.List<T> readAll() throws Exception {
        java.util.List<T> lista = new java.util.ArrayList<>();
        // Move o ponteiro para o início dos dados (pula o cabeçalho)
        arquivo.seek(TAM_CABECALHO);
        // Percorre o arquivo até o final
        while (arquivo.getFilePointer() < arquivo.length()) {
            byte lapide = arquivo.readByte(); // Lê a lápide
            short tamanho = arquivo.readShort(); // Lê o tamanho do registro
            byte[] dados = new byte[tamanho];
            arquivo.read(dados); // Lê o corpo do registro
            // Se o registro não estiver excluído
            if (lapide == ' ') {
                T obj = construtor.newInstance(); // Cria uma nova instância de T
                obj.fromByteArray(dados); // Preenche o objeto com os bytes
                lista.add(obj); // Adiciona na lista
            }
        }
        return lista;
    }

    // Delete
    public boolean delete(int id) throws Exception {
        // Lógica parecida com read
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == id) { // Se encontrar o id, marca como excluído e adiciona o espaço na lista de
                                         // excluídos
                    arquivo.seek(posicao);
                    arquivo.writeByte('*');
                    addDeleted(tamanho, posicao); // Mostra que tem um buraco que pode ser reutilizado
                    return true;
                }
            }
        }
        return false;
    }

    // Update
    public boolean update(T novoObj) throws Exception {
        // Lógica parecida com read
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long posicao = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();
            byte[] dados = new byte[tamanho];
            arquivo.read(dados);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(dados);
                if (obj.getId() == novoObj.getId()) {
                    byte[] novosDados = novoObj.toByteArray(); // Transforma o atualizado em bytes
                    short novoTam = (short) novosDados.length;
                    if (novoTam <= tamanho) { // Se couber no espaço atual, escreve lá mesmo
                        arquivo.seek(posicao + 3); // Pula a lápide e o tamanho para escrever os dados
                        arquivo.write(novosDados);
                    } else {
                        // Volta para o começo, coloca lápide e mostra que desocupou o espaço
                        arquivo.seek(posicao);
                        arquivo.writeByte('*');
                        addDeleted(tamanho, posicao);
                        // Ve se tem um espaço de tamanho suficiente para o novo registro
                        long novoEndereco = getDeleted(novosDados.length);
                        if (novoEndereco == -1) { // Não tem
                            arquivo.seek(arquivo.length()); // Fim do arquivo
                            novoEndereco = arquivo.getFilePointer();
                            arquivo.writeByte(' ');
                            arquivo.writeShort(novoTam);
                            arquivo.write(novosDados);
                        } else {
                            arquivo.seek(novoEndereco);
                            arquivo.writeByte(' ');
                            arquivo.writeShort(novoTam);
                            arquivo.write(novosDados);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // Gerencia a lista de registros excluídos, guardando o endereço e o tamanho do
    // espaço para reutilizar
    private void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong(); // Lê o endereço do 1º buraco da lista
        long proximo;
        // 1- Se a lista estiver vazia, o novo buraco é o primeiro
        if (endereco == -1) {
            arquivo.seek(4); // Cabeçalho
            arquivo.writeLong(enderecoEspaco); // Cabeçalho aponta para este novo buraco
            arquivo.seek(enderecoEspaco + 3); // Vai para dentro do novo buraco (pulando lápide e tamanho)
            arquivo.writeLong(-1); // -1 é último da fila
        } else { // Se já existe burraco, percorre a lista até achar o lugar certo para inserir
            do {
                // Pula lápide, vê o tamanho e vê quem é o próximo
                arquivo.seek(endereco + 1);
                int tamanho = arquivo.readShort();
                proximo = arquivo.readLong();
                // Se achou um burraco maior que o novo, insere aqui (ordem crescente de
                // tamanho)
                if (tamanho > tamanhoEspaco) {
                    // Se for o primeiro da lista muda o cabeçalho, senão muda o ponteiro do buraco
                    // anterior
                    if (posicao == 4)
                        arquivo.seek(posicao);
                    else
                        arquivo.seek(posicao + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(endereco);
                    break;
                }
                // Se chegou no fim da lista, insere aqui (não achou ninguém maior)
                if (proximo == -1) {
                    arquivo.seek(endereco + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(-1);
                    break;
                }
                // Se não entrou nos ifs, continua andando na lista
                posicao = endereco;
                endereco = proximo;
            } while (endereco != -1);
        }
    }

    // Procura um registro excluído com espaço suficiente para armazenar um novo
    // registro
    private long getDeleted(int tamanhoNecessario) throws Exception {
        long posicao = 4;
        arquivo.seek(posicao);
        long endereco = arquivo.readLong(); // Pega o primeiro buraco da fila
        long proximo;
        int tamanho;
        // Enquanto houver buracos na lista
        while (endereco != -1) {
            // Pula a lápide, descobre o tamanho e ve o próximo da fila
            arquivo.seek(endereco + 1);
            tamanho = arquivo.readShort();
            proximo = arquivo.readLong();
            // Confere se o buraco é grande o suficiente para o novo registro
            if (tamanho >= tamanhoNecessario) {
                if (posicao == 4)
                    arquivo.seek(posicao);
                else
                    arquivo.seek(posicao + 3);
                arquivo.writeLong(proximo);
                return endereco;
            }
            // Se o buraco era muito pequeno, caminha para o próximo da lista
            posicao = endereco;
            endereco = proximo;
        }
        return -1;
    }

    public void close() throws Exception {
        arquivo.close(); // Fecha o arquivo quando não for mais necessário
    }
}