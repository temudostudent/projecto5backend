package aor.paj.ctn.service;

import aor.paj.ctn.bean.TaskBean;
import aor.paj.ctn.bean.UserBean;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

@Path("/token")
public class TokenService {

    private static final Logger logger = LogManager.getLogger(TokenService.class);
    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;
    @Context
    private HttpServletRequest request;

    @GET
    @Path("/reset-password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkResetTokenValidation(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to check reset token validation from IP: " + ip + ", at: " + now);

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

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to check confirmation token validation from IP: " + ip + ", at: " + now);

        Response response;

        if (!userBean.isConfirmationTokenValid(token)) {
            response = Response.status(401).entity("Token Invalid").build();
        } else {
            response = Response.status(200).entity(true).build();
        }
        return response;
    }
}
