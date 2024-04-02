package aor.paj.ctn.service;

import aor.paj.ctn.bean.StatisticsBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.User;
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

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {
        Response response;

        int count = statsBean.countAllUsers();

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(count).build();
        }
        return response;
    }

    @GET
    @Path("/users/type")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByType(@HeaderParam("token") String token, @QueryParam("type") int typeOfUser) {
        Response response;

        int count = statsBean.countAllUsersByType(typeOfUser);

        if (userBean.isAuthenticated(token)) {
            response = Response.status(200).entity(count).build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/users/visible")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByVisibility(@HeaderParam("token") String token, @QueryParam("visible") boolean visible) {
        Response response;

        int count = statsBean.countAllUsersByVisibility(visible);

        if (userBean.isAuthenticated(token)) {
            response = Response.status(200).entity(count).build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasks(@HeaderParam("token") String token) {
        Response response;

        int count = statsBean.countAllTasks();

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(count).build();
        }
        return response;
    }

    @GET
    @Path("/tasks/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksFromUser(@HeaderParam("token") String token, @QueryParam("username") String username) {
        Response response;

        int count = statsBean.countAllTasksFromUser(username);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(count).build();
        }
        return response;
    }

    @GET
    @Path("/tasks/user/state")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksFromUserByState(@HeaderParam("token") String token, @QueryParam("username") String username, @QueryParam("state") int stateId) {
        Response response;

        int count = statsBean.countAllTasksFromUserByType(username,stateId);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(count).build();
        }
        return response;
    }


}
