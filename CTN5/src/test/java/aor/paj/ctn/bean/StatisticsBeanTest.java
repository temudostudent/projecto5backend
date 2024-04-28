package aor.paj.ctn.bean;


import aor.paj.ctn.dao.AuthenticationLogDao;
import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dto.OverallStatistics;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.dto.UserStatistics;
import aor.paj.ctn.entity.UserEntity;
import aor.paj.ctn.websocket.Notifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StatisticsBeanTest {

    @InjectMocks
    private StatisticsBean statisticsBean;

    @Mock
    private UserDao userDao;

    @Mock
    private TaskDao taskDao;

    @Mock
    private AuthenticationLogDao authenticationLogDao;

    @Mock
    private Notifier notifier;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void countAllUsersReturnsCorrectStatistics() {
        when(userDao.countAllUsers()).thenReturn(10);
        when(userDao.countUsersByTypeOfUser(User.DEVELOPER)).thenReturn(5);
        when(userDao.countUsersByTypeOfUser(User.SCRUMMASTER)).thenReturn(3);
        when(userDao.countUsersByTypeOfUser(User.PRODUCTOWNER)).thenReturn(2);

        OverallStatistics stats = statisticsBean.countAllUsers("token");

        assertEquals(10, stats.getUsers());
        assertEquals(5, stats.getDevs());
        assertEquals(3, stats.getScrumMasters());
        assertEquals(2, stats.getProductOwners());
    }

    @Test
    void countUsersByTypeReturnsCorrectStatistics() {
        when(userDao.countUsersByTypeOfUser(User.DEVELOPER)).thenReturn(5);
        when(userDao.countUsersByTypeOfUser(User.SCRUMMASTER)).thenReturn(3);
        when(userDao.countUsersByTypeOfUser(User.PRODUCTOWNER)).thenReturn(2);

        OverallStatistics stats = statisticsBean.countUsersByType(User.DEVELOPER);

        assertEquals(5, stats.getDevs());
        assertEquals(3, stats.getScrumMasters());
        assertEquals(2, stats.getProductOwners());
    }

    @Test
    void countAllTasksFromUserReturnsCorrectStatistics() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");

        when(userDao.findUserByUsername("testUser")).thenReturn(userEntity);
        when(taskDao.countTasksFromUser(userEntity)).thenReturn(10);
        when(taskDao.countAllTasksFromUserByState(userEntity, 100)).thenReturn(5);
        when(taskDao.countAllTasksFromUserByState(userEntity, 200)).thenReturn(3);
        when(taskDao.countAllTasksFromUserByState(userEntity, 300)).thenReturn(2);

        UserStatistics stats = statisticsBean.countAllTasksFromUser("testUser");

        assertEquals(10, stats.getTasks());
        assertEquals(5, stats.getToDo());
        assertEquals(3, stats.getDoing());
        assertEquals(2, stats.getDone());
    }
}