package command;

import net.RemoteDatabaseWithAuth;

public class LoginCommand implements Command{

    RemoteDatabaseWithAuth db;

    public LoginCommand(RemoteDatabaseWithAuth db){
        this.db = db;
    }

    @Override
    public void execute(String[] args) throws InvalidCommandArgumentException {
        String login = null;
        String password = null;
        try {
            login = args[0];
            password = args[1];
        } catch (ArrayIndexOutOfBoundsException e){
            throw new InvalidCommandArgumentException("Недостаточно аргументов!");
        }
        db.changeLoginAndPassword(login,password);
    }

    @Override
    public String description() {
        return "авторизация пользователя";
    }
}
