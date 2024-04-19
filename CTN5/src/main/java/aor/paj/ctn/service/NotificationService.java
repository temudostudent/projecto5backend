package aor.paj.ctn.service;

import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.Notification;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/notification")
public class NotificationService {

    @Inject
    NotificationBean notificationBean;
    @Inject
    UserBean userBean;

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receive(@HeaderParam("token") String token,
                            @QueryParam("readed") Boolean status) {

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
    public Response receive(@HeaderParam("token") String token,
                            @PathParam("username") String username) {

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

    @PUT
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markAllAsRead(@HeaderParam("token") String token) {
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
}
