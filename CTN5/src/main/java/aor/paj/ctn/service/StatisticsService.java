package aor.paj.ctn.service;

import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.StatisticsBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.OverallStatistics;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.dto.UserStatistics;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/statistics")
public class StatisticsService {

    @Inject
    UserBean userBean;
    @Inject
    StatisticsBean statsBean;
    @Inject
    NotificationBean notificationBean;

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countUsers(@HeaderParam("token") String token,
                             @QueryParam("type") Integer typeOfUser){
        Response response;

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            OverallStatistics stat=null;

            if (typeOfUser!=null){
                //Conta apenas o tipo de users pesquisado
                stat=statsBean.countUsersByType(typeOfUser);
            }else {
                //Conta todos os users + users por tipo
                stat = statsBean.countAllUsers();
            }

            response = Response.status(200).entity(stat).build();
        }
        return response;
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countTasks(@HeaderParam("token") String token,
                             @QueryParam("username") String username,
                             @QueryParam("state") Integer stateId) {
        Response response;

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {

            UserStatistics stat=null;

            if (username != null && stateId != null) {
                stat = statsBean.countTasksFromUserByType(username, stateId);
            } else if (username != null) {
                stat = statsBean.countAllTasksFromUser(username);
            }  else {
                //Conta todos as tasks + tasks por state
                stat = statsBean.countAllTasksOvr();
            }
            response = Response.status(200).entity(stat).build();
        }
        return response;
    }

    @GET
    @Path("/notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response countUnreadNotifications(@HeaderParam("token") String token){
        Response response;

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            Integer notifications=0;
            User user= userBean.convertEntityByToken(token);

            notifications = notificationBean.countUnreadedNotifications(user);

            response = Response.status(200).entity(notifications).build();
        }
        return response;
    }


}
