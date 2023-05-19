package SQL;

import data.*;
import storage.GroupDidNotFound;
import storage.LocalDatabase;

import java.io.FileNotFoundException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class SQLDatabase extends LocalDatabase {
    private Connection connection;
    private HashMap<Integer,LinkedList<Long>> clientsGroups;
    private HashMap<Integer,SQLUserDatabase> userDatabases;
    public SQLDatabase() throws SQLException {
        super();
        List<String> loginAndPassword = null;
        try {
            loginAndPassword = PGPassReader.getLoginAndPassword(".pgpass");
        } catch (FileNotFoundException e) {
            throw new SQLException("Не удалось прочитать логин и пароль");
        }
        this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/studs",
			//можно сделать чтение pgpass
                loginAndPassword.get(0),loginAndPassword.get(1));
        clientsGroups = new HashMap<>();
        userDatabases = new HashMap<>();
        userDatabases.put(-1,new NotAuthorizedSQLDatabase(-1,this));
        synchronize();
    }

    public int getIdByLogin(String login, String password){
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT id FROM users WHERE login=? AND password =?");
            pstm.setString(1,login);
            pstm.setString(2,password);
            pstm.executeQuery();
            ResultSet set = pstm.getResultSet();
            int id = -1;
            if(set.next()){
                id = set.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }


    public int createIdByLogin(String login, String password){
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT id FROM users WHERE login=?");
            pstm.setString(1,login);
            pstm.executeQuery();
            ResultSet set = pstm.getResultSet();
            int id = -1;
            if(set.next()){
                id = set.getInt(1);
            }
            if(id == -1){
                pstm = connection.prepareStatement("INSERT INTO users(login,password) VALUES(?,?)");
                pstm.setString(1,login);
                pstm.setString(2,password);
                pstm.execute();
                id = getIdByLogin(login,password);
            }
            return id;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    @Override
    public void add(StudyGroup newGroup) {
        this.add(newGroup,0);
    }

    public synchronized void add(StudyGroup newGroup, int clientId) {
        PreparedStatement pstm = null;
        try {
            pstm = connection.prepareStatement(
                    "INSERT INTO person (name,weight,eyeColor,hairColor, nationality) "+
                        "VALUES (?, ?, ?::color, ?::color, ?::country)");
            Person admin = newGroup.getGroupAdmin();
            int adminId = -1;
            if(admin != null) {
                pstm.setString(1, admin.getName());
                pstm.setInt(2, admin.getWeight());
                pstm.setString(3, admin.getEyeColor().name());
                if (admin.getHairColor() != null)
                    pstm.setString(4, admin.getHairColor().name());
                else
                    pstm.setNull(4, Types.NULL);
                pstm.setString(5, admin.getNationality().name());
                pstm.executeUpdate();

                pstm = connection.prepareStatement("SELECT last_value FROM person_id_seq");
                pstm.executeQuery();
                ResultSet set = pstm.getResultSet();
                if (set.next())
                    adminId = set.getInt(1);
            }

            pstm = connection.prepareStatement(
                    "INSERT INTO coordinates (x,y) "+
                            "VALUES (?, ?)");
            pstm.setFloat(1,newGroup.getCoordinates().getX());
            pstm.setDouble(2,newGroup.getCoordinates().getY());
            pstm.executeUpdate();

            pstm = connection.prepareStatement("SELECT last_value FROM coordinates_id_seq");
            pstm.executeQuery();
            ResultSet set = pstm.getResultSet();
            int coordId = -1;
            if(set.next())
                coordId = set.getInt(1);

            pstm = connection.prepareStatement(
                    "INSERT INTO studygroups(name,coords,creationTime, studentCount,expelledStudents,shouldBeExpelled,sem,admin,whoCreated) "
                    +"VALUES (?,?,?,?,?,?,?::semester,?,?)");
            pstm.setString(1,newGroup.getName());
            pstm.setInt(2,coordId);
            Timestamp ts = new Timestamp(newGroup.getCreationDate().toInstant().toEpochMilli());
            pstm.setTimestamp(
                    3,
                    ts,
                    Calendar.getInstance(TimeZone.getTimeZone(newGroup.getCreationDate().getZone()))
            );
            if(newGroup.getStudentsCount() != null)
                pstm.setLong(4,newGroup.getStudentsCount().longValue());
            else
                pstm.setNull(5, Types.NULL);
            if(newGroup.getExpelledStudents() != null)
                pstm.setLong(5,newGroup.getExpelledStudents().longValue());
            else
                pstm.setNull(5, Types.NULL);
            pstm.setInt(6,newGroup.getShouldBeExpelled().intValue());
            if(newGroup.getSemesterEnum() != null)
                pstm.setString(7,newGroup.getSemesterEnum().name());
            else
                pstm.setNull(7, Types.NULL);
            if(adminId != -1)
                pstm.setInt(8,adminId);
            else
                pstm.setNull(8, Types.NULL);
            pstm.setInt(9,clientId);

            pstm.executeUpdate();


            pstm = connection.prepareStatement("SELECT last_value FROM studygroups_id_seq");
            pstm.executeQuery();
            set = pstm.getResultSet();
            long nid = -1;
            if(set.next())
                nid = set.getLong(1);
            addGroupToClient(clientId,nid);
            newGroup = new StudyGroup(nid, newGroup.getName(), newGroup.getCoordinates(),newGroup.getCreationDate(),
                    newGroup.getStudentsCount(),newGroup.getExpelledStudents(),newGroup.getShouldBeExpelled(),
                    newGroup.getSemesterEnum(),newGroup.getGroupAdmin());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }
        super.put(newGroup);
    }

    public SQLUserDatabase getUserDatabaseById(int id){
        if(userDatabases.get(id) == null)
            userDatabases.put(id,new SQLUserDatabase(id,this));
        return userDatabases.get(id);
    }
    public synchronized void remove(long id, int clientId) throws GroupDidNotFound {
        PreparedStatement pstm = null;
        try {
            pstm = connection.prepareStatement(
                    "SELECT coords,admin FROM studygroups");
            int coordId = -1;
            int adminId = -1;
            pstm.executeQuery();
            ResultSet set = pstm.getResultSet();
            if(set.next()) {
                coordId = set.getInt(1);
                adminId = set.getInt(2);
            }

            if(!clientsGroups.get(clientId).contains(id))
                throw new GroupDidNotFound("Доступ к группе запрещен: нет прав на удаление группы");

            pstm = connection.prepareStatement(
                    "DELETE FROM studygroups WHERE id = ?");
            pstm.setLong(1,id);
            pstm.execute();

            pstm = connection.prepareStatement(
                    "DELETE FROM coordinates WHERE id = ?");
            pstm.setLong(1,id);
            pstm.execute();

            pstm = connection.prepareStatement(
                    "DELETE FROM person WHERE id = ?");
            pstm.setLong(1,id);
            pstm.execute();

            clientsGroups.get(clientId).remove(id);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }
        super.remove(id);
    }

    void addGroupToClient(int clientId, long groupId){
        if(clientsGroups.get(clientId) == null)
            clientsGroups.put(clientId,new LinkedList<>());
        clientsGroups.get(clientId).add(groupId);
    }

    @Override
    public void remove(long id) throws GroupDidNotFound {
        this.remove(id,0);
    }

    private synchronized void synchronize() throws SQLException {
        PreparedStatement pstm = connection.prepareStatement("SELECT * FROM studygroups");
        pstm.executeQuery();
        ResultSet set = pstm.getResultSet();
        while(set.next()){
            int id = set.getInt(1);
            String name = set.getString(2);
            int coords = set.getInt(3);
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(set.getTimestamp(4).toInstant(), ZoneId.systemDefault());
            Long sc = set.getLong(5);
            Long ec = set.getLong(6);
            Integer sbe = set.getInt(7);
            Semester sem = null;
            if(set.getString(8) != null)
                sem = Semester.valueOf(set.getString(8));
            Person admin = null;
            int whoCreated = set.getInt(9);
            if(set.getInt(9) > 0) {
                PreparedStatement pstm2 = connection.prepareStatement("SELECT * FROM person WHERE id = ?");
                pstm2.setInt(1, set.getInt(9));
                pstm2.executeQuery();
                ResultSet set2 = pstm2.getResultSet();
                if (set2.next()) {
                    String aName = set2.getString(2);
                    int weight = set2.getInt(3);
                    Color eyeColor = Color.valueOf(set2.getString(4));
                    Color hairColor = null;
                    if (set2.getString(5) == null)
                        hairColor = Color.valueOf(set2.getString(5));
                    Country nationality = Country.valueOf(set2.getString(6));
                    admin = new Person(aName,weight,eyeColor,hairColor,nationality);
                }
            }
            Coordinates coordinates = null;
            {
                PreparedStatement pstm2 = connection.prepareStatement("SELECT * FROM coordinates WHERE id = ?");
                pstm2.setInt(1, coords);
                pstm2.executeQuery();
                ResultSet set2 = pstm2.getResultSet();
                if (set2.next()) {
                    Float x = set2.getFloat(2);
                    Double y = set2.getDouble(3);
                    coordinates = new Coordinates(x,y);
                }
            }
            super.put(new StudyGroup(id,name, coordinates, zonedDateTime,sc, ec, sbe, sem, admin));
            addGroupToClient(whoCreated,id);
        }
    }

    public synchronized void clear(int clientId){
        try{
            LinkedList<Integer> persons = new LinkedList<>();
            LinkedList<Integer> coords = new LinkedList<>();
            LinkedList<Long> groups = new LinkedList<>();
            PreparedStatement pstm = connection.prepareStatement(
                    "SELECT id, coords, admin FROM studygroups WHERE whocreated=?");
            pstm.setInt(1,clientId);
            pstm.executeQuery();
            ResultSet set = pstm.getResultSet();
            while(set.next()){
                groups.add(set.getLong(1));
                persons.add(set.getInt(3));
                coords.add(set.getInt(2));
            }
            pstm = connection.prepareStatement("DELETE FROM studygroups where whocreated=?");
            pstm.setInt(1,clientId);
            pstm.executeUpdate();

            pstm = connection.prepareStatement("DELETE FROM person WHERE id=?");
            for(Integer i: persons){
                pstm.setInt(1,i);
                pstm.executeUpdate();
            }
            pstm = connection.prepareStatement("DELETE FROM coordinates WHERE id=?");
            for(Integer i: coords){
                pstm.setInt(1,i);
                pstm.executeUpdate();
            }
            persons.clear();
            coords.clear();
            for(Long l: groups){
                super.remove(l);
            }
        } catch (SQLException e) {
        }
    }
    @Override
    public void clear() {
        this.clear(0);
    }

    public synchronized StudyGroup removeHead(int clientId) throws GroupDidNotFound {
        long deletedId = -1;
        StudyGroup group = null;
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM studygroups WHERE whoCreated=? ORDER BY id LIMIT 1"
            );
            preparedStatement.setInt(1,clientId);
            preparedStatement.executeQuery();
            ResultSet set = preparedStatement.getResultSet();
            int coordId = -1;
            int adminId = -1;
            if(set.next()){
                deletedId = set.getLong(1);
                coordId = set.getInt(2);
                if(set.getInt(3) != 0)
                    adminId = set.getInt(3);
            }
            connection.prepareStatement("DELETE FROM studygroups WHERE id in (SELECT id FROM studygroups ORDER BY id LIMIT 1)").execute();
            preparedStatement = connection.prepareStatement("DELETE FROM coordinates WHERE id = ?");
            preparedStatement.setInt(1,coordId);
            if(adminId != -1){
                preparedStatement = connection.prepareStatement("DELETE FROM person WHERE id = ?");
                preparedStatement.setInt(1,adminId);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        super.remove(deletedId);
        return group;
    }

    @Override
    public void update(long id, StudyGroup group){
        this.update(id,group,0);
    }

    public synchronized void update(long id, StudyGroup group, int clientId) throws GroupDidNotFound {
        try{
            PreparedStatement pstm = connection.prepareStatement(
                    "SELECT coords,admin FROM studygroups WHERE id = ? AND whoCreated=?");
            pstm.setLong(1,id);
            pstm.setInt(2,clientId);
            pstm.executeQuery();
            ResultSet set = pstm.getResultSet();
            int coordId = -1, adminId = -1;
            if(set.next()){
                coordId = set.getInt(1);
                adminId = set.getInt(2);
            }
            else{
                throw new GroupDidNotFound("Группы не найдены!");
            }
            pstm = connection.prepareStatement("UPDATE coordinates SET x=?,y=? WHERE id = ?");
            pstm.setInt(3,coordId);
            pstm.setFloat(1,group.getCoordinates().getX());
            pstm.setDouble(2,group.getCoordinates().getY());
            pstm.executeUpdate();

            pstm = connection.prepareStatement("UPDATE person SET name=?,weight=?,eyecolor=?::color,haircolor=?::color,nationality=?::country WHERE id = ?");
            pstm.setInt(6,adminId);
            pstm.setString(1,group.getGroupAdmin().getName());
            pstm.setInt(2,group.getGroupAdmin().getWeight());
            pstm.setString(3,group.getGroupAdmin().getEyeColor().name());
            if(group.getGroupAdmin().getHairColor() != null)
                pstm.setString(4, group.getGroupAdmin().getHairColor().name());
            else
                pstm.setNull(4,Types.NULL);
            pstm.setString(5,group.getGroupAdmin().getNationality().name());
            pstm.executeUpdate();

            pstm = connection.prepareStatement("UPDATE studygroups SET name=?, studentcount=?,expelledstudents=?,shouldbeexpelled = ?,sem = ?::semester WHERE id = ?");
            pstm.setLong(6,id);
            pstm.setString(1,group.getName());
            if(group.getStudentsCount() != null)
                pstm.setLong(2,group.getStudentsCount());
            else
                pstm.setNull(2,Types.NULL);
            if(group.getExpelledStudents() != null)
                pstm.setLong(3,group.getExpelledStudents());
            else
                pstm.setNull(3,Types.NULL);
            pstm.setInt(4,group.getShouldBeExpelled());
            if(group.getSemesterEnum() != null)
                pstm.setString(5,group.getSemesterEnum().name());
            else
                pstm.setNull(5,Types.NULL);
            pstm.executeUpdate();
            synchronize();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized void addIfMax(StudyGroup group, int clientId){
        if(getMax().compareTo(group) == -1){
            add(group,clientId);
        }
    }

    @Override
    public void addIfMax(StudyGroup group) {
        this.addIfMax(group,0);
    }
}