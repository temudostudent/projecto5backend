package aor.paj.ctn.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "message")
@NamedQuery(name = "Message.findAllMessages", query = "SELECT m FROM MessageEntity m")
@NamedQuery(name = "Message.findMessagesBySender", query = "SELECT m FROM MessageEntity m WHERE m.sender = :sender")
@NamedQuery(name = "Message.findMessagesByReceiver", query = "SELECT m FROM MessageEntity m WHERE m.recipient = :recipient")
@NamedQuery(name = "Message.findMessagesByReadStatus", query = "SELECT m FROM MessageEntity m WHERE m.readStatus = :readStatus")
@NamedQuery(name = "Message.findMessagesBetweenTwoUsers",
        query = "SELECT m FROM MessageEntity m WHERE (m.sender = :user1 AND m.recipient = :user2) OR (m.sender = :user2 AND m.recipient = :user1) ORDER BY m.timestamp ASC")
public class MessageEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_username", referencedColumnName = "username")
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "recipient_username", referencedColumnName = "username")
    private UserEntity recipient;

    @Column(name = "message_content", nullable = false)
    private String messageContent;

    @Column(name = "timestamp", nullable = false)
    private LocalDate timestamp;

    @Column(name = "read_timestamp", nullable = true)
    private LocalDate readTimestamp;

    @Column(name = "read_status", nullable = false)
    private boolean readStatus;

    public MessageEntity() {
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
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
