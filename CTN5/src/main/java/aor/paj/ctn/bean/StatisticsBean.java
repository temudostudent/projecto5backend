package aor.paj.ctn.bean;

import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.io.Serializable;

@Stateless
public class StatisticsBean implements Serializable {

    @EJB
    private UserDao userDao;

    @EJB
    private TaskDao taskDao;

    public StatisticsBean(){}

    public int countAllUsers() {
        int count = userDao.countAllUsers();
        System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW   " + count);
        return count;
    }

    public int countAllUsersByType(int type) {
        int count = userDao.countUsersByTypeOfUser(type);
        return count;
    }

    public int countAllUsersByVisibility(boolean visible) {
        int count = userDao.countUsersByVisibility(visible);
        return count;
    }

    public int countAllTasks() {
        int count = taskDao.countAllTasks();
        return count;
    }

    public int countAllTasksFromUser(String username) {
        UserEntity u = userDao.findUserByUsername(username);
        int count = taskDao.countTasksFromUser(u);
        return count;
    }

    public int countAllTasksFromUserByType(String username, int stateId) {
        UserEntity u = userDao.findUserByUsername(username);
        System.out.println(username +" "+ stateId);
        int count = taskDao.countAllTasksFromUserByState(u, stateId);
        System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW   " + count);
        return count;
    }

}
