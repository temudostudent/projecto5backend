package aor.paj.ctn.service;

import aor.paj.ctn.bean.TaskBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/token")
public class TokenService {

    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;

    @GET
    @Path("/reset-password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkResetTokenValidation(@HeaderParam("token") String token) {
        Response response;

        if (!userBean.isResetTokenValid(token)) {
            response = Response.status(401).entity("Token Invalid").build();
        } else {
            response = Response.status(200).entity(true).build();
        }
        return response;
    }

    @GET
    @Path("/confirmation-account")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkConfirmationTokenValidation(@HeaderParam("token") String token) {
        Response response;

        if (!userBean.isConfirmationTokenValid(token)) {
            response = Response.status(401).entity("Token Invalid").build();
        } else {
            response = Response.status(200).entity(true).build();
        }
        return response;
    }
}
