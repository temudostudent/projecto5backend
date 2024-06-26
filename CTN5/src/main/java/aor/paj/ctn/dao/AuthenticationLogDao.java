package aor.paj.ctn.dao;

import aor.paj.ctn.entity.AuthenticationLogEntity;
import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

@Stateless
public class AuthenticationLogDao extends AbstractDao<AuthenticationLogEntity> {

    private static final long serialVersionUID = 1L;

    public AuthenticationLogDao() {
        super(AuthenticationLogEntity.class);
    }

    public AuthenticationLogEntity findALByToken(String token) {
        try {
            return (AuthenticationLogEntity) em.createNamedQuery("AuthenticationLog.findALByToken").setParameter("token", token)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public AuthenticationLogEntity findALByUser(String username) {
        try {
            return (AuthenticationLogEntity) em.createNamedQuery("AuthenticationLog.findALByUser").setParameter("username", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public Integer countAuthenticatedUsers(Boolean isAuth) {
        try {
            return ((Number) em.createNamedQuery("AuthenticationLog.countAuthenticated")
                    .setParameter("isAuth", isAuth)
                    .getSingleResult())
                    .intValue();
        } catch (NoResultException e) {
            return null;
        }
    }
}
