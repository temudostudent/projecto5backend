package aor.paj.ctn.bean;

import aor.paj.ctn.dao.CategoryDao;
import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dto.Notification;
import aor.paj.ctn.dto.Task;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.TaskEntity;
import aor.paj.ctn.entity.UserEntity;
import aor.paj.ctn.websocket.Notifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

@Stateless
public class TaskBean implements Serializable {

    @EJB
    private TaskDao taskDao;
    @EJB
    private CategoryDao categoryDao;
    @EJB
    private UserDao userDao;
    @EJB
    private UserBean userBean;
    @EJB
    private CategoryBean categoryBean;
    @EJB
    private TaskBean taskBean;
    @EJB
    private NotificationBean notificationBean;
    @EJB
    private Notifier notifier;
    @Inject
    ObjectMapper objectMapper;

    private static final Logger logger = LogManager.getLogger(TaskBean.class);


    public boolean newTask(Task task, String token) {
        boolean created = false;

        task.generateId();
        task.setInitialStateId();
        task.setOwner(userBean.convertUserEntitytoUserDto(userDao.findUserByToken(token)));
        task.setErased(false);
        task.setCategory(task.getCategory());
        if (validateTask(task)) {
            taskDao.persist(convertTaskToEntity(task));
            created = true;

            task.setCreateThis(true);
            // Convert the Task DTO to a JSON string
            String taskJson;
            try {
                if (task != null) {
                    taskJson = objectMapper.writeValueAsString(task);
                } else {
                    throw new RuntimeException("Task is null");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error converting task to JSON", e);
            }

            notifier.sendToAllExcept(taskJson, token);

            logger.info("Task " + task.getTitle() + " is added successfully by "+ task.getOwner().getUsername());
            // the following lines are added just to see the difference between different levels in Logger
            logger.debug("Sample debug message");
            logger.info("Sample info message");
            logger.warn("Sample warn message");
            logger.error("Sample error message");
            logger.fatal("Sample fatal message");
        }

        return created;
    }

    public ArrayList<Task> getAllTasks(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        ArrayList<TaskEntity> entityTasks = taskDao.findAllTasks();
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                if (userEntity.getTypeOfUser() == User.DEVELOPER) {
                    tasks.add(convertTaskEntityToTaskDto(taskEntity));
                } else if (userEntity.getTypeOfUser() == User.SCRUMMASTER || userEntity.getTypeOfUser() == User.PRODUCTOWNER) {
                    tasks.add(convertTaskEntityToTaskDto(taskEntity));
                }
            }
        }
        return tasks;
    }

    public ArrayList<Task> getAllTasksFromUser(String username, String token) {
        UserEntity loggedUser = userDao.findUserByToken(token);
        UserEntity tasksOwner = userDao.findUserByUsername(username);
        ArrayList<TaskEntity> entityUserTasks = taskDao.findTasksByUser(tasksOwner);

        ArrayList<Task> userTasks = new ArrayList<>();
        if (entityUserTasks != null) {
            for (TaskEntity taskEntity : entityUserTasks) {
                if (loggedUser.getTypeOfUser() == User.DEVELOPER && !taskEntity.getErased()) {
                    userTasks.add(convertTaskEntityToTaskDto(taskEntity));
                } else if (loggedUser.getTypeOfUser() == User.SCRUMMASTER || loggedUser.getTypeOfUser() == User.PRODUCTOWNER) {
                    userTasks.add(convertTaskEntityToTaskDto(taskEntity));
                }
            }
        }
        return userTasks;
    }

    public Task getTaskById(String id) {
        TaskEntity t = taskDao.findTaskById(id);
        if (t != null) {
            return convertTaskEntityToTaskDto(t);
        } else {
            return null;
        }
    }

    public boolean isTaskIdFromThisOwner(String id, String token) {
        boolean response= false;
        TaskEntity t = taskDao.findTaskById(id);
        UserEntity u = userDao.findUserByToken(token);
        if (t != null && t.getOwner().equals(u) ) {
            return true;
        }

        return response;
    }

    public boolean updateTask(String token, Task originalTask, Task updatedTask) {
        // Verifica se a tarefa original existe no banco de dados
        if (taskDao.findTaskById(originalTask.getId()) == null) {
            return false; // Tarefa não encontrada, não pode ser atualizada
        }

        // Atualiza apenas os campos fornecidos
        if (updatedTask.getId() != null) {
            originalTask.setId(updatedTask.getId());
        }
        if (updatedTask.getTitle() != null) {
            originalTask.setTitle(updatedTask.getTitle());
        }
        if (updatedTask.getDescription() != null) {
            originalTask.setDescription(updatedTask.getDescription());
        }
        if (updatedTask.getCategory() != null) {
            originalTask.setCategory(updatedTask.getCategory());
        }
        if (updatedTask.getStartDate() != null) {
            originalTask.setStartDate(updatedTask.getStartDate());
        }
        if (updatedTask.getLimitDate() != null) {
            originalTask.setLimitDate(updatedTask.getLimitDate());
        }

        // Valida a tarefa atualizada
        if (validateTaskForUpdate(originalTask, updatedTask)) {
            // Atualiza a tarefa no banco de dados
            taskDao.merge(convertTaskToEntity(originalTask));

            // Convert the Task DTO to a JSON string
            String taskJson;
            try {
                if (updatedTask != null) {
                    taskJson = objectMapper.writeValueAsString(originalTask);
                } else {
                    throw new RuntimeException("Task is null");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error converting task to JSON", e);
            }

            notifier.sendToAllExcept(taskJson, token);

            return true;
        }
        return false;
    }

    public boolean updateTaskStatus(String token, String taskId, int stateId) {
        boolean updated = false;

        if (stateId != 100 && stateId != 200 && stateId != 300) {
            updated = false;
        } else {
            TaskEntity taskEntity = taskDao.findTaskById(taskId);
            if (taskEntity != null) {
                taskEntity.setStateId(stateId);
                if (taskEntity.getStateId() == 300) {
                    taskEntity.setConclusionDate(LocalDate.now());
                    if (taskEntity.getStartDate().isAfter(taskEntity.getConclusionDate())) {
                        taskEntity.setStartDate(taskEntity.getConclusionDate());
                    }
                }
                taskDao.merge(taskEntity);

                // Create a new Task DTO
                Task task = taskBean.convertTaskEntityToTaskDto(taskEntity);

                // Convert the Task DTO to a JSON string
                String taskJson;
                try {
                    if (task != null) {
                        taskJson = objectMapper.writeValueAsString(task);
                    } else {
                        throw new RuntimeException("Task is null");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error converting task to JSON", e);
                }

                notifier.sendToAllExcept(taskJson, token);

                String currentUserUsername = userBean.convertEntityByToken(token).getUsername();
                String taskOwnerUsername = task.getOwner().getUsername();

                if (!currentUserUsername.equals(taskOwnerUsername)) {
                    notificationBean.sendNotification(task.getOwner(), token, Notification.TASK, taskEntity);
                }

                updated = true;
            }
        }
        return updated;
    }



    public boolean switchErasedTaskStatus(String token, String id) {
        boolean swithedErased = false;
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if(taskEntity != null) {
            taskEntity.setErased(!taskEntity.getErased());
            taskDao.merge(taskEntity);

            // Create a new Task DTO
            Task task = taskBean.convertTaskEntityToTaskDto(taskEntity);

            // Convert the Task DTO to a JSON string
            String taskJson;
            try {
                if (task != null) {
                    taskJson = objectMapper.writeValueAsString(task);
                } else {
                    throw new RuntimeException("Task is null");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error converting task to JSON", e);
            }

            notifier.sendToAllExcept(taskJson, token);

            swithedErased = true;
        }
        return swithedErased;
    }

    public boolean permanentlyDeleteTask(String token, String id) {
        boolean removed = false;
        TaskEntity taskEntity = taskDao.findTaskById(id);
        if (taskEntity != null && !taskEntity.getErased()) {
            taskDao.eraseTask(id);
            removed = true;
        } else if (taskEntity != null && taskEntity.getErased()) {
            taskDao.deleteTask(id);

            // Create a new Task DTO
            Task task = new Task();
            task.setId(id);
            task.setDeleteThis(true);

            // Convert the Task DTO to a JSON string
            String taskJson;
            try {
                if (task != null) {
                    taskJson = objectMapper.writeValueAsString(task);
                } else {
                    throw new RuntimeException("Task is null");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error converting task to JSON", e);
            }

            notifier.sendToAllExcept(taskJson, token);
            removed = true;
        }
        return removed;
    }

    public ArrayList<Task> getTasksByCategory(String category) {
        ArrayList<TaskEntity> entityTasks = categoryDao.findTasksByCategory(category);
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        }
        return tasks;
    }

    public boolean validateTask(Task task) {
        boolean valid = true;

        if (task.getTitle() == null || task.getDescription() == null ||
                task.getOwner() == null || task.getCategory() == null ||
                task.getPriority() == 0 || task.getStateId() == 0) {
            valid = false;
        }
        else {
            // Se as datas de início e limite estiverem presentes, verifique se a data limite é posterior à data de início
            if (task.getStartDate() != null && task.getLimitDate() != null) {
                valid = task.getLimitDate().isAfter(task.getStartDate());
            }

            // Verifique se a categoria existe
            if (valid) {
                valid = categoryBean.categoryExists(task.getCategory().getName());
            }

            // Verifique se a prioridade e o estado são válidos
            if (valid) {
                valid = (task.getPriority() == Task.LOWPRIORITY ||
                        task.getPriority() == Task.MEDIUMPRIORITY ||
                        task.getPriority() == Task.HIGHPRIORITY);
                valid = valid && (task.getStateId() == Task.TODO ||
                        task.getStateId() == Task.DOING ||
                        task.getStateId() == Task.DONE);
            }
        }

        return valid;
    }

    public boolean validateTaskForUpdate(Task originalTask, Task updatedTask) {
        boolean valid = true;

        // Verificar e validar apenas os atributos que foram atualizados (não são nulos)
        if (updatedTask.getTitle() != null && !updatedTask.getTitle().isBlank()) {
            originalTask.setTitle(updatedTask.getTitle());
        }
        if (updatedTask.getDescription() != null && !updatedTask.getDescription().isBlank()) {
            originalTask.setDescription(updatedTask.getDescription());
        }
        if (updatedTask.getOwner() != null) {
            originalTask.setOwner(updatedTask.getOwner());
        }
        if (updatedTask.getCategory() != null) {
            if (categoryBean.categoryExists(updatedTask.getCategory().getName())) {
                originalTask.setCategory(updatedTask.getCategory());
            } else {
                valid = false; // Categoria não existe
            }
        }
        if (updatedTask.getPriority() != 0) {
            if (updatedTask.getPriority() == Task.LOWPRIORITY ||
                    updatedTask.getPriority() == Task.MEDIUMPRIORITY ||
                    updatedTask.getPriority() == Task.HIGHPRIORITY) {
                originalTask.setPriority(updatedTask.getPriority());
            } else {
                valid = false; // Prioridade inválida
            }
        }
        if (updatedTask.getStateId() != 0) {
            if (updatedTask.getStateId() == Task.TODO ||
                    updatedTask.getStateId() == Task.DOING ||
                    updatedTask.getStateId() == Task.DONE) {
                originalTask.setStateId(updatedTask.getStateId());
            } else {
                valid = false; // Estado inválido
            }
        }

        // Verificar se as datas de início e limite são válidas
        if (updatedTask.getStartDate() != null || updatedTask.getLimitDate() != null) {
            LocalDate startDate = updatedTask.getStartDate() != null ? updatedTask.getStartDate() : originalTask.getStartDate();
            LocalDate limitDate = updatedTask.getLimitDate() != null ? updatedTask.getLimitDate() : originalTask.getLimitDate();

            if (limitDate.isAfter(startDate)) {
                originalTask.setStartDate(startDate);
                originalTask.setLimitDate(limitDate);
            } else {
                valid = false; // Data limite não é posterior à data de início
            }
        }

        return valid;
    }

    public ArrayList<Task> getErasedTasks() {
        ArrayList<TaskEntity> entityTasks = taskDao.findErasedTasks();
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        }
        return tasks;
    }

    public ArrayList<Task> getTasksByErasedStatus(boolean erasedStatus) {
        ArrayList<TaskEntity> entityTasks = taskDao.findTasksByErasedStatus(erasedStatus);
        ArrayList<Task> tasks = new ArrayList<>();
        if (entityTasks != null) {
            for (TaskEntity taskEntity : entityTasks) {
                tasks.add(convertTaskEntityToTaskDto(taskEntity));
            }
        }
        return tasks;
    }

    public boolean eraseAllTasksFromUser(String username) {
        boolean erased = false;
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            ArrayList<TaskEntity> userTasks = taskDao.findTasksByUser(userEntity);
            if (userTasks != null) {
                for (TaskEntity taskEntity : userTasks) {
                    if (taskEntity.getErased()==false) {
                        taskEntity.setErased(true);
                        taskDao.merge(taskEntity);
                    }
                }
                erased = true;
            }
        }
        return erased;
    }


    private TaskEntity convertTaskToEntity(Task task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(task.getId());
        taskEntity.setTitle(task.getTitle());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setPriority(task.getPriority());
        taskEntity.setStateId(task.getStateId());
        taskEntity.setStartDate(task.getStartDate());
        taskEntity.setLimitDate(task.getLimitDate());
        taskEntity.setCategory(categoryDao.findCategoryByName(task.getCategory().getName()));
        taskEntity.setErased(task.getErased());
        taskEntity.setOwner(userBean.convertUserDtotoUserEntity(task.getOwner()));
        return taskEntity;
    }

    public Task convertTaskEntityToTaskDto(TaskEntity taskEntity) {
        Task task = new Task();
        task.setId(taskEntity.getId());
        task.setTitle(taskEntity.getTitle());
        task.setDescription(taskEntity.getDescription());
        task.setPriority(taskEntity.getPriority());
        task.setStateId(taskEntity.getStateId());
        task.setStartDate(taskEntity.getStartDate());
        task.setLimitDate(taskEntity.getLimitDate());
        task.setCategory(categoryBean.convertCategoryEntityToCategoryDto(taskEntity.getCategory()));
        task.setErased(taskEntity.getErased());
        task.setOwner(userBean.convertUserEntitytoUserDto(taskEntity.getOwner()));
        return task;
    }



}
