package aor.paj.ctn.bean;

import aor.paj.ctn.dao.MessageDao;
import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dto.Message;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.MessageEntity;
import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class MessageBean {

    private static final Logger logger = LogManager.getLogger(EmailService.class);
    @EJB
    private MessageDao messageDao;
    @EJB
    private UserDao userDao;
    @EJB
    private UserBean userBean;

    public void sendMessage(Message message, String token, User to) {
        User sender = userBean.convertEntityByToken(token);
        if (sender == null) {
            logger.error("Non existent token tried to send a message");
            throw new RuntimeException("Invalid token");
        }
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setSender(userBean.convertUserDtotoUserEntity(sender));
        messageEntity.setRecipient(userBean.convertUserDtotoUserEntity(to));
        messageEntity.setMessageContent(message.getContent());
        messageEntity.setTimestamp(LocalDateTime.now());
        messageEntity.setReadStatus(false);
        messageDao.persist(messageEntity);
    }

    public List<Message> getMessages(String receiver) {
        UserEntity recipient = userDao.findUserByUsername(receiver);

        if (recipient!=null){
            return messageDao.findMessagesByReceiver((recipient)).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }else
            return null;
    }

    public List<Message> getMessagesBetweenTwoUsers(String username1, String username2) {
        UserEntity user1 = userDao.findUserByUsername(username1);
        UserEntity user2 = userDao.findUserByUsername(username2);

        if (user1 == null || user2 == null) {
            logger.error("Non existent user tried to get messages between two users");
            throw new RuntimeException("Invalid user");
        } else if (user1.equals(user2)) {
            logger.error("User tried to get messages between himself");
            throw new RuntimeException("Invalid user");
        } else {
            return messageDao.findMessagesBetweenTwoUsers((user1), (user2)).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

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