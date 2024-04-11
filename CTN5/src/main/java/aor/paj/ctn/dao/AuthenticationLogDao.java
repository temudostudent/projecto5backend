package aor.paj.ctn.dao;

import aor.paj.ctn.entity.AuthenticationLogEntity;
import jakarta.ejb.Stateless;

@Stateless
public class AuthenticationLogDao extends AbstractDao<AuthenticationLogEntity> {

    private static final long serialVersionUID = 1L;

    public AuthenticationLogDao() {
        super(AuthenticationLogEntity.class);
    }
}
