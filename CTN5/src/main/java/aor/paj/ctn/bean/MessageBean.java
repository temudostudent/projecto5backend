package aor.paj.ctn.bean;

import aor.paj.ctn.dao.MessageDao;
import aor.paj.ctn.dto.Message;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.MessageEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class MessageBean {

    private static final Logger logger = LogManager.getLogger(EmailService.class);
    @EJB
    private MessageDao messageDao;
    @EJB
    private UserBean userBean;

    public void sendMessage(Message message, String token) {
        User sender = userBean.convertEntityByToken(token);
        if (sender == null) {
            logger.error("Non existent token tried to send a message");
            throw new RuntimeException("Invalid token");
        }
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setSender(userBean.convertUserDtotoUserEntity(sender));
        messageEntity.setRecipient(userBean.convertUserDtotoUserEntity(message.getReceiver()));
        messageEntity.setMessageContent(message.getContent());
        messageEntity.setTimestamp(LocalDate.now());
        messageEntity.setReadStatus(false);
        messageDao.persist(messageEntity);
    }

    public List<Message> getMessages(String receiver) {
        return messageDao.findMessagesByReceiver(receiver).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private Message convertToDto(MessageEntity messageEntity) {
        Message message = new Message();
        message.setId(messageEntity.getId().toString());
        message.setSender(userBean.convertUserEntitytoUserDto(messageEntity.getSender()));
        message.setReceiver(userBean.convertUserEntitytoUserDto(messageEntity.getRecipient()));
        message.setContent(messageEntity.getMessageContent());
        message.setReadStatus(messageEntity.isReadStatus());
        message.setTimestamp(messageEntity.getTimestamp());
        return message;
    }

    public MessageEntity convertToEntity(Message message) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setSender(userBean.convertUserDtotoUserEntity(message.getSender()));
        messageEntity.setRecipient(userBean.convertUserDtotoUserEntity(message.getReceiver()));
        messageEntity.setMessageContent(message.getContent());
        messageEntity.setTimestamp(message.getTimestamp());
        messageEntity.setReadStatus(message.isReadStatus());
        return messageEntity;
    }
}