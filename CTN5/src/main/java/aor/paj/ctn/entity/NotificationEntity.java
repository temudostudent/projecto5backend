package aor.paj.ctn.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@NamedQuery(name = "Notification.findAllNotifications", query = "SELECT n FROM NotificationEntity n")
@NamedQuery(name = "Notification.findNotificationsByReceiver", query = "SELECT n FROM NotificationEntity n WHERE n.recipient.username = :username")
@NamedQuery(name = "Notification.findNotificationsByReadStatus", query = "SELECT n FROM NotificationEntity n WHERE n.readStatus = :readStatus")
@NamedQuery(name = "Notification.findUserNotificationsByReadStatus", query = "SELECT n FROM NotificationEntity n WHERE n.readStatus = :readStatus AND n.recipient.username = :username ORDER BY n.timestamp DESC")
@NamedQuery(name = "Notification.findNotificationsBySenderAndReceiver", query = "SELECT n FROM NotificationEntity n WHERE n.readStatus = :readStatus AND n.sender.username = :senderUsername AND n.recipient.username = :receiverUsername")
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
    private LocalDateTime timestamp;

    @Column(name = "read_timestamp", nullable = true)
    private LocalDateTime readTimestamp;

    @Column(name = "read_status", nullable = false)
    private boolean readStatus;

    @Column(name = "type", nullable = false)
    private String type;

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getReadTimestamp() {
        return readTimestamp;
    }

    public void setReadTimestamp(LocalDateTime readTimestamp) {
        this.readTimestamp = readTimestamp;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}