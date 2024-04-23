package aor.paj.ctn.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "authentication_log")
@NamedQuery(name = "AuthenticationLog.findALByToken", query = "SELECT DISTINCT al FROM AuthenticationLogEntity al WHERE al.confirmToken = :token")
@NamedQuery(name = "AuthenticationLog.findALByUser", query = "SELECT DISTINCT al FROM AuthenticationLogEntity al WHERE al.user = :user")
@NamedQuery(name = "AuthenticationLog.countAuthenticated", query = "SELECT COUNT(al) FROM AuthenticationLogEntity al WHERE al.authenticated = :isAuth")
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

    @Column(name="confirm_token", nullable=true, unique = true, updatable = true)
    private String confirmToken;

    @Column(name = "confirm_token_expiry", nullable = true)
    private Date confirmTokenExpiry;


    @CreationTimestamp
    @Column(name = "invite_time", nullable = false)
    private LocalDate sendInviteTime;

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

    public LocalDate getAuthenticationTime() {
        return sendInviteTime;
    }

    public LocalDate getSendInviteTime() {
        return sendInviteTime;
    }

    public void setSendInviteTime(LocalDate sendInviteTime) {
        this.sendInviteTime = sendInviteTime;
    }

    public String getConfirmToken() {
        return confirmToken;
    }

    public void setConfirmToken(String confirmToken) {
        this.confirmToken = confirmToken;
    }

    public Date getConfirmTokenExpiry() {
        return confirmTokenExpiry;
    }

    public void setConfirmTokenExpiry(Date confirmTokenExpiry) {
        this.confirmTokenExpiry = confirmTokenExpiry;
    }
}
