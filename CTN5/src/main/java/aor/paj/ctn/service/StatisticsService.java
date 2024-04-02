package aor.paj.ctn.service;

import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/statistics")
public class StatisticsService {

    @Inject
    UserBean userBean;

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@HeaderParam("token") String token) {
        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        int numberUsers = userBean.allUsers();

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(numberUsers).build();
        }
        return response;
    }


}
