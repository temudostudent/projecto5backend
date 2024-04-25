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
@NamedQuery(name = "Notification.findLatestFromEachSenderByReceiver", query = "SELECT n FROM NotificationEntity n WHERE n.recipient.username = :receiverUsername AND n.timestamp = (SELECT MAX(n2.timestamp) FROM NotificationEntity n2 WHERE n2.sender.username = n.sender.username AND n2.recipient.username = :receiverUsername) ORDER BY n.timestamp DESC")
@NamedQuery(name = "Notification.findLatestFromEachSenderByReceiverAndType", query = "SELECT n FROM NotificationEntity n WHERE n.recipient.username = :receiverUsername AND n.type = :type AND n.timestamp IN (SELECT MAX(n2.timestamp) FROM NotificationEntity n2 WHERE n2.sender.username = n.sender.username AND n2.recipient.username = :receiverUsername AND n2.type = :type GROUP BY n2.sender) ORDER BY n.timestamp DESC")
@NamedQuery(name = "Notification.findLatestFromEachSenderByReceiverAndType10",
        query = "SELECT n FROM NotificationEntity n WHERE n.recipient.username = :receiverUsername AND n.type = 10 AND n.timestamp IN (SELECT MAX(n2.timestamp) FROM NotificationEntity n2 WHERE n2.sender.username = n.sender.username AND n2.recipient.username = :receiverUsername AND n2.type = 10 GROUP BY n2.sender) ORDER BY n.timestamp DESC")

@NamedQuery(name = "Notification.findLatestFromEachSenderByReceiverAndType20",
        query = "SELECT n FROM NotificationEntity n WHERE n.recipient.username = :receiverUsername AND n.type = 20 AND n.timestamp IN (SELECT MAX(n2.timestamp) FROM NotificationEntity n2 WHERE n2.sender.username = n.sender.username AND n2.recipient.username = :receiverUsername AND n2.type = 20 GROUP BY n2.sender) ORDER BY n.timestamp DESC")

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

    @Column(name = "type", nullable = true)
    private int type;

    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private TaskEntity task;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }
}