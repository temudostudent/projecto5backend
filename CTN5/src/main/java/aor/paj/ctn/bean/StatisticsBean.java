package aor.paj.ctn.bean;

import aor.paj.ctn.dao.AuthenticationLogDao;
import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.dto.OverallStatistics;
import aor.paj.ctn.dto.Task;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.dto.UserStatistics;
import aor.paj.ctn.entity.UserEntity;
import aor.paj.ctn.websocket.Notifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Stateless
public class StatisticsBean implements Serializable {

    @EJB
    private UserDao userDao;
    @EJB
    private TaskDao taskDao;
    @EJB
    private AuthenticationLogDao authenticationLogDao;
    @EJB
    private Notifier notifier;
    @Inject
    ObjectMapper objectMapper;

    public StatisticsBean(){}

    //USERS
    public OverallStatistics countAllUsers(String token) {
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

        // Convert the DTO to a JSON string
        String userStatsJson;
        try {
            if (s != null) {
                userStatsJson = objectMapper.writeValueAsString(s);
            } else {
                throw new RuntimeException("User Stats is null");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting user stats to JSON", e);
        }

        notifier.sendToAllExcept(userStatsJson, token);

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

        Map<String, Integer> usersByDay = new TreeMap<>(); // Changed to TreeMap

        for (Object[] result : userCountsByDay) {
            if (result[0] != null && result[1] != null) {
                Timestamp timestamp = (Timestamp) result[0];
                Date date = new Date(timestamp.getTime());
                Long count = (Long) result[1];

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
        Map<String, String> usersByDayStr = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : usersByDay.entrySet()) {
            usersByDayStr.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        s.setUsersByTime(usersByDayStr);
    }


    //TASKS
    public OverallStatistics countAllTasksOvr(String token) {

        OverallStatistics s = new OverallStatistics();
        int count = taskDao.countAllTasks();

        if (count >= 0){
            s.setTasks(count);
            countAllTasksByState(s);
            updateTasksDoneByTime(s);
            s.setAvgTasksPerUser(taskDao.averageTasksPerUser());
            s.setAvgTaskDone(taskDao.averageCompletionTime());

        }

        // Convert the DTO to a JSON string
        String tasksStatsJson;
        try {
            if (s != null) {
                tasksStatsJson = objectMapper.writeValueAsString(s);
            } else {
                throw new RuntimeException("Task Stats is null");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting task stats to JSON", e);
        }

        System.out.println("statsBean");
        notifier.sendToAllExcept(tasksStatsJson, token);
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

        Map<String, String> tasksDoneByTime = new TreeMap<>();
        long runningTotal = 0;

        for (Object[] data : taskData) {
            LocalDate conclusionDate = (LocalDate) data[0];
            Long count = (Long) data[1];

            runningTotal += count;
            tasksDoneByTime.put(conclusionDate.toString(), String.valueOf(runningTotal));
        }

        s.setTasksDoneByTime(tasksDoneByTime);
    }

}
