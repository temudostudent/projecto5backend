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

@Singleton
@ServerEndpoint("/websocket/notifier/{token}")
public class Notifier {

    @EJB
    private NotificationBean notificationBean;
    @EJB
    private TaskBean taskBean;

    HashMap<String, Session> sessions = new HashMap<String, Session>();
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


    @OnOpen
    public void toDoOnOpen (Session session, @PathParam("token") String token) {
        System.out.println("A new WebSocket session is opened for client with token: "+ token);
        sessions.put(token,session);
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason){
        System.out.println("Websocket session is closed with CloseCode: "+ reason.getCloseCode() + ": "+reason.getReasonPhrase());
        for(String key:sessions.keySet()){
            if(sessions.get(key) == session)
                System.out.println(key + " is removed from the session list");
                sessions.remove(key);
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

   /*@OnMessage
    public void handleTaskStatusUpdate(Session session, String message) {
        // Parse the message to get the task ID and new status
        // This assumes the message is in the format "taskId:status"
        String[] parts = message.split(":");
        String taskId = parts[0];
        int status = Integer.parseInt(parts[1]);

        // Update the task status
        boolean updated = taskBean.updateTaskStatus(taskId, status);

        // Send a response back to the client
        try {
            if (updated) {
                session.getBasicRemote().sendText("Task status updated successfully");
            } else {
                session.getBasicRemote().sendText("Failed to update task status");
            }
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }*/

    public boolean isSessionOpen(String token) {
        Session session = sessions.get(token);
        return session != null && session.isOpen();
    }
}
