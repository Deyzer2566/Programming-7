package SQL;

public class IncorrectPasswordErrorException extends Exception{
    public IncorrectPasswordErrorException() {
        super();
    }

    public IncorrectPasswordErrorException(String message) {
        super(message);
    }
}
