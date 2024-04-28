package aor.paj.ctn.bean;

import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.entity.TaskEntity;
import aor.paj.ctn.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskBeanTest {

    @InjectMocks
    TaskBean taskBean;

    @Mock
    TaskDao taskDao;

    @Mock
    UserDao userDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName("Erase all tasks from user when user exists and has tasks")
    @Test
    void eraseAllTasksFromUser_UserExistsAndHasTasks() {
        UserEntity userEntity = new UserEntity();
        TaskEntity task1 = new TaskEntity();
        TaskEntity task2 = new TaskEntity();
        ArrayList<TaskEntity> userTasks = new ArrayList<>(Arrays.asList(task1, task2));

        when(userDao.findUserByUsername("username")).thenReturn(userEntity);
        when(taskDao.findTasksByUser(userEntity)).thenReturn(userTasks);

        boolean result = taskBean.eraseAllTasksFromUser("username");

        assertTrue(result);
        verify(taskDao, times(2)).merge(any(TaskEntity.class));
    }

    @DisplayName("Do not erase tasks when user does not exist")
    @Test
    void eraseAllTasksFromUser_UserDoesNotExist() {
        when(userDao.findUserByUsername("username")).thenReturn(null);

        boolean result = taskBean.eraseAllTasksFromUser("username");

        assertFalse(result);
        verify(taskDao, never()).merge(any(TaskEntity.class));
    }

    @DisplayName("Do not erase tasks when user has no tasks")
    @Test
    void eraseAllTasksFromUser_UserHasNoTasks() {
        UserEntity userEntity = new UserEntity();
        when(userDao.findUserByUsername("username")).thenReturn(userEntity);
        when(taskDao.findTasksByUser(userEntity)).thenReturn(null);

        boolean result = taskBean.eraseAllTasksFromUser("username");

        assertFalse(result);
        verify(taskDao, never()).merge(any(TaskEntity.class));
    }
}