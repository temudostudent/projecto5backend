package aor.paj.ctn.dao;

import aor.paj.ctn.entity.TaskEntity;
import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class TaskDao extends AbstractDao<TaskEntity> {

	private static final long serialVersionUID = 1L;

	public TaskDao() {
		super(TaskEntity.class);
	}
	

	public TaskEntity findTaskById(String id) {
		try {
			return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> findTasksByUser(UserEntity userEntity) {
		try {
            return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByUser").setParameter("owner", userEntity).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> findTasksByCategoryID(int id) {
		try {
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByCategoryID").setParameter("categoryId", id).getResultList();
		} catch (Exception e) {
			return null;
		}
	}


	public ArrayList<TaskEntity> findErasedTasks() {
		try {
            return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findErasedTasks").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<TaskEntity> findActiveTasks() {
		try {
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findActiveTasks").getResultList();
		} catch (Exception e) {
			return null;
		}
	}


	public ArrayList<TaskEntity> findTasksByErasedStatus(boolean erasedStatus) {
		try {
			return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTasksByErasedStatus").setParameter("erased", erasedStatus).getResultList();
		} catch (Exception e) {
			return null;
		}
	}


	public ArrayList<TaskEntity> findAllTasks() {
		try {
            return (ArrayList<TaskEntity>) em.createNamedQuery("Task.findAllTasks").getResultList();
		} catch (Exception e) {
			return null;
		}
	}


	public boolean eraseTask(String id) {
		boolean erased = false;
		if (id == null) {
			erased = false;
		} else {
			try {
				TaskEntity taskToErase = findTaskById(id);
				taskToErase.setErased(true);
				merge(taskToErase);
				erased = true;
			} catch (Exception e) {
				erased = false;
			}
		}
		return erased;
	}


	public boolean deleteTask(String id) {
		boolean deleted = false;
		if (id == null) {
			deleted = false;
		} else {
			try {
				em.createNamedQuery("DeleteTask").setParameter("id", id).executeUpdate();
				deleted = true;
			} catch (Exception e) {
				deleted = false;
			}
		}
		return deleted;
	}


	//STATISTICS
	public Integer countAllTasks() {
		try {
			return ((Number) em.createNamedQuery("Task.countAllTasks").getSingleResult()).intValue();
		} catch (Exception e) {
			return null;
		}
	}
	public Integer countAllTasksByState(int stateId) {
		try {
			return ((Number) em.createNamedQuery("Task.countTasksByState").setParameter("stateId", stateId).getSingleResult()).intValue();
		} catch (Exception e) {
			return null;
		}
	}


	public Integer countTasksFromUser(UserEntity u) {
		try {
			return ((Number) em.createNamedQuery("Task.countAllTasksFromUser").setParameter("owner", u).getSingleResult()).intValue();
		} catch (Exception e) {
			return null;
		}
	}

	public Integer countAllTasksFromUserByState(UserEntity u, int stateId) {
		try {
			return ((Number) em.createNamedQuery("Task.countAllTasksFromUserByState").setParameter("owner", u).
					setParameter("stateId", stateId).getSingleResult()).intValue();
		} catch (Exception e) {
			return null;
		}
	}

	public Double averageTasksPerUser() {
		try {
			return ((Number) em.createNamedQuery("Task.averageTasksPerUser").getSingleResult()).doubleValue();
		} catch (Exception e) {
			return null;
		}
	}

	public Double averageCompletionTime() {
		try {
			List<Object[]> dates = em.createNamedQuery("Task.fetchStartAndConclusionDates").getResultList();
			if (dates.isEmpty()) {
				return null;
			}
			long totalDays = 0;
			for (Object[] datePair : dates) {
				LocalDate startDate = (LocalDate) datePair[0];
				LocalDate conclusionDate = (LocalDate) datePair[1];
				totalDays += ChronoUnit.DAYS.between(startDate, conclusionDate);
			}
			return (double) totalDays / dates.size();
		} catch (Exception e) {
			return null;
		}
	}

	public List<Object[]> countTasksByConclusionDate() {
		try {
			return em.createNamedQuery("Task.countTasksByConclusionDate").getResultList();
		} catch (Exception e) {
			return null;
		}
	}



}
