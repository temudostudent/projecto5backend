package aor.paj.ctn.service;

import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.Notification;
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

@Path("/notification")
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);
    @Inject
    NotificationBean notificationBean;
    @Inject
    UserBean userBean;
    @Context
    private HttpServletRequest request;


    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receive(@HeaderParam("token") String token,
                            @QueryParam("readed") Boolean status) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get notifications from IP: " + ip + ", at: " + now);

        boolean auth = userBean.isAuthenticated(token);
        String username = userBean.convertEntityByToken(token).getUsername();
        Response response = null;

        if (auth) {
            if (status == false) {
                // Get all unread notifications
                List<Notification> notifications = notificationBean.findNotificationsUnreadedByReceiver(username);
                response = Response.status(200).entity(notifications).build();
            } else if (status == true) {
                // Get notifications readed
                response = null;
            } else if (status  == null) {
                // Get all notifications
                List<Notification> notifications = notificationBean.findAllNotificationsByReceiver(username);
                response = Response.status(200).entity(notifications).build();
            }

        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUserNotifications(@HeaderParam("token") String token,
                            @PathParam("username") String username) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all notifications for user: " + username + ", from IP: " + ip + ", at: " + now);

        boolean auth = userBean.isAuthenticated(token);
        String usernameToken = userBean.convertEntityByToken(token).getUsername();
        Response response = null;

        if (auth) {
            if (usernameToken.equals(username)) {
                // Get all notifications
                List<Notification> notifications = notificationBean.findAllNotificationsByReceiver(username);
                response = Response.status(200).entity(notifications).build();
            }

        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/{username}/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestUserNotifications(@HeaderParam("token") String token,
                                            @PathParam("username") String username) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get latest notifications for user: " + username + ", from IP: " + ip + ", at: " + now);

        boolean auth = userBean.isAuthenticated(token);
        String usernameToken = userBean.convertEntityByToken(token).getUsername();
        Response response = null;

        if (auth) {
            if (usernameToken.equals(username)) {
                // Get all notifications
                List<Notification> notifications = notificationBean.findLatestNotificationsByReceiver(username);
                response = Response.status(200).entity(notifications).build();
            }

        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/{username}/latest/type")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestUserNotificationsByType(@HeaderParam("token") String token,
                                               @PathParam("username") String username,
                                                     @QueryParam("type") int type) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get latest notifications by type for user: " + username + ", from IP: " + ip + ", at: " + now);

        boolean auth = userBean.isAuthenticated(token);
        String usernameToken = userBean.convertEntityByToken(token).getUsername();
        Response response = null;

        if (auth) {
            if (usernameToken.equals(username)) {
                // Get all notifications
                List<Notification> notifications = notificationBean.findLatestFromEachSenderByReceiverAndType(username, type);
                response = Response.status(200).entity(notifications).build();
            }

        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markAllAsRead(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to mark all notifications as read from IP: " + ip + ", at: " + now);

        boolean auth = userBean.isAuthenticated(token);
        String username = userBean.convertEntityByToken(token).getUsername();
        Response response = null;

        if (auth) {
            notificationBean.setAllNotificationsFromUserToReaded(username);
            response = Response.status(200).entity("All notifications marked as read").build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/read/{senderUsername}/{receiverUsername}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markAllFromSenderToReceiverAsRead(@HeaderParam("token") String token,
                                                      @PathParam("senderUsername") String senderUsername,
                                                      @PathParam("receiverUsername") String receiverUsername,
                                                      @QueryParam("type") int type){

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to mark all notifications from sender: " + senderUsername + " to receiver: " + receiverUsername + " as read from IP: " + ip + ", at: " + now);

        boolean auth = userBean.isAuthenticated(token);
        Response response = null;

        if (auth) {
            notificationBean.setNotificationsFromSenderToReceiverReaded(senderUsername, receiverUsername, type);
            response = Response.status(200).entity("All notifications from sender to receiver marked as read").build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }
}
