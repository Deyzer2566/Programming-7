package command;

public class ThereIsUserWithThisLogin extends Exception{
    public ThereIsUserWithThisLogin() {
        super();
    }

    public ThereIsUserWithThisLogin(String message) {
        super(message);
    }
}
