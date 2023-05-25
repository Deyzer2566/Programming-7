package Client;

import data.StudyGroup;

import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;

public class ClientWithMultiThreading extends Client{

    public ClientWithMultiThreading(Socket main, Socket err) throws IOException {
        super(main, err);
    }

    @Override
    public void writeObject(Object obj) {
        new Thread(()-> super.writeObject(obj)).start();
    }

    @Override
    public synchronized StudyGroup readStudyGroup() {
        return super.readStudyGroup();
    }

    @Override
    protected synchronized Object readObject() throws IOException {
        return super.readObject();
    }

    @Override
    public synchronized String read() throws NoSuchElementException {
        return super.read();
    }
}
