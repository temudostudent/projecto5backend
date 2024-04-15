package aor.paj.ctn.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "notification")
@NamedQuery(name = "Notification.findAllNotifications", query = "SELECT n FROM NotificationEntity n")
@NamedQuery(name = "Notification.findNotificationsByReceiver", query = "SELECT n FROM NotificationEntity n WHERE n.recipient = :recipient")
@NamedQuery(name = "Notification.findNotificationsByReadStatus", query = "SELECT n FROM NotificationEntity n WHERE n.readStatus = :readStatus")
@NamedQuery(name = "Notification.findUserNotificationsByReadStatus", query = "SELECT n FROM NotificationEntity n WHERE n.readStatus = :readStatus AND n.recipient = :recipient")
@NamedQuery(name = "Notification.countUserNotificationsByReadStatus", query = "SELECT COUNT(n) FROM NotificationEntity n WHERE n.readStatus = :readStatus AND n.recipient = :recipient")
public class NotificationEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_username", referencedColumnName = "username")
    private UserEntity recipient;

    @ManyToOne
    @JoinColumn(name = "sender_username", referencedColumnName = "username")
    private UserEntity sender;

    @Column(name = "timestamp", nullable = false)
    private LocalDate timestamp;

    @Column(name = "read_timestamp", nullable = true)
    private LocalDate readTimestamp;

    @Column(name = "read_status", nullable = false)
    private boolean readStatus;

    public NotificationEntity() {
    }

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDate getReadTimestamp() {
        return readTimestamp;
    }

    public void setReadTimestamp(LocalDate readTimestamp) {
        this.readTimestamp = readTimestamp;
    }

    public Long getId() {
        return id;
    }
}