package command;

import SQL.SQLDatabase;
import SQL.ThereIsUserWithThisLogin;
import Client.Client;

public class RegisterCommand implements Command{
    SQLDatabase db;
    Client client;

    public RegisterCommand(SQLDatabase db, Client client) {
        this.db = db;
        this.client = client;
    }

    @Override
    public void execute(String[] args) throws InvalidCommandArgumentException {
        try{
            if(args.length == 2)
                db.createIdByLogin(args[0],args[1]);
            if(args.length== 1)
                db.createIdByLogin(args[0],"");
        } catch (ThereIsUserWithThisLogin e){
            client.writeError(e.getMessage());
        }
    }

    @Override
    public String description() {
        return "команда для регистрации";
    }
}
