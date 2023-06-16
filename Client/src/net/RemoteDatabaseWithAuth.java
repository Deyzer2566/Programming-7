package net;

import command.NotCorrectLoginOrPassword;
import command.ThereIsUserWithThisLogin;
import data.StudyGroup;
import storage.GroupDidNotFound;
import storage.ThereIsGroupWithThisIdException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;

public class RemoteDatabaseWithAuth extends RemoteDatabase {

    private String login;
    private String password;

    public RemoteDatabaseWithAuth(String host, int port) throws IOException {
        super(host, port);
        login = "";
        password = "";
    }

    public void login(String login,String password) throws NotCorrectLoginOrPassword {
        changeLoginAndPassword(login,password);
        try {
            sendLoginAndPassword();
            send(new Object[]{"login "+this.login+" "+this.password});
        } catch (IOException e) {
            disconnect();
        }

        try {
            String err = (String) recieveErr();
            if (err != null)
                throw new NotCorrectLoginOrPassword(err);
        } catch (ClassNotFoundException | ClassCastException e) {
        }
    }

    /**
     *
     * @param login логин
     * @param password пароль
     */
    public void changeLoginAndPassword(String login, String password) {
        this.login = login;
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
        }
        sha.update(password.getBytes(StandardCharsets.UTF_8));
        this.password = Base64.getEncoder().encodeToString(
                        sha.digest());
    }

    public void register(String login, String password) throws ThereIsUserWithThisLogin {
        changeLoginAndPassword(login,password);
        try {
            sendLoginAndPassword();
            send(new Object[]{"register "+this.login+" "+this.password});
        }   catch (IOException e){
            disconnect();
        }
        try {
            String err = (String) recieveErr();
            if (err != null)
                throw new ThereIsUserWithThisLogin(err);
        } catch (ClassNotFoundException | ClassCastException e) {
        }
    }

    public void sendLoginAndPassword() throws IOException {
        send(new Object[]{login,password});
    }

    @Override
    public void add(StudyGroup newGroup) {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return;
        }
        super.add(newGroup);
    }

    @Override
    public void remove(long id) throws GroupDidNotFound {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return;
        }
        super.remove(id);
    }

    @Override
    public void clear() {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return;
        }
        super.clear();
    }

    @Override
    public StudyGroup removeHead() throws GroupDidNotFound {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return null;
        }
        return super.removeHead();
    }

    @Override
    public void update(long id, StudyGroup group) throws GroupDidNotFound {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return;
        }
        super.update(id, group);
    }

    @Override
    public void addIfMax(StudyGroup group) {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return;
        }
        super.addIfMax(group);
    }

    @Override
    public String getInfo() {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return null;
        }
        return super.getInfo();
    }

    @Override
    public StudyGroup getMaxByStudentsCountGroup() {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return null;
        }
        return super.getMaxByStudentsCountGroup();
    }

    @Override
    public Collection<Long> getExpelledStudentsCount() {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return null;
        }
        return super.getExpelledStudentsCount();
    }

    @Override
    public Collection<String> getUniqueNamesGroupsAdmins() {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return null;
        }
        return super.getUniqueNamesGroupsAdmins();
    }

    @Override
    public Collection<StudyGroup> showAllGroups() {
        try {
            this.sendLoginAndPassword();
        } catch (IOException e) {
            disconnect();
            return null;
        }
        return super.showAllGroups();
    }
}
