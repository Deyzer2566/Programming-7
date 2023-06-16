package command;
import data.StudyGroup;
import storage.Database;
import io.Writer;

import java.util.Collection;

/**
 * Команда вывода всех групп базы
 */
public class ShowCommand implements Command{
    private Database db;
    private Writer writer;

    public ShowCommand(Database db, Writer writer){
        this.db = db;
        this.writer = writer;
    }

    /**
     * выводит все элементы базы
     */
    private void show(){
        Collection<StudyGroup> groups = db.showAllGroups();
        StringBuilder str = new StringBuilder();
        if(groups != null) {
            groups.forEach(x -> str.append(x.toString()).append("\n"));
            writer.write(str.toString());
        } else {
            writer.writeln("null");
        }
    }

    @Override
    public void execute(String [] args){
        show();
    }

    @Override
    public String description() {
        return "вывести все элементы коллекции в строковом представлении";
    }
}