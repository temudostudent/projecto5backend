package aor.paj.ctn.bean;

import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.dto.OverallStatistics;
import aor.paj.ctn.dto.Task;
import aor.paj.ctn.dto.User;
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
            s.setDevs(userDao.countUsersByTypeOfUser(User.DEVELOPER));
            s.setScrumMasters(userDao.countUsersByTypeOfUser(User.SCRUMMASTER));
            s.setProductOwners(userDao.countUsersByTypeOfUser(User.PRODUCTOWNER));
        }
        return s;
    }

    public OverallStatistics countUsersByType(int type) {
        OverallStatistics s = new OverallStatistics();

        int countDevs = userDao.countUsersByTypeOfUser(User.DEVELOPER);
        int countScrumMasters = userDao.countUsersByTypeOfUser(User.SCRUMMASTER);
        int countProOwners = userDao.countUsersByTypeOfUser(User.PRODUCTOWNER);

        s.setDevs(countDevs);
        s.setScrumMasters(countScrumMasters);
        s.setProductOwners(countProOwners);

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
        int countTodo = taskDao.countAllTasksByState(Task.TODO);
        int countDoing = taskDao.countAllTasksByState(Task.DOING);
        int countDone = taskDao.countAllTasksByState(Task.DONE);

        s.setToDo(countTodo);
        s.setDoing(countDoing);
        s.setDone(countDone);

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

        int countTodo = taskDao.countAllTasksFromUserByState(u, Task.TODO);
        int countDoing = taskDao.countAllTasksFromUserByState(u, Task.DOING);
        int countDone = taskDao.countAllTasksFromUserByState(u, Task.DONE);

        s.setToDo(countTodo);
        s.setDoing(countDoing);
        s.setDone(countDone);

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
