package aor.paj.ctn.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;

@XmlRootElement
public class Notification {
    @XmlElement
    private String id;
    @XmlElement
    private User sender;
    @XmlElement
    private User receiver;
    @XmlElement
    private boolean readStatus;
    @XmlElement
    private LocalDateTime timestamp;
    @XmlElement
    private int type;
    @XmlElement
    public static final int MESSAGE = 10;
    @XmlElement
    public static final int TASK = 20;
    @XmlElement
    private static final String dtoType = "Notification";
    @XmlElement
    private Task task;

    public Notification() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void generateId() {
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        if (type == MESSAGE) {
            this.type = MESSAGE;
        } else if (type == TASK) {
            this.type = TASK;
        }
    }

    public String getDtoType() {
        return dtoType;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
