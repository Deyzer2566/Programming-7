package command;

public class NotCorrectLoginOrPassword extends Exception{
    public NotCorrectLoginOrPassword() {
    }

    public NotCorrectLoginOrPassword(String message) {
        super(message);
    }
}
