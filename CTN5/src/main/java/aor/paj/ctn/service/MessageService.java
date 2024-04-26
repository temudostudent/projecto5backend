package aor.paj.ctn.service;

import aor.paj.ctn.bean.MessageBean;
import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.Message;
import aor.paj.ctn.dto.Notification;
import aor.paj.ctn.dto.User;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

@Path("/message")
public class MessageService {

    private static final Logger logger = LogManager.getLogger(MessageService.class);
    @Inject
    MessageBean messageBean;
    @Inject
    NotificationBean notificationBean;
    @Inject
    UserBean userBean;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response send(Message message, @HeaderParam("token") String token, @QueryParam("to") String to) {

        boolean auth = userBean.isAuthenticated(token);
        User userTo = userBean.getUser(to);

        Response response;

        if (auth) {
            // Check if the recipient user exists
            if (userTo == null) {
                return Response.status(404).entity("Recipient user does not exist").build();
            }

            Long messageId = messageBean.sendMessage(message, token, userTo);
            notificationBean.sendNotification(userTo, token, Notification.MESSAGE, null);
            response = Response.status(200).entity(messageId).build();

            // Log the author, IP, and date
            String author = userBean.convertEntityByToken(token).getUsername();
            String ip = request.getRemoteAddr();
            LocalDateTime now = LocalDateTime.now();
            logger.info("Message sent by author: " + author + ", from IP: " + ip + ", at: " + now);
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveMessagesBetweenTwoUsers(@HeaderParam("token") String token,
                            @QueryParam("user1") String username1,
                            @QueryParam("user2") String username2){

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get messages between two users from IP: " + ip + ", at: " + now);


        if (username1 == null || username1.isEmpty() || username2 == null || username2.isEmpty()) {
            return Response.status(400).entity("Both user1 and user2 must be provided").build();
        }

        boolean auth = userBean.isAuthenticated(token);

        if (!auth) {
            return Response.status(401).entity("Invalid credentials").build();
        }

        List<Message> messages = messageBean.getMessagesBetweenTwoUsers(username1, username2);

        if (messages == null) {
            return Response.status(404).entity("Users not found").build();
        }

        return Response.status(200).entity(messages).build();
    }

    @GET
    @Path("/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveLastUnreadedMessages(@HeaderParam("token") String token,
                            @QueryParam("user1") String username1,
                            @QueryParam("user2") String username2){

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get latest unread messages between two users from IP: " + ip + ", at: " + now);


        if (username1 == null || username1.isEmpty() || username2 == null || username2.isEmpty()) {
            return Response.status(400).entity("Both user1 and user2 must be provided").build();
        }

        boolean auth = userBean.isAuthenticated(token);

        if (!auth) {
            return Response.status(401).entity("Invalid credentials").build();
        }

        List<Message> messages = messageBean.getLastUnreadMessagesBetweenTwoUsers(username1, username2);

        if (messages == null) {
            return Response.status(404).entity("Users not found").build();
        }

        return Response.status(200).entity(messages).build();
    }

    @GET
    @Path("/receive/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receive(@HeaderParam("token") String token, @PathParam("username") String username) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get messages for user: " + username + ", from IP: " + ip + ", at: " + now);


        boolean auth = userBean.isAuthenticated(token);
        Response response;

        if (auth) {
            List<Message> messages = messageBean.getMessages(username);
            response = Response.status(200).entity(messages).build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/read")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAllMessagesRead(@HeaderParam("token") String token,
                                       @QueryParam("user1") String username1,
                                       @QueryParam("user2") String username2) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to set all messages as read between two users by User: " + username1 + " from IP: " + ip + ", at: " + now);


        boolean auth = userBean.isAuthenticated(token);
        String authenticatedUsername = userBean.convertEntityByToken(token).getUsername();

        if (!auth) {
            return Response.status(401).entity("Invalid credentials").build();
        }

        if (username1 == null || username1.isEmpty() || username2 == null || username2.isEmpty()) {
            return Response.status(400).entity("Both user1 and user2 must be provided").build();
        }

        try {
            messageBean.setAllMessagesFromConversationRead(authenticatedUsername, username1, username2);
            return Response.status(200).entity("All messages from the conversation have been set to read").build();
        } catch (RuntimeException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }
}
