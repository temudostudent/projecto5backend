package aor.paj.ctn.service;

import aor.paj.ctn.bean.MessageBean;
import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.Message;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/message")
public class MessageService {

    @Inject
    MessageBean messageBean;
    @Inject
    NotificationBean notificationBean;
    @Inject
    UserBean userBean;

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response send(Message message, @HeaderParam("token") String token) {

        boolean auth = userBean.isAuthenticated(token);
        Response response;

        if (auth) {
            messageBean.sendMessage(message, token);
            notificationBean.sendNotification(message.getReceiver(), token);
            response = Response.status(200).entity("Message sent successfully").build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/receive/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receive(@HeaderParam("token") String token, @PathParam("username") String username) {

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
}
