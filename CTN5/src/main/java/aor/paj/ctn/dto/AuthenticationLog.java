package aor.paj.ctn.dto;

import aor.paj.ctn.entity.UserEntity;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDate;

@XmlRootElement
public class AuthenticationLog {

    @XmlElement
    private boolean authenticated;
    @XmlElement
    private LocalDate sendInviteTime;
    @XmlElement
    private User user;

    public AuthenticationLog() {
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public LocalDate getSendInviteTime() {
        return sendInviteTime;
    }

    public void setSendInviteTime(LocalDate sendInviteTime) {
        this.sendInviteTime = sendInviteTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
