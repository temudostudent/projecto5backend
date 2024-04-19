package aor.paj.ctn.dao;

import aor.paj.ctn.entity.MessageEntity;
import aor.paj.ctn.entity.NotificationEntity;
import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.Stateless;

import java.util.ArrayList;

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

    public ArrayList<NotificationEntity> findNotificationsUnreadedByReceiver(Boolean readStatus, String username) {
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
}
