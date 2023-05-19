package Client;

import data.StudyGroup;
import io.IOHandler;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.NoSuchElementException;

public class Client extends IOHandler {

    public Socket main;
    private Socket err;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ObjectOutputStream errOut;

    private boolean isConnected = false;

    public Client(Socket main, Socket err) throws IOException {
        this.main = main;
        this.err = err;
        //this.out = new ObjectOutputStream(main.getOutputStream());
        //out.flush();
        //this.errOut = new ObjectOutputStream(err.getOutputStream());
        //errOut.flush();
        //this.in = new ObjectInputStream(kostyl);
        isConnected = true;
    }

    private Object readObject() throws IOException{
        try {
            this.in = new ObjectInputStream(main.getInputStream());
            return in.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            disconnect();
            return null;
        }
    }

    @Override
    public String read() throws NoSuchElementException {
        try {
            return (String)(in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            disconnect();
            throw new NoSuchElementException("Не удалось прочитать строку!");
        }
    }

    @Override
    public String readLine() throws NoSuchElementException {
        try {
            return (String)(readObject());
        } catch (IOException e) {
            disconnect();
            throw new NoSuchElementException("Не удалось прочитать строку!");
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return main.getInputStream().available()>0;
        } catch (IOException e) {
            disconnect();
            return false;
        }
    }

    @Override
    public boolean hasNextLine() {
        return hasNext();
    }

    @Override
    public void writeObject(Object obj) {
        org.slf4j.LoggerFactory.getLogger("client handler").info("Отправка ответа");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(baos);
            o.writeObject(obj);
            o.flush();
            o.close();

            out = new ObjectOutputStream(main.getOutputStream());
            out.writeInt(baos.size());
            out.flush();
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            disconnect();
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void write(String str) {
        writeObject(str);
    }

    @Override
    public void writeln(String str) {
        writeObject(str);
    }

    @Override
    public void writeError(String str) {
        try{
            errOut = new ObjectOutputStream(err.getOutputStream());
            errOut.writeObject(str);
        } catch (IOException e) {
            disconnect();
        }
    }

    @Override
    public StudyGroup readStudyGroup() {
        try {
            //this.in = new ObjectInputStream(main.getInputStream());
            return (StudyGroup)in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            disconnect();
            return null;
        }
    }

    public boolean isConnected(){
        return isConnected;
    }

    public void disconnect(){
        try {
            isConnected=false;
            main.close();
            err.close();
        } catch (IOException e) {

        }
    }
}
