package aor.paj.ctn.dao;

import aor.paj.ctn.entity.MessageEntity;
import jakarta.ejb.Stateless;

import java.util.ArrayList;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    private static final long serialVersionUID = 1L;

    public MessageDao() {
        super(MessageEntity.class);
    }

    public ArrayList<MessageEntity> findMessagesBySender(String sender) {
        try {
            return (ArrayList<MessageEntity>) em.createNamedQuery("Message.findMessagesBySender").setParameter("sender", sender)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<MessageEntity> findMessagesByReceiver(String recipient) {
        try {
            return (ArrayList<MessageEntity>) em.createNamedQuery("Message.findMessagesByReceiver").setParameter("recipient", recipient)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<MessageEntity> findMessagesByReadStatus(Boolean readStatus) {
        try {
            return (ArrayList<MessageEntity>) em.createNamedQuery("Message.findMessagesByReadStatus").setParameter("readStatus", readStatus)
                    .getResultList();

        } catch (Exception e) {
            return null;
        }
    }
}
