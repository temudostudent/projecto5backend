package aor.paj.ctn.bean;

import aor.paj.ctn.dao.NotificationDao;
import aor.paj.ctn.dto.Message;
import aor.paj.ctn.dto.Notification;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.MessageEntity;
import aor.paj.ctn.entity.NotificationEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDate;

@Stateless
public class NotificationBean {

    @EJB
    private NotificationDao notificationDao;
    @EJB
    private UserBean userBean;

    public void sendNotification(User receiver, String token) {

        User sender = userBean.convertEntityByToken(token);
        if (sender == null) {
            throw new RuntimeException("Invalid token");
        }
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setSender(userBean.convertUserDtotoUserEntity(sender));
        notificationEntity.setRecipient(userBean.convertUserDtotoUserEntity(receiver));
        notificationEntity.setTimestamp(LocalDate.now());
        notificationEntity.setReadStatus(false);
        notificationDao.persist(notificationEntity);
    }

    public Integer countUnreadedNotifications(User recipient) {
        return notificationDao.countUserNotificationsUnreaded(userBean.convertUserDtotoUserEntity(recipient));
    }
}
