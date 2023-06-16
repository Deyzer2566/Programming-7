package Client;

import SQL.SQLDatabase;
import command.*;
import io.IOHandler;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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
    private void executeWithCheckingLogin (Client client) throws ThereIsNoClient {
        int id = readUserId(client);
        if(id == -2)
            return;
        synchronized (executeThread) {
            executeThread.execute(() ->
            {
                synchronized (globalDB) {
                    CommandHandler commandHandler = new CommandHandler(globalDB.getUserDatabaseById(id), client);
                    commandHandler.register("register", new RegisterCommand(globalDB, client));
                    commandHandler.register("login", new LoginCommand(globalDB, client));
                    synchronized (client) {
                        execute(client, commandHandler);
                    }
                }
            });
        }
    }
    private synchronized int readUserId(Client client) throws ThereIsNoClient {
        if (!client.hasNext())
            return -2;
        int id = -1;
        try {
            String login = client.readLine();
            String password = client.read();
            id = globalDB.getIdByLogin(login, password);
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

    public void addClient(Client client){
        clients.add(client);
        queriesReader.execute(() -> {
            while(true){
                synchronized (client) {
                    if(!client.isConnected())
                        break;
                    try {
                        executeWithCheckingLogin(client);
                    } catch (ThereIsNoClient e) {
                        client.disconnect();
                        break;
                    }
                }
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            clients.remove(client);
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
