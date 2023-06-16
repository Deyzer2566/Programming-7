package command;

import io.IOHandler;
import io.Writer;
import net.RemoteDatabaseWithAuth;

public class RegisterCommand implements Command{

    RemoteDatabaseWithAuth db;
    Writer writer;

    public RegisterCommand(RemoteDatabaseWithAuth db,Writer writer){
        this.db = db;
        this.writer=writer;
    }

    @Override
    public void execute(String[] args) throws InvalidCommandArgumentException {
        try {
            if (args.length == 2)
                db.register(args[0], args[1]);
            else if (args.length == 1)
                db.register(args[0], " ");
        } catch (ThereIsUserWithThisLogin e){
            writer.writeError(e.getMessage());
        }
        if(args.length == 0)
            throw new InvalidCommandArgumentException("Не удалось прочитать логин и пароль");
    }

    @Override
    public String description() {
        return "команда для регистрации";
    }
}
