package SQL;

import io.FileReader;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class PGPassReader {
    public static List<String> getLoginAndPassword(String path) throws FileNotFoundException {
        FileReader reader = new FileReader(path);
        String line = reader.readLine();
        String[] subStrings = line.split(":");
        reader.close();
        return Arrays.stream(subStrings).filter(i -> i.length() > 2).toList();
    }
}
