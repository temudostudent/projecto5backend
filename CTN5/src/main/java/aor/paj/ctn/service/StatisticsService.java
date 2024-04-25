package aor.paj.ctn.service;

import aor.paj.ctn.bean.CategoryBean;
import aor.paj.ctn.bean.NotificationBean;
import aor.paj.ctn.bean.StatisticsBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.Category;
import aor.paj.ctn.dto.OverallStatistics;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.dto.UserStatistics;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/statistics")
public class StatisticsService {

    @Inject
    UserBean userBean;
    @Inject
    StatisticsBean statsBean;
    @Inject
    CategoryBean categoryBean;
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
                stat = statsBean.countAllUsers(token);
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
                stat = statsBean.countAllTasksOvr(token);
            }
            response = Response.status(200).entity(stat).build();
        }
        return response;
    }

    @GET
    @Path("/notifications/unreaded")
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

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token") String token) {

        Response response;

        if (userBean.isAuthenticated(token)) {
            try {
                List<Category> allCategories = categoryBean.findAllCategoriesByTaskFrequency();
                response = Response.status(200).entity(allCategories).build();
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The categories were not found.").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


}
