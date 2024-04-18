package aor.paj.ctn.service;

import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.Notification;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

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
                Map<String, String> notifications = notificationBean.getUnreadNotificationsByTypeForUser(username);
                response = Response.status(200).entity(notifications).build();
            } else if (status == true) {
                // Get notifications readed
                response = null;
            }

        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }
}
