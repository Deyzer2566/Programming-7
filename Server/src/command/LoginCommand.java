package command;

import Client.Client;
import SQL.SQLDatabase;

public class LoginCommand implements Command{

    SQLDatabase db;
    Client client;

    public LoginCommand(SQLDatabase db, Client client) {
        this.db = db;
        this.client = client;
    }

    @Override
    public void execute(String[] args) throws InvalidCommandArgumentException {
        if (args.length == 2) {
            if (db.getIdByLogin(args[0], args[1]) == -1)
                throw new InvalidCommandArgumentException("Неверный логин или пароль");
            return;
        }
        if (args.length == 1) {
            if (db.getIdByLogin(args[0], "") == -1)
                throw new InvalidCommandArgumentException("Неверный логин или пароль");
            return;
        }
        if(args.length == 0){
            throw new InvalidCommandArgumentException("Недостаточно аргументов!");
        }
    }

    @Override
    public String description() {
        return "авторизация пользователя";
    }
}
