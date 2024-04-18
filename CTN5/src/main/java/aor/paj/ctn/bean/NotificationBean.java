package aor.paj.ctn.bean;

import aor.paj.ctn.dao.NotificationDao;
import aor.paj.ctn.dto.Message;
import aor.paj.ctn.dto.Notification;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.NotificationEntity;
import aor.paj.ctn.entity.UserEntity;
import aor.paj.ctn.websocket.Notifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class NotificationBean {

    @EJB
    private NotificationDao notificationDao;
    @EJB
    private UserBean userBean;
    @Inject
    Notifier notifier;

    public void sendNotification(User receiver, String token, String type) {

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
        notificationDao.persist(notificationEntity);

        String receiverToken = userBean.findTokenByUsername(receiver.getUsername());

        // Check if the receiver's WebSocket session is open before sending the notification
        if (receiverToken != null && notifier.isSessionOpen(receiverToken)) {
            notifier.send(receiverToken, sender.getUsername() + " sent you a new message");
        }
    }

    public Integer countUnreadedNotifications(User recipient) {
        return notificationDao.countUserNotificationsUnreaded(userBean.convertUserDtotoUserEntity(recipient));
    }

    /*public void sendUnreadNotifications(String token) {
        User user = userBean.convertEntityByToken(token);
        if (user == null) {
            throw new RuntimeException("Invalid token");
        }
        List<NotificationEntity> unreadNotifications = notificationDao.findNotificationsUnreadedByReceiver(userBean.convertUserDtotoUserEntity(user).getUsername());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        for (NotificationEntity notificationEntity : unreadNotifications) {
            Notification notification = convertToDto(notificationEntity);
            try {
                String notificationJson = mapper.writeValueAsString(notification);
                notifier.send(token, notificationJson);
            } catch (Exception e) {
                throw new RuntimeException("Error sending notification", e);
            }
        }
    }*/

    private Notification convertToDto(NotificationEntity notificationEntity) {
        Notification notification = new Notification();
        notification.setId(notificationEntity.getId().toString());
        notification.setSender(userBean.convertUserEntitytoUserDto(notificationEntity.getSender()));
        notification.setReceiver(userBean.convertUserEntitytoUserDto(notificationEntity.getRecipient()));
        notification.setReadStatus(notificationEntity.isReadStatus());
        notification.setTimestamp(notificationEntity.getTimestamp());
        return notification;
    }

    public List<Notification> findNotificationsUnreadedByReceiver(String username) {

        if (username != null) {
            return notificationDao.findNotificationsUnreadedByReceiver(username).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public Map<String, String> getUnreadNotificationsByTypeForUser(String username) {
        try {
            List<Notification> notifications = findNotificationsUnreadedByReceiver(username);
            if (notifications != null) {
                return notifications.stream()
                        .filter(notification -> notification != null && notification.getSender() != null)
                        .collect(Collectors.toMap(
                                notification -> notification.getSender().getUsername(), // Key is sender's username
                                Notification::getType, // Value is notification type
                                (existingValue, newValue) -> existingValue + ", " + newValue)); // If there are multiple notifications from the same sender, concatenate the types
            } else {
                return null;
            }
        } catch (Exception e) {
            // Log the exception here
            System.err.println("Error in getUnreadNotificationsByTypeForUser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
