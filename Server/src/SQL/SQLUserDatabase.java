package SQL;

import data.StudyGroup;
import storage.Database;
import storage.GroupDidNotFound;

import java.util.Collection;

public class SQLUserDatabase extends Database {
    private int clientId;
    private SQLDatabase dbSource;
    public SQLUserDatabase(int clientId,SQLDatabase dbSource){
        this.clientId = clientId;
        this.dbSource=dbSource;
    }

    @Override
    public void add(StudyGroup newGroup) {
        dbSource.add(newGroup,this.clientId);
    }


    @Override
    public void remove(long id) throws GroupDidNotFound {
        dbSource.remove(id,this.clientId);
    }

    @Override
    public void clear() {
        dbSource.clear(this.clientId);
    }

    @Override
    public StudyGroup removeHead() throws GroupDidNotFound {
        return dbSource.removeHead(this.clientId);
    }

    @Override
    public void update(long id, StudyGroup group) throws GroupDidNotFound {
        dbSource.update(id,group,this.clientId);
    }

    @Override
    public void addIfMax(StudyGroup group) {
        dbSource.addIfMax(group,this.clientId);
    }

    @Override
    public String getInfo() {
        return dbSource.getInfo();
    }

    @Override
    public StudyGroup getMaxByStudentsCountGroup() {
        return dbSource.getMaxByStudentsCountGroup();
    }

    @Override
    public Collection<Long> getExpelledStudentsCount() {
        return dbSource.getExpelledStudentsCount();
    }

    @Override
    public Collection<String> getUniqueNamesGroupsAdmins() {
        return dbSource.getUniqueNamesGroupsAdmins();
    }

    @Override
    public Collection<StudyGroup> showAllGroups() {
        return dbSource.showAllGroups();
    }
}
