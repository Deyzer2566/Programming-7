package SQL;

import data.StudyGroup;
import storage.GroupDidNotFound;

import java.util.Collection;

public class NotAuthorizedSQLDatabase extends SQLUserDatabase {

    public NotAuthorizedSQLDatabase(int clientId, SQLDatabase dbSource) {
        super(clientId, dbSource);
    }

    @Override
    public void add(StudyGroup newGroup) {

    }

    @Override
    public void remove(long id) throws GroupDidNotFound {
        throw new GroupDidNotFound("Авторизируйтесь для получения доступа к коллекции");
    }

    @Override
    public void clear() {

    }

    @Override
    public StudyGroup removeHead() throws GroupDidNotFound {
        throw new GroupDidNotFound("Авторизируйтесь для получения доступа к коллекции");
    }

    @Override
    public void update(long id, StudyGroup group) throws GroupDidNotFound {
        throw new GroupDidNotFound("Авторизируйтесь для получения доступа к коллекции");
    }

    @Override
    public void addIfMax(StudyGroup group) {

    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public StudyGroup getMaxByStudentsCountGroup() {
        return null;
    }

    @Override
    public Collection<Long> getExpelledStudentsCount() {
        return null;
    }

    @Override
    public Collection<String> getUniqueNamesGroupsAdmins() {
        return null;
    }

    @Override
    public String showAllGroups() {
        return null;
    }
}
