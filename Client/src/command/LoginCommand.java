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
        String password = "";
        try {
            login = args[0];
        } catch (ArrayIndexOutOfBoundsException e){
            throw new InvalidCommandArgumentException("Недостаточно аргументов!");
        }
        try{
            password = args[1];
        } catch (ArrayIndexOutOfBoundsException e){
        }
        try {
            db.login(login, password);
        }catch (NotCorrectLoginOrPassword e){
            throw new InvalidCommandArgumentException(e.getMessage());
        }
    }

    @Override
    public String description() {
        return "авторизация пользователя";
    }
}
