package Client;

import SQL.SQLDatabase;
import command.CommandHandler;
import command.InvalidCommandArgumentException;
import command.ThereIsNotCommand;
import io.IOHandler;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
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

    HashMap<IOHandler, Boolean> executeClients;
    public ClientHandler(SQLDatabase globalDB) {
        this.globalDB = globalDB;
        log = org.slf4j.LoggerFactory.getLogger("client handler");
        clients = new LinkedList<Client>();
        queriesReader = Executors.newCachedThreadPool();
        executeThread = Executors.newFixedThreadPool(3);
        executeClients = new HashMap<>();
    }
    private void executeWithCheckingLogin (Client client, SQLDatabase database) throws ThereIsNoClient{
        int id = readUserId(client);
        if(id == -2)
            return;
        executeThread.execute(()->
        {
            executeClients.put(client,true);
            execute(client, new CommandHandler(database.getUserDatabaseById(id),client));
            executeClients.put(client,false);
        });
        //executeWithFixedThreadPool(client, new CommandHandler(database.getUserDatabaseById(id),client));
    }
    private int readUserId(Client client) throws ThereIsNoClient {
        if (!client.hasNext())
            return -2;
        int id = -1;
        try {
            String login = client.readLine();
            String password = client.read();
            id = globalDB.getIdByLogin(login, password);
            //if(id == -1)
            //    id = createIdByLogin(login,password);
        } catch (NoSuchElementException e) {
            throw new ThereIsNoClient("Клиент отключился!");
        }
        return id;
    }
    private LinkedList<String> readQuery(IOHandler client){
        String command = null;
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
        executeClients.put(client,true);
        executeThread.execute(()-> {
            try {
                ch.execute(command, commandArgs.size() == 0 ? null : commandArgs.toArray(new String[commandArgs.size()]));
            } catch (ThereIsNotCommand | InvalidCommandArgumentException e) {
                client.writeError(e.getMessage());
            }
        });
        executeClients.put(client,false);
    }

    public void addClient(Client client){
        clients.add(client);
        queriesReader.execute(() -> {
            executeClients.put(client,false);
            while(client.isConnected()){
                try{
                    if(!executeClients.get(client))
                        executeWithCheckingLogin(client,globalDB);
                } catch (ThereIsNoClient e) {
                    client.disconnect();
                    break;
                }
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            clients.remove(client);
            executeClients.remove(client);
            log.info("Клиент отключился");
        });
        log.info("Новое подключение");
    }

    public void close(){
        clients.forEach(t->t.disconnect());
        executeThread.shutdown();
        queriesReader.shutdown();
        clients.clear();
    }
}
