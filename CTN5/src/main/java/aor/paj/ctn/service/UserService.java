package aor.paj.ctn.service;

import aor.paj.ctn.bean.CategoryBean;
import aor.paj.ctn.bean.TaskBean;
import aor.paj.ctn.bean.UserBean;
import aor.paj.ctn.dto.Category;
import aor.paj.ctn.dto.Login;
import aor.paj.ctn.dto.Task;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.util.SessionManager;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Path("/users")
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;
    @Inject
    CategoryBean categoryBean;
    @Inject
    SessionManager sessionManager;
    @Context
    private HttpServletRequest request;


    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Login login) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to login from IP: " + ip + ", at: " + now + ", with username: " + login.getUsername());

        String token = userBean.login(login);
        Response response;

        if (token != null) {
            sessionManager.userActivity(token);
            response = Response.status(200).entity(token).build();
        } else if(userBean.isUsernamePending(login.getUsername())){
            response = Response.status(404).entity("Username is pending").build();
        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to logout from IP: " + ip + ", at: " + now);


        if (userBean.logout(token)) return Response.status(200).entity("Logout Successful!").build();

        return Response.status(401).entity("Invalid Token!").build();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to register user from IP: " + ip + ", at: " + now);

        Response response;

        boolean isUsernameAvailable = userBean.isUsernameAvailable(user);
        boolean isEmailValid = userBean.isEmailValid(user);
        boolean isFieldEmpty = userBean.isAnyFieldEmpty(user);
        boolean isPhoneNumberValid = userBean.isPhoneNumberValid(user);

        if (isFieldEmpty) {
            response = Response.status(422).entity("There's an empty field. ALl fields must be filled in").build();
        } else if (!isEmailValid) {
            response = Response.status(422).entity("Invalid email").build();
        } else if (!isUsernameAvailable) {
            response = Response.status(Response.Status.CONFLICT).entity("Username already in use").build(); //status code 409
        } else if (!isPhoneNumberValid) {
            response = Response.status(422).entity("Invalid phone number").build();
        } else if (userBean.register(user)) {
            response = Response.status(Response.Status.CREATED).entity("User registered successfully").build(); //status code 201
        } else {
            response = Response.status(Response.Status.BAD_REQUEST).entity("Something went wrong").build(); //status code 400
        }
        return response;
    }

    @POST
    @Path("/register/pending")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPendingUser(User user,
                                        @HeaderParam("token") String token) {

        User productOwner = userBean.convertEntityByToken(token);
        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to register user from IP: " + ip + ", at: " + now + ", by Product Owner: " + productOwner.getUsername());

        Response response;

        if(userBean.userIsProductOwner(token)){
            boolean isUsernameAvailable = userBean.isUsernameAvailable(user);
            boolean isEmailValid = userBean.isEmailValid(user);
            boolean isFieldEmpty = userBean.isAnyFieldEmpty(user);
            boolean isPhoneNumberValid = userBean.isPhoneNumberValid(user);

            if (isFieldEmpty) {
                response = Response.status(422).entity("There's an empty field. ALl fields must be filled in").build();
            } else if (!isEmailValid) {
                response = Response.status(422).entity("Invalid email").build();
            } else if (!isUsernameAvailable) {
                response = Response.status(Response.Status.CONFLICT).entity("Username already in use").build(); //status code 409
            } else if (!isPhoneNumberValid) {
                response = Response.status(422).entity("Invalid phone number").build();
            } else if (userBean.registerPending(user)) {
                response = Response.status(Response.Status.CREATED).entity("User registered successfully").build(); //status code 201
            } else {
                response = Response.status(Response.Status.BAD_REQUEST).entity("Something went wrong").build(); //status code 400
            }
        }else{
            response = Response.status(Response.Status.BAD_REQUEST).entity("Invalid credentials").build();
        }

        return response;
    }

    @POST
    @Path("/application-membership")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response applicationMembership(@HeaderParam("email") String email) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request for application membership from IP: " + ip + ", at: " + now + ", with email: " + email);

        try {
            if (email == null || email.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Email header is missing").build();
            }
            // Chama o método no UserBean para enviar o e-mail para admin
            userBean.askToJoin(email);

            // Retorna uma resposta de sucesso
            return Response.status(Response.Status.OK).entity("E-mail send to admin").build();
        } catch (Exception e) {
            // Se houver algum erro, retorna uma resposta de erro
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error").build();
        }
    }

    @POST
    @Path("/forgot-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPasswordRequest(@HeaderParam("email") String email) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to reset password from IP: " + ip + ", at: " + now + ", with email: " + email);

        try {
            if (email == null || email.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Email header is missing").build();
            }
            // Chama o método no UserBean para enviar o e-mail de redefinição de senha
            userBean.sendPasswordResetEmail(email);

            // Retorna uma resposta de sucesso
            return Response.status(Response.Status.OK).entity("E-mail send to " + email).build();
        } catch (Exception e) {
            // Se houver algum erro, retorna uma resposta de erro
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("error").build();
        }
    }

    @GET
    @Path("/getFirstName")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFirstName(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get first name from IP: " + ip + ", at: " + now);

        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getFirstName()).build();
        }
        return response;
    }

    //Retorna o url da foto do token enviado
    @GET
    @Path("/getPhotoUrl")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImage(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get photo URL from IP: " + ip + ", at: " + now);

        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getPhotoURL()).build();
        }
        return response;
    }

    //Retorna username do token enviado
    @GET
    @Path("/getUsername")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsername(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get username from IP: " + ip + ", at: " + now);

        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getUsername()).build();
        }
        return response;
    }

    //Retorna username do token enviado
    @GET
    @Path("/username/pending")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernamePending(@HeaderParam("token") String token){

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get pending username from IP: " + ip + ", at: " + now);

        Response response;

        User currentUser = userBean.convertEntityByConfirmToken(token);

        if (!userBean.isConfirmationTokenValid(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getUsername()).build();
        }
        return response;
    }


    //Retorna tipo de user do token enviado
    @GET
    @Path("/getTypeOfUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTypeOfUser(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get type of user from IP: " + ip + ", at: " + now);

        Response response;

        User currentUser = userBean.convertEntityByToken(token);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(currentUser.getTypeOfUser()).build();
        }
        return response;
    }

    @GET
    @Path("/getUsernameFromEmail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernameFromEmail(@HeaderParam("email") String email, @HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get username from email from IP: " + ip + ", at: " + now);

        Response response;

        User user = userBean.convertEntityByEmail(email);

        if (!userBean.isAuthenticated(token)) {
            response = Response.status(401).entity("Invalid credentials").build();
        } else {
            response = Response.status(200).entity(user.getUsername()).build();
        }
        return response;
    }

    //Atualizar um user
    @PUT
    @Path("/update/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("username") String username, @HeaderParam("token") String token, User user) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to update user from IP: " + ip + ", at: " + now);

        Response response;

        User userUpdate = userBean.getUser(username);

        //Verifica se o username existe na base de dados
        if (userUpdate==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        //Verifica se token existe de quem consulta e se é Product Owner ou o próprio user
        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token) || userBean.thisTokenIsFromThisUsername(token,username)) {
            if (!userBean.isEmailUpdatedValid(user) && user.getEmail() != null) {
                response = Response.status(422).entity("Invalid email").build();

            } else if (!userBean.isImageUrlUpdatedValid(user.getPhotoURL()) && user.getPhotoURL() != null) {
                response = Response.status(422).entity("Image URL invalid").build();

            } else if (!userBean.isPhoneNumberUpdatedValid(user) && user.getPhone() != null) {
                response = Response.status(422).entity("Invalid phone number").build();

            } else {
                boolean updatedUser = userBean.updateUser(user, username, token);
                response = Response.status(Response.Status.OK).entity(updatedUser).build(); //status code 200
            }
        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
    return response;
    }


    @PUT
    @Path("/update/{username}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(@PathParam("username") String username,
                                   @HeaderParam("token") String token,
                                   @HeaderParam("oldpassword") String oldPassword,
                                   @HeaderParam("newpassword") String newPassword) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to update password from IP: " + ip + ", at: " + now);

        //Verica se user está autentificado
        if (userBean.isAuthenticated(token)){
            // Verificar password antiga
            boolean isOldPasswordValid = userBean.verifyOldPassword(username, oldPassword);
            if (!isOldPasswordValid) {
                return Response.status(401).entity("Incorrect old password").build();
            }
            // Se a password antiga é válida, update a password
            boolean updated = userBean.updatePassword(username, newPassword);
            if (!updated) {
                return Response.status(400).entity("User with this username is not found").build();
            }else return Response.status(200).entity("User password updated").build();
        }else
            return Response.status(401).entity("User is not logged in").build();
    }

    @PUT
    @Path("/resetpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetPassword(@HeaderParam("token") String token,
                                  @HeaderParam("newPassword") String newPassword,
                                  @HeaderParam("confirmPass") String confirmPassword){

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to reset password from IP: " + ip + ", at: " + now);

        // Verify if token is authenticated
        if (userBean.resetTokenIsAuth(token) && userBean.isResetTokenValid(token)){
            if (newPassword.equals(confirmPassword)){
                // Reset password
                boolean updated = userBean.resetPassword(token, newPassword);
                if (!updated) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("This token is not found")
                            .build();
                } else {
                    return Response.status(Response.Status.OK)
                            .entity("Password updated")
                            .build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Passwords do not match")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token does not exist or is invalid")
                    .build();
        }
    }

    @PUT
    @Path("/set-first-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setFirstPassword(@HeaderParam("token") String token,
                                  @HeaderParam("newPassword") String newPassword,
                                  @HeaderParam("confirmPass") String confirmPassword){

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to set first password from IP: " + ip + ", at: " + now);

        // Verify if token is authenticated
        if (userBean.isConfirmationTokenValid(token)){
            if (newPassword.equals(confirmPassword)){
                // Set password
                boolean updated = userBean.setFirstPassword(token, newPassword);
                if (!updated) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("This token is not found")
                            .build();
                } else {
                    return Response.status(Response.Status.OK)
                            .entity("Password set")
                            .build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Passwords do not match")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token does not exist or is invalid")
                    .build();
        }
    }
//-------------------------------------
    @PUT
    @Path("/update/{username}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVisibility(@PathParam("username") String username, @HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to update visibility from IP: " + ip + ", at: " + now);

        Response response;

        User user = userBean.getUser(username);

        //Verifica se o username existe na base de dados
        if (user==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        //Verifica se token de quem consulta existe e se é Product Owner
        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {

            userBean.updateUserEntityVisibility(username);
            response = Response.status(Response.Status.OK).entity(username + " visibility: " + !user.isVisible()).build(); //status code 200

        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    //Atualizar tipo de user
    @PUT
    @Path("/update/{username}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRole(@PathParam("username") String username, @HeaderParam("token") String token, @HeaderParam("typeOfUser") int typeOfUser) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to update role from IP: " + ip + ", at: " + now);

        Response response;

        User user = userBean.getUser(username);

        //Verifica se o username existe na base de dados
        if (user==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        //Verifica se token existe de quem consulta e se é Product Owner
        if (userBean.isAuthenticated(token) && userBean.userIsProductOwner(token)) {

            if (typeOfUser == 100 || typeOfUser == 200 || typeOfUser == 300) {

                boolean updatedRole = userBean.updateUserEntityRole(username, typeOfUser);
                response = Response.status(Response.Status.OK).entity("Role updated with success").build(); //status code 200
            }else response = Response.status(401).entity("Invalid type of User").build();

        }else {
            response = Response.status(401).entity("Invalid credentials").build();
        }

        return response;
    }

    //Apagar um user
    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to delete user with username: " + username + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {

            boolean removed = userBean.delete(username);
            if (removed) {
                response = Response.status(200).entity("User removed successfully").build();
            } else {
                response = Response.status(404).entity("User is not found").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all users from IP: " + ip + ", at: " + now);


        Response response;
        if (userBean.isAuthenticated(token)) {
            List<User> allUsers = userBean.getUsers();
            response = Response.status(200).entity(allUsers).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    @GET
    @Path("/all/visible/{visible}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByVisibility(@HeaderParam("token") String token, @PathParam("visible") boolean visible) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all users with visibility set as: " + visible + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {
            List<User> users = userBean.getUsersByVisibility(visible);
            response = Response.status(200).entity(users).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    @GET
    @Path("/all/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByType(@HeaderParam("token") String token, @PathParam("type") int typeOfUser) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all users with the type: " + typeOfUser + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {
            List<User> users = userBean.getUsersByType(typeOfUser);
            response = Response.status(200).entity(users).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    //GET Users pelo tipo usando QueryParams
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersByTypeQuery(@HeaderParam("token") String token, @QueryParam("type") int typeOfUser) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all users with the type: " + typeOfUser + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {
            List<User> users = userBean.getUsersByType(typeOfUser);
            response = Response.status(200).entity(users).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }


    @GET
    @Path("/all/{type}/{visible}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token, @PathParam("type") int typeOfUser, @PathParam("visible") boolean visible) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all users with the type: " + typeOfUser + " and visibility set as: " + visible + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {
            List<User> users = userBean.getUsersByTypeAndVisibility(typeOfUser,visible);
            response = Response.status(200).entity(users).build();
        } else {
            response = Response.status(401).entity("You don't have permission").build();
        }
        return response;
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
        public Response getUser(@PathParam("username") String username, @HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get user with username; " + username +", from IP: " + ip + ", at: " + now);

        Response response;

        User userSearched = userBean.getUser(username);

        //Verifica se o username existe na base de dados
        if (userSearched==null){
            response = Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            return response;
        }

        //Verifica se o token é igual ao username pesquisado
        if (userBean.thisTokenIsFromThisUsername(token,username)) {

            response = Response.ok().entity(userSearched).build();

        }else {
            //Verifica se token existe de quem consulta
            if (userBean.isAuthenticated(token)) {
                if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token) || userBean.userIsDeveloper(token)) {

                    response = Response.ok().entity(userSearched).build();
                } else {
                    response = Response.status(Response.Status.BAD_REQUEST).entity("Invalid username on path").build();
                }
            } else {
                response = Response.status(401).entity("Invalid credentials").build();
            }
        }
        return response;
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasks(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all tasks from IP: " + ip + ", at: " + now);

        Response response;

        if (userBean.isAuthenticated(token)) {
            ArrayList<Task> allTasks = taskBean.getAllTasks(token);
            allTasks.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getLimitDate)));
            response = Response.status(Response.Status.OK).entity(allTasks).build();
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/{username}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all tasks from username: " + username + ", from IP: " + ip + ", at: " + now);

        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.thisTokenIsFromThisUsername(token, username) || userBean.userIsProductOwner(token) || userBean.userIsScrumMaster(token) || userBean.userIsDeveloper(token)){
                ArrayList<Task> userTasks = taskBean.getAllTasksFromUser(username, token);
                userTasks.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getLimitDate)));
                response = Response.status(Response.Status.OK).entity(userTasks).build();
            } else {
                response = Response.status(406).entity("You don't have permission for this request").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @POST
    @Path("/{username}/addTask")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newTask(@HeaderParam("token") String token, @PathParam("username") String username, Task task) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to add a task from IP: " + ip + ", at: " + now + ", to username: " + username);

        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.thisTokenIsFromThisUsername(token, username)) {
                try {
                    boolean added = taskBean.newTask(task, token);
                    if (added) {
                        response = Response.status(201).entity("Task created successfully").build();
                    } else {
                        response = Response.status(404).entity("Impossible to create task. Verify all fields").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. A new task was not created.").build();
                }
            } else {
                response = Response.status(Response.Status.BAD_REQUEST).entity("Invalid username on path").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }

        return response;
    }

    @PUT
    @Path("/updatetask/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTask(@HeaderParam("token") String token, @PathParam("id") String id, Task updatedTask) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to update a task from IP: " + ip + ", at: " + now + ", with id: " + id);

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsTaskOwner(token, id) || userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                Task originalTask = taskBean.getTaskById(id);
                if (originalTask!= null) {
                    boolean updated = taskBean.updateTask(token, originalTask, updatedTask);
                    if (updated)
                        response = Response.status(200).entity("Task updated successfully").build();
                    else {
                        response = Response.status(404).entity("Impossible to update task. Verify all fields").build();
                    }
                } else {
                    response = Response.status(404).entity("Task not found").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to update this task").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }


    @PUT
    @Path("/tasks/{taskId}/{newStateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateTaskStatus(@HeaderParam("token") String token, @PathParam("taskId") String taskId, @PathParam("newStateId") int stateId) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to update the task with ID: " + taskId + " to status: " + stateId + ", from IP: " + ip + ", at: " + now );

        Response response;
        if (userBean.isAuthenticated(token)) {
            boolean updated = taskBean.updateTaskStatus(token, taskId, stateId);
            if (updated) {
                response = Response.status(200).entity("Task status updated successfully").build();
            } else {
                response = Response.status(404).entity("Impossible to update task status. Task not found or invalid status").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseTaskStatus(@HeaderParam("token") String token, @PathParam("taskId") String id) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to erase a task from IP: " + ip + ", at: " + now + ", with id: " + id);

        Response response;
        if (userBean.isAuthenticated(token)) {

            if (userBean.userIsScrumMaster(token) ||
                    userBean.userIsProductOwner(token) ||
                    (userBean.userIsDeveloper(token) && taskBean.isTaskIdFromThisOwner(id, token))) {
                try {
                    boolean switched = taskBean.switchErasedTaskStatus(token, id);
                    if (switched) {
                        response = Response.status(200).entity("Task erased status switched successfully").build();
                    } else {
                        response = Response.status(404).entity("Task with this id is not found").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The task erased status was switched.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to switch the erased status of a task").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/eraseAllTasks/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eraseAllTasksFromUser(@HeaderParam("token") String token, @PathParam("username") String username) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to erase all task from IP: " + ip + ", at: " + now + ", from username: " + username);

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean erased = taskBean.eraseAllTasksFromUser(username);
                    if (erased) {
                        response = Response.status(200).entity("All tasks were erased successfully").build();
                    } else {
                        response = Response.status(404).entity("Impossible to erase tasks").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The tasks were not erased.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to erase these tasks").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @DELETE
    @Path("/delete/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTask(@HeaderParam("token") String token, @PathParam("taskId") String id) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to delete permanently a task with id:" + id + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = taskBean.permanentlyDeleteTask(token, id);
                    if (deleted) {
                        response = Response.status(200).entity("Task removed successfully").build();
                    } else {
                        response = Response.status(404).entity("Task with this id is not found").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The task was not removed.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete a task").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/tasks/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByCategory(@HeaderParam("token") String token, @PathParam("category") String category) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all tasks from category:" + category + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> tasksByCategory = taskBean.getTasksByCategory(category);
                response = Response.status(200).entity(tasksByCategory).build();
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/erasedTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByErasedStatus(@HeaderParam("token") String token, @QueryParam("erased") boolean erasedStatus) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all tasks with erased status as:" + erasedStatus + ", from IP: " + ip + ", at: " + now);

        Response response;
        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsScrumMaster(token) || userBean.userIsProductOwner(token)) {
                ArrayList<Task> erasedTasks = taskBean.getTasksByErasedStatus(erasedStatus);
                response = Response.status(200).entity(erasedTasks).build();
            } else {
                response = Response.status(403).entity("You don't have permission for this request").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @POST
    @Path("/newCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newCategory(@HeaderParam("token") String token, Category category) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to add a new category:" + category.getName() + ", from IP: " + ip + ", at: " + now);

        Response response;

        if (userBean.isAuthenticated(token)) {
                if (userBean.userIsProductOwner(token)) {
                    if (categoryBean.categoryExists(category.getName())) {
                        response = Response.status(409).entity("Category with this name already exists").build();
                    } else {
                        try {
                            boolean added = categoryBean.newCategory(category.getName());
                            if (added) {
                                response = Response.status(201).entity("Category created successfully").build();
                            } else {
                                response = Response.status(404).entity("Impossible to create category. Verify all fields").build();
                            }
                        } catch (Exception e) {
                            response = Response.status(404).entity("Something went wrong. A new category was not created.").build();
                        }
                    }
                } else {
                    response = Response.status(403).entity("You don't have permission to create a category").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response; // FALTA FAZER VERIFICAÇÃO DAS PERMISSÕES DO UTILIZADOR PARA CRIAR CATEGORIA
    }

    @DELETE
    @Path("/deleteCategory/{categoryName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@HeaderParam("token") String token, @PathParam("categoryName") String categoryName) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to delete the category:" + categoryName + ", from IP: " + ip + ", at: " + now);

        Response response;

        if (userBean.isAuthenticated(token)) {
                if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = categoryBean.deleteCategory(categoryName);
                    if (deleted) {
                        response = Response.status(200).entity("Category removed successfully").build();
                    } else {
                        response = Response.status(400).entity("Category with this name can't be deleted").build();
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The category was not removed.").build();
                }
                } else {
                    response = Response.status(403).entity("You don't have permission to delete a category").build();
                }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @DELETE
    @Path("/deleteCategory/id/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCategoryById(@HeaderParam("token") String token, @PathParam("id") int id) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to delete the category with ID:" + id + ", from IP: " + ip + ", at: " + now);

        Response response;

        if (userBean.isAuthenticated(token)) {
            if (userBean.userIsProductOwner(token)) {
                try {
                    boolean deleted = categoryBean.deleteCategoryById(id);
                    if (deleted) {
                        response = Response.status(200).entity("Category removed successfully").build();
                    } else {
                        if (!categoryBean.categoryIdExists(id)) {
                            response = Response.status(404).entity("Category with this name not found").build();
                        } else {
                            response = Response.status(400).entity("Category with this name can't be deleted").build();
                        }
                    }
                } catch (Exception e) {
                    response = Response.status(404).entity("Something went wrong. The category was not removed.").build();
                }
            } else {
                response = Response.status(403).entity("You don't have permission to delete a category").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @PUT
    @Path("/editCategory/{categoryName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editCategory(@HeaderParam("token") String token, @PathParam("categoryName") String categoryName, @HeaderParam("newCategoryName") String newCategoryName) {


        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to edit the category:" + categoryName + ", to: " + newCategoryName + ", from IP: " + ip + ", at: " + now);

        Response response;

        if (userBean.isAuthenticated(token)) {
                if (userBean.userIsProductOwner(token)) {
                    try {
                        System.out.println("########################## TRY " + categoryName + " " + newCategoryName);
                        boolean edited = categoryBean.editCategory(categoryName, newCategoryName);
                        System.out.println("************************** EDITED ENDPOINT " + edited + " *********************************");
                        if (edited) {
                            response = Response.status(200).entity("Category edited successfully").build();
                        } else {
                            response = Response.status(404).entity("Category with this name is not found").build();
                        }
                    } catch (Exception e) {
                        response = Response.status(404).entity("Something went wrong. The category was not edited.").build();
                    }
                } else {
                    response = Response.status(403).entity("You don't have permission to edit a category").build();
                }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token") String token) {

        String ip = request.getRemoteAddr();
        LocalDateTime now = LocalDateTime.now();
        logger.info("Received request to get all categories from IP: " + ip + ", at: " + now);

        Response response;

        if (userBean.isAuthenticated(token)) {
            try {
                List<Category> allCategories = categoryBean.findAllCategories();
                response = Response.status(200).entity(allCategories).build();
            } catch (Exception e) {
                response = Response.status(404).entity("Something went wrong. The categories were not found.").build();
            }
        } else {
            response = Response.status(401).entity("Invalid credentials").build();
        }
        return response;
    }

    @POST
    @Path("/session/reset")
    public void resetSessionTimeout(@HeaderParam("token") String token) {

        sessionManager.userActivity(token);
    }

}
