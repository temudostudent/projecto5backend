package aor.paj.ctn.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "authentication_log")
public class AuthenticationLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    private UserEntity user;

    @Column(name="authenticated", nullable = false, unique = false, updatable = true)
    private boolean authenticated;

    @CreationTimestamp
    @Column(name = "authentication_time", nullable = false)
    private Timestamp authenticationTime;

    public AuthenticationLogEntity() {
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Timestamp getAuthenticationTime() {
        return authenticationTime;
    }

    public void setAuthenticationTime(Timestamp authenticationTime) {
        this.authenticationTime = authenticationTime;
    }
}
