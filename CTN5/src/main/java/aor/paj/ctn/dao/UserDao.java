package aor.paj.ctn.dao;

import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {

	private static final long serialVersionUID = 1L;

	public UserDao() {
		super(UserEntity.class);
	}


	public UserEntity findUserByToken(String token) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByConfirmToken(String token) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByConfirmToken").setParameter("confirmToken", token)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByResetToken(String token) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByResetToken").setParameter("resetToken", token)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByUsername(String username) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}


	public UserEntity findUserByEmail(String email) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByPhone(String phone) {
		try {
			return (UserEntity) em.createNamedQuery("User.findUserByPhone").setParameter("phone", phone)
					.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public UserEntity findUserByUsernameAndPassword(String username, String password){
		try{
			return (UserEntity) em.createNamedQuery("User.findUserByUsernameAndPassword")
					.setParameter("username", username)
					.setParameter("password", password)
					.getSingleResult();
		}catch (NoResultException e){
			return null; //Nenhum user foi encontrado com estes dados
		}
	}

	public String findTokenByUsername(String username) {
		try {
			return (String) em.createNamedQuery("User.findTokenByUsername").setParameter("username", username)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public ArrayList<UserEntity> findAllUsers() {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsers").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<UserEntity> findAllUsersByTypeOfUser(int typeOfUser) {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsersByTypeOfUser").setParameter("typeOfUser", typeOfUser).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<UserEntity> findAllUsersByVisibility(boolean visible) {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsersByVisibility").setParameter("visible", visible).getResultList();
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<UserEntity> findAllUsersByTypeOfUserAndVisibility(int typeOfUser, boolean visible) {
		try {
			return (ArrayList<UserEntity>) em.createNamedQuery("User.findAllUsersByTypeOfUserByVisibility").setParameter("typeOfUser", typeOfUser)
					.setParameter("visible", visible).getResultList();
		} catch (Exception e) {
			return null;
		}
	}


	//STATISTICS
	public Integer countAllUsers() {
		try {
			return ((Number) em.createNamedQuery("User.countAllUsers").getSingleResult()).intValue();
		} catch (Exception e) {
			return null;
		}
	}

	public Integer countUsersByTypeOfUser(int typeOfUser) {
		try {
			return ((Number) em.createNamedQuery("User.countUsersByTypeOfUser").setParameter("typeOfUser", typeOfUser).getSingleResult()).intValue();
		} catch (Exception e) {
			return null;
		}
	}

	public Integer countUsersByVisibility(boolean visible) {
		try {
			return ((Number) em.createNamedQuery("User.countUsersByVisibility").setParameter("visible", visible).getSingleResult()).intValue();
		} catch (Exception e) {
			return null;
		}
	}

	public List<Object[]> countUsersOverTime() {
		try {
			return em.createNamedQuery("User.countUsersOverTime").getResultList();
		} catch (Exception e) {
			return null;
		}
	}

}
