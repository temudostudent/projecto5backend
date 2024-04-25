package aor.paj.ctn.dao;

import aor.paj.ctn.entity.MessageEntity;
import aor.paj.ctn.entity.NotificationEntity;
import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity>{

    private static final long serialVersionUID = 1L;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public ArrayList<NotificationEntity> findAllNotificationsByReceiver(String username) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findNotificationsByReceiver")
                    .setParameter("username", username)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<NotificationEntity> findNotificationsFromReadStatusByReceiver(Boolean readStatus, String username) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findUserNotificationsByReadStatus")
                    .setParameter("readStatus", readStatus)
                    .setParameter("username", username)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }

    public Integer countUserNotificationsUnreaded(UserEntity recipient) {
        try {
            return ((Number) em.createNamedQuery("Notification.countUserNotificationsByReadStatus")
                    .setParameter("readStatus", false)
                    .setParameter("recipient", recipient)
                    .getSingleResult()).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<NotificationEntity> findNotificationsBySenderAndReceiver(Boolean readStatus, String senderUsername, String receiverUsername) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findNotificationsBySenderAndReceiver")
                    .setParameter("readStatus", readStatus)
                    .setParameter("senderUsername", senderUsername)
                    .setParameter("receiverUsername", receiverUsername)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<NotificationEntity> findLatestNotificationFromEachSenderByReceiver(String receiverUsername) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findLatestFromEachSenderByReceiver")
                    .setParameter("receiverUsername", receiverUsername)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<NotificationEntity> findLatestFromEachSenderByReceiverAndType(String receiverUsername, int type) {
        try {
            return (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findLatestFromEachSenderByReceiverAndType")
                    .setParameter("receiverUsername", receiverUsername)
                    .setParameter("type", type)
                    .getResultList();
        } catch (Exception e) {
            return new ArrayList<>(); // return an empty list instead of null
        }
    }

    public List<NotificationEntity> findLatestFromEachSenderByReceiverAndTypes(String receiverUsername) {
        List<NotificationEntity> type10Notifications = em.createNamedQuery("Notification.findLatestFromEachSenderByReceiverAndType10")
                .setParameter("receiverUsername", receiverUsername)
                .getResultList();

        List<NotificationEntity> type20Notifications = em.createNamedQuery("Notification.findLatestFromEachSenderByReceiverAndType20")
                .setParameter("receiverUsername", receiverUsername)
                .getResultList();

        type10Notifications.addAll(type20Notifications);

        // Sort the combined list by timestamp in descending order
        type10Notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

        return type10Notifications;
    }


}
