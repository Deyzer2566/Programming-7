package Client;

import java.io.IOException;
import java.net.Socket;

public class ClientWithMultiThreadingSend extends Client{

    public ClientWithMultiThreadingSend(Socket main, Socket err) throws IOException {
        super(main, err);
    }

    @Override
    public void writeObject(Object obj) {
        new Thread(()-> super.writeObject(obj)).start();
    }
}
