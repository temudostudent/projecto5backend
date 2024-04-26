package aor.paj.ctn.bean;

import aor.paj.ctn.dao.MessageDao;
import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dto.Message;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.MessageEntity;
import aor.paj.ctn.entity.UserEntity;
import aor.paj.ctn.websocket.ChatEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class MessageBean {

    private static final Logger logger = LogManager.getLogger(MessageBean.class);
    @EJB
    private MessageDao messageDao;
    @EJB
    private UserDao userDao;
    @EJB
    private UserBean userBean;

    @Inject
    ChatEndpoint chatEndpoint;
    @Inject
    ObjectMapper objectMapper;

    public Long sendMessage(Message message, String token, User receiver) {
        User sender = userBean.convertEntityByToken(token);
        if (sender == null) {
            logger.error("Non existent token tried to send a message");
            throw new RuntimeException("Invalid token");
        }
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setSender(userBean.convertUserDtotoUserEntity(sender));
        messageEntity.setRecipient(userBean.convertUserDtotoUserEntity(receiver));
        messageEntity.setMessageContent(message.getContent());
        messageEntity.setTimestamp(LocalDateTime.now());

        String receiverToken = userBean.findTokenByUsername(receiver.getUsername());

        // Check if the receiver's WebSocket session is open before sending the notification
        if (receiverToken != null && chatEndpoint.isSessionOpen(receiverToken)) {
            // Create a new Message DTO
            Message messageInst = new Message();
            messageInst.setSender(sender);
            messageInst.setReceiver(receiver);
            messageInst.setContent(message.getContent());
            messageInst.setTimestamp(messageEntity.getTimestamp());
            messageInst.setReadStatus(true); // Set readStatus to true if the ChatEndpoint session is open

            // Convert the Message DTO to a JSON string
            String messageJson;
            try {
                if (messageInst != null) {
                    messageJson = objectMapper.writeValueAsString(messageInst);
                } else {
                    throw new RuntimeException("Message is null");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error converting Message to JSON", e);
            }

            chatEndpoint.onMessage(messageJson, token, receiver.getUsername());
            messageEntity.setReadStatus(true);
            messageEntity.setReadTimestamp(LocalDateTime.now());
        } else {
            messageEntity.setReadStatus(false); // Set readStatus to false if the ChatEndpoint session is not open

        }
        messageDao.persist(messageEntity);
        return messageEntity.getId(); // return the ID of the persisted message entity
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

    public List<Message> getLastUnreadMessagesBetweenTwoUsers(String username1, String username2) {
        UserEntity user1 = userDao.findUserByUsername(username1);
        UserEntity user2 = userDao.findUserByUsername(username2);
        List<Message> lastMessages= new ArrayList<>();

        if (user1 == null || user2 == null) {
            logger.error("Non existent user tried to get messages between two users");
            throw new RuntimeException("Invalid user");
        } else if (user1.equals(user2)) {
            logger.error("User tried to get messages between himself");
            throw new RuntimeException("Invalid user");
        } else {
            List<MessageEntity> messages = messageDao.findMessagesBetweenTwoUsers(user1, user2);
            for (MessageEntity message : messages) {
                if (!message.isReadStatus()) {
                    lastMessages.add(convertToDto(message));
                }
            }
        }
        return lastMessages;
    }

    public void setAllMessagesFromConversationRead(String authenticatedUsername, String username1, String username2) {
        UserEntity authenticatedUser = userDao.findUserByUsername(authenticatedUsername);
        UserEntity user1 = userDao.findUserByUsername(username1);
        UserEntity user2 = userDao.findUserByUsername(username2);

        String senderToken = userBean.findTokenByUsername(username2);

        if (authenticatedUser == null || user1 == null || user2 == null) {
            logger.error("Non existent user tried to set messages as read");
            throw new RuntimeException("Invalid user");
        } else if (user1.equals(user2)) {
            logger.error("User tried to set messages with himself as read");
            throw new RuntimeException("Invalid user");
        } else {
            List<MessageEntity> messages = messageDao.findMessagesBetweenTwoUsers(user1, user2);
            for (MessageEntity message : messages) {
                if (!message.isReadStatus() && message.getRecipient().equals(authenticatedUser)) {
                    message.setReadStatus(true);
                    message.setReadTimestamp(LocalDateTime.now());
                    messageDao.merge(message);

                    // Check if the receiver's WebSocket session is open before sending the notification
                    if (senderToken != null && chatEndpoint.isSessionOpen(senderToken)) {
                        // Create a new Message DTO
                        Message messageInst = new Message();
                        messageInst.setId(message.getId().toString());
                        messageInst.setReadNow(true);

                        // Convert the Message DTO to a JSON string
                        String messageJson;
                        try {
                            if (messageInst != null) {
                                messageJson = objectMapper.writeValueAsString(messageInst);
                            } else {
                                throw new RuntimeException("Message is null");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Error converting Message to JSON", e);
                        }

                        chatEndpoint.sendMessage(senderToken, messageJson);
                    }
                }
            }
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