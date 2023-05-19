import Client.*;
import SQL.SQLDatabase;
import SQL.SQLUserDatabase;
import command.*;
import io.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;


public class Main {
    static org.slf4j.Logger log;

    public static void main(String [] args) throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(25565));
        ServerSocket serverSocket = serverSocketChannel.socket();
        SQLDatabase db;
        try {
            db = new SQLDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ConsoleIO console = new ConsoleIO();
        SQLUserDatabase userDatabase = db.getUserDatabaseById(0);
        CommandHandler ch = new CommandHandler(userDatabase,console);
        ClientHandler clients = new ClientHandler(db);
        console.write(">");
        log = org.slf4j.LoggerFactory.getLogger("main");
        log.info("Начало работы");
        while(true){
            if(System.in.available()>0)
            {
                String command = null;
                try {
                    command = console.readLine();
                } catch (NoSuchElementException e) {
                    break;
                }
                if (command == null)
                    break;
                if (command.equals("exit")) {
                    break;
                }
                if (command.equals("")) {
                    break;
                }
                LinkedList<String> commandArgs = new LinkedList<>(Arrays.asList(command.split(" ")));
                command = commandArgs.get(0);
                commandArgs.remove(0);
                try {
                    ch.execute(command, commandArgs.size() == 0 ? null : commandArgs.toArray(new String[commandArgs.size()]));
                } catch (ThereIsNotCommand | InvalidCommandArgumentException e) {
                    console.writeError(e.getMessage());
                }
                console.write(">");
            }
            SocketChannel socketChannel = serverSocketChannel.accept();
            if(socketChannel != null){
                SocketChannel errChannel = null;
                while(errChannel == null)
                    errChannel = serverSocketChannel.accept();
                Client client = new ClientWithMultiThreadingSend(socketChannel.socket(),errChannel.socket());
                clients.addClient(client);
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        clients.close();
        serverSocket.close();
        log.info("Конец работы");
    }
}
