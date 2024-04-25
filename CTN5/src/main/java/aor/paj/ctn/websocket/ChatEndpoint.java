package aor.paj.ctn.websocket;

import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.bean.EmailService;
import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.dto.Notification;
import aor.paj.ctn.dto.User;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

@Singleton
@ServerEndpoint(value= "/websocket/chat/{senderToken}/{receiverUsername}")
public class ChatEndpoint {
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    @Inject
    NotificationBean notificationBean;
    @Inject
    UserBean userBean;


    HashMap<String, Session> chatSessions = new HashMap<>();
    @OnOpen
    public void onOpen(Session session, @PathParam("senderToken") String senderToken, @PathParam("receiverUsername") String receiverUsername) {
        // When a user opens the chatroom, add their session to the map.
        chatSessions.put(senderToken, session);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("senderToken") String senderToken, @PathParam("receiverUsername") String receiverUsername) {
        String receiverToken = userBean.findTokenByUsername(receiverUsername);
        // Check if the recipient's chatroom is open.
        Session receiverSession = chatSessions.get(receiverToken);
        if (receiverSession != null) {
            // If it is, send the message via WebSocket.
            sendMessage(receiverToken, message);
        } else {
            // If it's not, send a notification instead.
            User sender = userBean.convertEntityByToken(senderToken);
            User receiver = userBean.convertEntityByToken(receiverToken);
            if (sender != null && receiver != null) {
                notificationBean.sendNotification(receiver, senderToken, Notification.MESSAGE, null);
            } else {
                logger.error("Invalid sender or receiver token");
            }
        }
    }

    @OnClose
    public void OnClose(Session session, @PathParam("senderToken") String senderToken, @PathParam("receiverToken") String receiverToken, CloseReason reason){
        System.out.println("Websocket chatSession is closed with CloseCode: "+ reason.getCloseCode() + ": "+reason.getReasonPhrase());
        // When a user closes the chatroom, remove their session from the map.
        chatSessions.remove(senderToken);
    }


    public boolean isSessionOpen(String token) {
        Session session = chatSessions.get(token);
        return session != null && session.isOpen();
    }

    public void sendMessage(String receiverToken, String message) {

        Session receiverSession = chatSessions.get(receiverToken);

        if (receiverSession != null) {
            if(receiverSession.isOpen()) {
                try {
                    receiverSession.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Receiver's token is not active.");
            }
        } else {
            System.out.println("Receiver's token does not exist.");
        }
    }

}
