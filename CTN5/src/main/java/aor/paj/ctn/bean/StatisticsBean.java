package aor.paj.ctn.bean;

import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.dto.OverallStatistics;
import aor.paj.ctn.dto.UserStatistics;
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

    public OverallStatistics countAllUsers() {
        OverallStatistics s = new OverallStatistics();
        int count = userDao.countAllUsers();

        if (count >= 0){
            s.setUsers(count);
            countAllUsersByType(s);
        }
        return s;
    }

    private OverallStatistics countAllUsersByType(OverallStatistics s) {

        int count;

        for(int i=100;i<=300;i+=100){
            count = userDao.countUsersByTypeOfUser(i);
            if (i==100){
                s.setDevs(count);
            } else if (i==200) {
                s.setScrumMasters(count);
            } else if (i==300) {
                s.setProductOwners(count);
            }
        }

        return s;
    }

    public OverallStatistics countUsersByType(int type) {
        OverallStatistics s = new OverallStatistics();
        int count=userDao.countUsersByTypeOfUser(type);

        if (count >= 0){
            if (type==100){
                s.setDevs(count);
            } else if (type==200) {
                s.setScrumMasters(count);
            } else if (type==300) {
                s.setProductOwners(count);
            }
        }

        return s;
    }

    public OverallStatistics countAllTasksOvr() {
        OverallStatistics s = new OverallStatistics();
        int count = taskDao.countAllTasks();

        if (count >= 0){
            s.setTasks(count);
            countAllTasksByState(s);
        }
        return s;
    }

    private OverallStatistics countAllTasksByState(OverallStatistics s) {

        int count;

        for(int i=100;i<=300;i+=100){
            count = taskDao.countAllTasksByState(i);
                if (i==100){
                    s.setToDo(count);
                } else if (i==200) {
                    s.setDoing(count);
                } else if (i==300) {
                    s.setDone(count);
                }
        }

        return s;
    }

    public UserStatistics countAllTasksFromUser(String username) {
        UserEntity u = userDao.findUserByUsername(username);
        UserStatistics s = new UserStatistics();
        int count = taskDao.countTasksFromUser(u);

        if (count >= 0){
            s.setTasks(count);
            countAllTasksFromUserByType(u, s);
        }
        return s;
    }

    //Conta todas as tarefas por estado e adiciona ao UserStatistics
    private UserStatistics countAllTasksFromUserByType(UserEntity u, UserStatistics s) {

        int count;

        for(int i=100;i<=300;i+=100){
            count = taskDao.countAllTasksFromUserByState(u, i);
            if (i==100){
                s.setToDo(count);
            } else if (i==200) {
                s.setDoing(count);
            } else if (i==300) {
                s.setDone(count);
            }
        }

        return s;
    }

    //Conta apenas as tarefas do estado definido do User
    public UserStatistics countTasksFromUserByType(String username, int stateId) {
        UserEntity u = userDao.findUserByUsername(username);
        UserStatistics s = new UserStatistics();

        int count = taskDao.countAllTasksFromUserByState(u, stateId);

        if (count >= 0){
            if (stateId==100){
                s.setToDo(count);
            } else if (stateId==200) {
                s.setDoing(count);
            } else if (stateId==300) {
                s.setDone(count);
            }
        }
        return s;
    }

}
