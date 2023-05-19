package Client;

import SQL.SQLDatabase;
import command.CommandHandler;
import command.InvalidCommandArgumentException;
import command.ThereIsNotCommand;
import io.IOHandler;

import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler{

    org.slf4j.Logger log;
    SQLDatabase globalDB;

    LinkedList<Client> clients;

    ExecutorService queriesReader;

    ExecutorService executeThread;

    public ClientHandler(SQLDatabase globalDB) {
        this.globalDB = globalDB;
        log = org.slf4j.LoggerFactory.getLogger("client handler");
        clients = new LinkedList<Client>();
        queriesReader = Executors.newCachedThreadPool();
        executeThread = Executors.newFixedThreadPool(3);
    }
    private void executeWithCheckingLogin (Client client, SQLDatabase database){
        int id = readUserId(client);
        if(id == -2)
            return;
        executeWithFixedThreadPool(client, new CommandHandler(database.getUserDatabaseById(id),client));
    }
    private int readUserId(Client client) {
        if(!client.hasNext())
            return -2;
        int id = -1;
        try {
            String login = client.readLine();
            String password = client.read();
            id = globalDB.getIdByLogin(login, password);
            //if(id == -1)
            //    id = createIdByLogin(login,password);
        } catch (ArrayIndexOutOfBoundsException e) {}
        return id;
    }
    private LinkedList<String> readQuery(IOHandler client){
        String command = null;
        if(!client.hasNext())
            return null;
        try {
            command = client.readLine();
        } catch (NoSuchElementException e) {
            return null;
        }
        if (command == null)
            return null;
        if (command.equals("")) {
            return null;
        }
        LinkedList<String> commandArgs = new LinkedList<>(Arrays.asList(command.split(" ")));
        return commandArgs;
    }
    private void execute(IOHandler client, CommandHandler ch){
        LinkedList<String> commandArgs = readQuery(client);
        if(commandArgs == null)
            return;
        String command = commandArgs.get(0);
        commandArgs.remove(0);
        log.info("Пришла команда "+command);
        try {
            ch.execute(command, commandArgs.size() == 0 ? null : commandArgs.toArray(new String[commandArgs.size()]));
        } catch (ThereIsNotCommand | InvalidCommandArgumentException e) {
            client.writeError(e.getMessage());
        }
    }

    private void executeWithFixedThreadPool(IOHandler client, CommandHandler ch){
        LinkedList<String> commandArgs = readQuery(client);
        if(commandArgs == null)
            return;
        String command = commandArgs.get(0);
        commandArgs.remove(0);
        log.info("Пришла команда "+command);
        executeThread.execute(()-> {
            try {
                ch.execute(command, commandArgs.size() == 0 ? null : commandArgs.toArray(new String[commandArgs.size()]));
            } catch (ThereIsNotCommand | InvalidCommandArgumentException e) {
                client.writeError(e.getMessage());
            }
        });
    }

    public void addClient(Client client){
        clients.add(client);
        queriesReader.execute(() -> {
            while(client.isConnected()){
                executeWithCheckingLogin(client,globalDB);
            }
            clients.remove(client);
        });
        log.info("Новое подключение");
    }

    public void close(){
        executeThread.shutdown();
        queriesReader.shutdown();
        clients.forEach(t->t.disconnect());
        clients.clear();
    }
}
