package aor.paj.ctn.bean;

import aor.paj.ctn.dao.NotificationDao;
import aor.paj.ctn.dto.Notification;
import aor.paj.ctn.dto.Task;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.NotificationEntity;
import aor.paj.ctn.entity.TaskEntity;
import aor.paj.ctn.websocket.Notifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class NotificationBean {

    @EJB
    private NotificationDao notificationDao;
    @EJB
    private UserBean userBean;
    @EJB
    private TaskBean taskBean;
    @Inject
    Notifier notifier;
    @Inject
    ObjectMapper objectMapper;

    public void sendNotification(User receiver, String token, int type, TaskEntity taskEntity) {

        User sender = userBean.convertEntityByToken(token);
        if (sender == null) {
            throw new RuntimeException("Invalid token");
        }
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setSender(userBean.convertUserDtotoUserEntity(sender));
        notificationEntity.setRecipient(userBean.convertUserDtotoUserEntity(receiver));
        notificationEntity.setTimestamp(LocalDateTime.now());
        notificationEntity.setReadStatus(false);
        notificationEntity.setType(type);
        if (type == Notification.TASK) {
            notificationEntity.setTask(taskEntity);
        }
        notificationDao.persist(notificationEntity);

        String receiverToken = userBean.findTokenByUsername(receiver.getUsername());

        // Check if the receiver's WebSocket session is open before sending the notification
        if (receiverToken != null && notifier.isSessionOpen(receiverToken)) {
            // Create a new Notification DTO
            Notification notification = new Notification();
            notification.setSender(sender);
            notification.setTimestamp(notificationEntity.getTimestamp());
            notification.setType(type);

            if (type == Notification.MESSAGE) {
                notification.setTask(null);
            } else if (type == Notification.TASK) {
                notification.setTask(taskBean.convertTaskEntityToTaskDto(taskEntity));
            } else {
                throw new RuntimeException("Invalid notification type");
            }

            // Convert the Notification DTO to a JSON string

            String notificationJson;
            try {
                if (notification != null) {
                    notificationJson = objectMapper.writeValueAsString(notification);
                } else {
                    throw new RuntimeException("Notification is null");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error converting notification to JSON", e);
            }

            notifier.send(receiverToken, notificationJson);
        }
    }

    public Integer countUnreadedNotifications(User recipient) {
        return notificationDao.countUserNotificationsUnreaded(userBean.convertUserDtotoUserEntity(recipient));
    }

    private Notification convertToDto(NotificationEntity notificationEntity) {
        Notification notification = new Notification();
        notification.setId(notificationEntity.getId().toString());
        notification.setSender(userBean.convertUserEntitytoUserDto(notificationEntity.getSender()));
        notification.setReceiver(userBean.convertUserEntitytoUserDto(notificationEntity.getRecipient()));
        notification.setReadStatus(notificationEntity.isReadStatus());
        notification.setTimestamp(notificationEntity.getTimestamp());
        notification.setType(notificationEntity.getType());
        return notification;
    }

    public List<Notification> findNotificationsUnreadedByReceiver(String username) {

        if (username != null) {
            return notificationDao.findNotificationsFromReadStatusByReceiver(false, username).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<Notification> findAllNotificationsByReceiver(String username) {

        if (username != null) {
            return notificationDao.findAllNotificationsByReceiver(username).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public List<Notification> findLatestNotificationsByReceiver(String username) {

        if (username != null) {
            return notificationDao.findLatestNotificationFromEachSenderByReceiver(username).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public void setAllNotificationsFromUserToReaded(String username) {
        List<NotificationEntity> unreadNotifications = notificationDao.findNotificationsFromReadStatusByReceiver(false, username);
        for (NotificationEntity notification : unreadNotifications) {
            notification.setReadStatus(true);
            notification.setReadTimestamp(LocalDateTime.now());
            notificationDao.merge(notification);
        }
    }

    public void setNotificationsFromSenderToReceiverReaded(String senderUsername, String receiverUsername) {
        List<NotificationEntity> unreadNotifications = notificationDao.findNotificationsBySenderAndReceiver(false, senderUsername, receiverUsername);
        for (NotificationEntity notification : unreadNotifications) {
            notification.setReadStatus(true);
            notification.setReadTimestamp(LocalDateTime.now());
            notificationDao.merge(notification);
        }
    }
}
