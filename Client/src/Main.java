import command.*;
import io.*;
import net.RemoteDatabaseWithAuth;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Main {
    public static void main(String [] args) {
        RemoteDatabaseWithAuth db = null;
        try {
            db = new RemoteDatabaseWithAuth("127.0.0.1", 33737);
        } catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
        ConsoleIO console = new ConsoleIO();
        CommandHandler ch = new CommandHandler(db, console);
        ch.register("login", new LoginCommand(db));
        while(db.isConnected()) {
            console.write(">");
            String command = null;
            try{
                command = console.readLine();
            } catch (NoSuchElementException e){
                break;
            }
            if(command == null)
                break;
            if(command.equals("exit")){
                break;
            }
            if(command.equals("")){
                console.write(">");
                continue;
            }
            LinkedList<String> commandArgs = new LinkedList<>(Arrays.asList(command.split(" ")));
            command = commandArgs.get(0);
            commandArgs.remove(0);
            try{
                ch.execute(command,commandArgs.size()==0?null:commandArgs.toArray(new String[commandArgs.size()]));
            } catch(ThereIsNotCommand | InvalidCommandArgumentException e){
                console.writeError(e.getMessage());
            }
        }
        db.disconnect();
    }
}
