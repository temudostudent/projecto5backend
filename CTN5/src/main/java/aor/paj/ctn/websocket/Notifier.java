package aor.paj.ctn.websocket;

import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.TaskBean;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Singleton
@ServerEndpoint("/websocket/notifier/{token}")
public class Notifier {

    @EJB
    private NotificationBean notificationBean;
    @EJB
    private TaskBean taskBean;

    HashMap<String, Session> sessions = new HashMap<String, Session>();


    @OnOpen
    public void toDoOnOpen (Session session, @PathParam("token") String token) {
        System.out.println("A new WebSocket session is opened for client with token: "+ token);
        sessions.put(token,session);
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason){
        System.out.println("Websocket session is closed with CloseCode: "+ reason.getCloseCode() + ": "+reason.getReasonPhrase());

        Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Session> entry = iterator.next();
            if (entry.getValue().equals(session)) {
                System.out.println(entry.getKey() + " is removed from the session list");
                iterator.remove();
            }
        }
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg){
        System.out.println("A new message is received: "+ msg);
        try {
            session.getBasicRemote().sendText("ack");
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }

    public void send(String token, String msg) {
        Session session = sessions.get(token);
        if (session != null){
            System.out.println("sending........" + msg);
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            }
        }
    }

    public void sendToAll(String msg) {
        sessions.values().forEach(session -> {
            if (session.isOpen()){

                try {
                    session.getBasicRemote().sendText(msg);
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        });
    }

    public void sendToAllExcept(String msg, String token) {
        Session excludedSession = sessions.get(token);
        sessions.values().forEach(session -> {
            if (session != excludedSession && session.isOpen()){
                System.out.println("sending........" + msg);
                try {
                    session.getBasicRemote().sendText(msg);
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        });
    }

    public boolean isSessionOpen(String token) {
        Session session = sessions.get(token);
        return session != null && session.isOpen();
    }
}
