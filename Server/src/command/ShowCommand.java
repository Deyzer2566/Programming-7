package command;

import data.StudyGroup;
import io.Writer;
import storage.Database;

import java.util.Collection;

public class ShowCommand implements Command {

    Database db;
    Writer writer;

    public ShowCommand(Database db, Writer writer) {
        this.db = db;
        this.writer = writer;
    }

    @Override
    public void execute(String[] args) throws InvalidCommandArgumentException {
        Collection<StudyGroup> groups = db.showAllGroups();
        writer.writeObject(groups);
    }

    @Override
    public String description() {
        return "вывести все элементы коллекции в строковом представлении";
    }
}
