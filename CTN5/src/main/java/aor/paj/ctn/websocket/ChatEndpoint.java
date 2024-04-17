package aor.paj.ctn.websocket;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

@ServerEndpoint(value= "/websocket/chat/{token}")
public class ChatEndpoint {

    HashMap<String, Session> sessions = new HashMap<String, Session>();
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        sessions.put(token, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Parse the message to get the senderToken, receiverToken, and the actual message
        JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();

        String receiverToken = jsonObject.getString("receiverToken");
        String msg = jsonObject.getString("message");
        sendMessage(receiverToken, msg);
    }

    @OnClose
    public void OnClose(Session session, CloseReason reason){
        System.out.println("Websocket session is closed with CloseCode: "+ reason.getCloseCode() + ": "+reason.getReasonPhrase());
        for(String key:sessions.keySet()){
            if(sessions.get(key) == session)
                sessions.remove(key);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Handle error
    }

    private void sendMessage(String receiverToken, String message) {
        Session receiverSession = sessions.get(receiverToken);
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
