package aor.paj.ctn.bean;

import aor.paj.ctn.dao.AuthenticationLogDao;
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
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class StatisticsBean implements Serializable {

    @EJB
    private UserDao userDao;
    @EJB
    private TaskDao taskDao;
    @EJB
    private AuthenticationLogDao authenticationLogDao;

    public StatisticsBean(){}

    //USERS
    public OverallStatistics countAllUsers() {
        OverallStatistics s = new OverallStatistics();
        int count = userDao.countAllUsers();

        if (count >= 0){
            s.setUsers(count);
            s.setDevs(userDao.countUsersByTypeOfUser(User.DEVELOPER));
            s.setScrumMasters(userDao.countUsersByTypeOfUser(User.SCRUMMASTER));
            s.setProductOwners(userDao.countUsersByTypeOfUser(User.PRODUCTOWNER));
        }

        countConfirmedAndNotConfirmedUsers(s);

        addUserCountByDayToStatistics(s);

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

    private void countConfirmedAndNotConfirmedUsers(OverallStatistics s) {
        int countConfirmed = authenticationLogDao.countAuthenticatedUsers(true);
        int countNotConfirmed = authenticationLogDao.countAuthenticatedUsers(false);

        s.setConfirmedUsers(countConfirmed);
        s.setNotConfirmedUsers(countNotConfirmed);
    }

    private void addUserCountOverTimeToStatistics(OverallStatistics s) {
        List<Object[]> userCountsOverTime = userDao.countUsersOverTime();
        Map<String, String> usersByTime = new HashMap<>();

        for (Object[] result : userCountsOverTime) {
            if (result[0] != null && result[1] != null && result[2] != null) {
                int year = (int) result[0];
                int week = (int) result[1];
                long count = (long) result[2];

                usersByTime.put(String.valueOf(week + year * 100), String.valueOf(count));
            }
        }

        s.setUsersByTime(usersByTime);
    }

    private void addUserCountByDayToStatistics(OverallStatistics s) {
        List<Object[]> userCountsByDay = userDao.countUsersByDay();

        Map<String, Integer> usersByDay = new HashMap<>();

        for (Object[] result : userCountsByDay) {
            if (result[0] != null && result[1] != null) {
                Timestamp timestamp = (Timestamp) result[0];
                Date date = new Date(timestamp.getTime());
                System.out.println(result[0] + " " + result[1]);
                Long count = (Long) result[1]; // Change this line

                String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);

                // Check if the map already contains an entry for the day
                if (usersByDay.containsKey(dateString)) {
                    // If it does, add the new count to the existing count
                    usersByDay.put(dateString, usersByDay.get(dateString) + count.intValue());
                } else {
                    // If it doesn't, simply put the new count in the map
                    usersByDay.put(dateString, count.intValue());
                }
            }
        }

        // Convert the map values to string
        Map<String, String> usersByDayStr = new HashMap<>();
        for (Map.Entry<String, Integer> entry : usersByDay.entrySet()) {
            usersByDayStr.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        s.setUsersByTime(usersByDayStr);
    }


    //TASKS
    public OverallStatistics countAllTasksOvr() {
        OverallStatistics s = new OverallStatistics();
        int count = taskDao.countAllTasks();

        if (count >= 0){
            s.setTasks(count);
            countAllTasksByState(s);
            updateTasksDoneByTime(s);
            s.setAvgTasksPerUser(taskDao.averageTasksPerUser());
            s.setAvgTaskDone(taskDao.averageCompletionTime());

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

    private void updateTasksDoneByTime(OverallStatistics s) {
        List<Object[]> taskData = taskDao.countTasksByConclusionDate();
        Map<String, String> tasksDoneByTime = new HashMap<>();

        for (Object[] data : taskData) {
            LocalDate conclusionDate = (LocalDate) data[0];
            Long count = (Long) data[1];

            tasksDoneByTime.put(conclusionDate.toString(), count.toString());
        }

        s.setTasksDoneByTime(tasksDoneByTime);
    }

}
