package aor.paj.ctn.bean;

import aor.paj.ctn.dao.AuthenticationLogDao;
import aor.paj.ctn.dao.TaskDao;
import aor.paj.ctn.dao.UserDao;
import aor.paj.ctn.dto.AuthenticationLog;
import aor.paj.ctn.dto.Login;
import aor.paj.ctn.dto.Task;
import aor.paj.ctn.dto.User;
import aor.paj.ctn.entity.AuthenticationLogEntity;
import aor.paj.ctn.entity.TaskEntity;
import aor.paj.ctn.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

@Stateless
public class UserBean implements Serializable {

    @EJB
    private UserDao userDao;
    @EJB
    private TaskDao taskDao;
    @EJB
    private AuthenticationLogDao authenticationLogDao;
    @EJB
    private CategoryBean categoryBean;

    @Inject
    private EmailService emailService;

    private static final Logger logger = LogManager.getLogger(UserBean.class);


    //Construtor vazio
    public UserBean(){}

    public UserBean(UserDao userDao) {
        this.userDao = userDao;
    }

    public void createDefaultUsersIfNotExistent() {
        UserEntity userEntity = userDao.findUserByUsername("admin");
        if (userEntity == null) {
            User admin = new User();
            admin.setUsername("ADMIN");
            admin.setPassword("admin");
            admin.setEmail("admin@admin.com");
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setPhone("000000001");
            admin.setTypeOfUser(300);

            register(admin);
        }

        UserEntity userEntity2 = userDao.findUserByUsername("deletedUser");
        if (userEntity2 == null) {
            User deletedUser = new User();
            deletedUser.setUsername("deletedUser");
            deletedUser.setPassword("123");
            deletedUser.setEmail("deleted@user.com");
            deletedUser.setFirstName("Deleted");
            deletedUser.setLastName("User");
            deletedUser.setPhone("000000000");
            deletedUser.setTypeOfUser(400);

            register(deletedUser);
        }
    }

    //Permite ao utilizador entrar na app, gera token
    public String login(Login user) {
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if (userEntity != null && userEntity.isVisible()) {
            //Verifica se a password coincide com a password encriptada
            if (BCrypt.checkpw(user.getPassword(), userEntity.getPassword())) {
                String token = generateNewToken();
                userEntity.setToken(token);
                return token;
            }
        }
        return null;
    }

    //Faz o registo do utilizador, adiciona à base de dados
    public boolean register(User user) {

        if (user != null) {
            if (user.getUsername().equals("ADMIN") || user.getUsername().equals("deletedUser")){
                user.setVisible(false);
            }else{

                if (user.getTypeOfUser() != User.SCRUMMASTER && user.getTypeOfUser() != User.PRODUCTOWNER) {

                    user.setInitialTypeOfUser();
                }
                user.setVisible(true);
            }

            //Encripta a password usando BCrypt
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            //Define a password encriptada
            user.setPassword(hashedPassword);

            //Persist o user
            userDao.persist(convertUserDtotoUserEntity(user));
            return true;
        } else {
            return false;
        }
    }

    //Faz o registo pendente do utilizador, adiciona à base de dados
    public boolean registerPending(User user) {

        if (user != null) {

            user.setVisible(false);

            AuthenticationLogEntity a= new AuthenticationLogEntity();

            // Gerar um token de redefinição de senha único
            String confirmToken = generateNewToken();

            // Atualizar o token de redefinição de senha no banco de dados
            a.setConfirmToken(confirmToken);
            a.setConfirmTokenExpiry(new Date(System.currentTimeMillis() + (48 * 60 * 60 * 1000))); // Token expira em 48 horas

            // Construir o URL para a página de redefinição de senha
            String confirmURL = "http://localhost:3000/confirm-account?token=" + confirmToken;


            a.setUser(convertUserDtotoUserEntity(user));
            a.setAuthenticated(false);
            a.setSendInviteTime(LocalDate.now());

            //Persist o user
            userDao.persist(convertUserDtotoUserEntity(user));
            authenticationLogDao.persist(a);

            sendConfirmAccountEmail(user.getEmail(), confirmURL);

            return true;
        } else {
            return false;
        }
    }


    //Apaga todos os registos do utilizador da base de dados
    //Verificar tarefas!!!!!!!
    public boolean delete(String username) {

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null /*&& u.isVisible()==false*/) {
            ArrayList<TaskEntity> tasks = taskDao.findTasksByUser(u);
            UserEntity notAssigned = userDao.findUserByUsername("deletedUser");

            notAssigned.addNewTasks(tasks);

            if (!tasks.isEmpty()){
                for (TaskEntity t : tasks) {
                    t.setOwner(notAssigned);
                }
            }

            userDao.remove(u);
            return true;
        } else
            return false;
    }

    public void sendPasswordResetEmail(String userEmail) {
        // Verifica se o e-mail existe na base de dados
        UserEntity userEntity = userDao.findUserByEmail(userEmail);
        if (userEntity != null) {
            // Gerar um token de redefinição de senha único
            String resetToken = generateNewToken();

            // Atualizar o token de redefinição de senha no banco de dados
            userEntity.setResetToken(resetToken);
            userEntity.setResetTokenExpiry(new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000))); // Token expira em 24 horas

            // Persistir as alterações no banco de dados
            userDao.merge(userEntity);

            // Construir o URL para a página de redefinição de senha
            String resetURL = "http://localhost:3000/reset-password?token=" + resetToken;

            // Enviar e-mail com o link para redefinição de senha
            try {
                emailService.sendPasswordResetEmail(userEmail, userEntity.getUsername(), resetURL);
            } catch (MessagingException e) {
                logger.error(e);
            }
        } else {
            System.out.println(userEmail + " not registed and want to reset the password");
            logger.warn(userEmail + " not registed and want to reset the password");
        }
    }

    public void sendConfirmAccountEmail(String userEmail, String confirmURL) {
        // Verifica se o e-mail existe na base de dados
        UserEntity userEntity = userDao.findUserByEmail(userEmail);
        if (userEntity != null) {

            // Enviar e-mail com o link para redefinição de senha
            try {
                emailService.sendAccountConfimationEmail(userEmail, userEntity.getUsername(), confirmURL);
            } catch (MessagingException e) {
                logger.error(e);
            }
        } else {
            System.out.println(userEmail + " not registed and want to reset the password");
            logger.warn(userEmail + " not registed and want to reset the password");
        }
    }

    public boolean isResetTokenValid(String resetToken) {
        // Busca o user pelo token de redefinição de senha
        UserEntity userEntity = userDao.findUserByResetToken(resetToken);

        // Verifica se o usuário e o token existem e se o token não expirou
        if (userEntity != null && userEntity.getResetToken().equals(resetToken) && userEntity.getResetTokenExpiry().after(new Date())) {
            return true; // Token é válido
        } else {
            userEntity.setResetToken(null);
            userEntity.setResetTokenExpiry(null);
            return false; // Token não é válido
        }
    }


    //Métodos de conversão

    public UserEntity convertUserDtotoUserEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        userEntity.setTypeOfUser(user.getTypeOfUser());
        userEntity.setEmail(user.getEmail());
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setPhone(user.getPhone());
        userEntity.setVisible(user.isVisible());
        userEntity.setPhotoURL(user.getPhotoURL());

        return userEntity;
    }

    public User convertUserEntitytoUserDto(UserEntity userEntity) {
        User user = new User();
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());
        user.setTypeOfUser(userEntity.getTypeOfUser());
        user.setEmail(userEntity.getEmail());
        user.setFirstName(userEntity.getFirstName());
        user.setLastName(userEntity.getLastName());
        user.setPhone(userEntity.getPhone());
        user.setPhotoURL(userEntity.getPhotoURL());
        user.setVisible(userEntity.isVisible());

        return user;
    }

    public Task convertTaskEntitytoTaskDto(TaskEntity taskEntity) {
        Task t = new Task();
        t.setId(taskEntity.getId());
        t.setOwner(convertUserEntitytoUserDto(taskEntity.getOwner()));
        t.setTitle(taskEntity.getTitle());
        t.setDescription(taskEntity.getDescription());
        t.setStateId(taskEntity.getStateId());
        t.setPriority(taskEntity.getPriority());
        t.setStartDate(taskEntity.getStartDate());
        t.setLimitDate(taskEntity.getLimitDate());
        t.setCategory(categoryBean.convertCategoryEntityToCategoryDto(taskEntity.getCategory()));
        t.setErased(taskEntity.getErased());

        return t;
    }

    public AuthenticationLogEntity convertAuthenticationLogDtotoAuthenticationLogEntity(AuthenticationLog a) {
        AuthenticationLogEntity aEntity = new AuthenticationLogEntity();
        aEntity.setAuthenticated(a.isAuthenticated());
        aEntity.setUser(convertUserDtotoUserEntity(a.getUser()));
        aEntity.setSendInviteTime(a.getSendInviteTime());

        return aEntity;
    }


    //Gerar token
    private String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom(); //threadsafe
        Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }


    //Logout
    public boolean logout(String token) {
        UserEntity u = userDao.findUserByToken(token);

        if (u != null) {
            u.setToken(null);
            return true;
        }
        return false;
    }

    public ArrayList<User> getUsers() {

        ArrayList<UserEntity> userEntities = userDao.findAllUsers();
        if (userEntities != null) {
            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {

                if (userE.getTypeOfUser()!=400 || userE.getUsername().equals("ADMIN")){
                    users.add(convertUserEntitytoUserDto(userE));
                }
            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    //Receber users pelo tipo de user
    public ArrayList<User> getUsersByType(int typeOfUser) {

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByTypeOfUser(typeOfUser);
        if (userEntities != null) {
            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {

                users.add(convertUserEntitytoUserDto(userE));

            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    //Receber users pelo tipo de visibilidade
    public ArrayList<User> getUsersByVisibility(boolean visible) {

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByVisibility(visible);
        if (userEntities != null) {
            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {

                if (userE.getTypeOfUser()!=400 || userE.getUsername().equals("ADMIN"))

                users.add(convertUserEntitytoUserDto(userE));

            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    //Receber users pelo tipo de user e de visibilidade
    public ArrayList<User> getUsersByTypeAndVisibility(int typeOfUser, boolean visible) {

        ArrayList<UserEntity> userEntities = userDao.findAllUsersByTypeOfUserAndVisibility(typeOfUser, visible);
        if (userEntities != null) {
            ArrayList<User> users = new ArrayList<>();
            for (UserEntity userE : userEntities) {

                users.add(convertUserEntitytoUserDto(userE));

            }
            return users;
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    /*public boolean addUser(User user) {

        boolean status = false;
        if (users.add(user)) {
            status = true;
        }
        writeIntoJsonFile();
        return status;
    }*/

    public User getUser(String username) {

        UserEntity u = userDao.findUserByUsername(username);

        if (u!=null){
            return convertUserEntitytoUserDto(u);
        }

        return null;
    }

    //Coloco username porque no objeto de atualização não está referenciado
    public boolean updateUser(User user, String username) {
        boolean status = false;

        // Busca o user pelo username
        UserEntity u = userDao.findUserByUsername(username);

        if (u != null && u.getUsername().equals(username)){

            // Verifica se o email no objeto User é nulo ou vazio
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                // Se não for nulo nem vazio, atualiza o email
                u.setEmail(user.getEmail());
            }

            // Verifica se o contacto no objeto User é nulo ou vazio
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                // Se não for nulo nem vazio, atualiza o contacto
                u.setPhone(user.getPhone());
            }

            // Verifica se o primeiro nome no objeto User é nulo ou vazio
            if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                // Se não for nulo nem vazio, atualiza o primeiro nome
                u.setFirstName(user.getFirstName());
            }

            // Verifica se o apelido no objeto User é nulo ou vazio
            if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                // Se não for nulo nem vazio, atualiza o apelido
                u.setLastName(user.getLastName());
            }

            // Verifica se a foto no objeto User é nulo ou vazio
            if (user.getPhotoURL() != null && !user.getPhotoURL().isEmpty()) {
                // Se não for nulo nem vazio, atualiza a foto
                u.setPhotoURL(user.getPhotoURL());
            }

            // Verifica se o typeOfUser no objeto User é nulo ou vazio
            if (user.getTypeOfUser() != 0) {
                // Se não for nulo nem vazio, atualiza a foto
                u.setTypeOfUser(user.getTypeOfUser());
            }

            try{
                userDao.merge(u); //Atualiza o user na base de dados
                status = true;
            } catch (Exception e){
                e.printStackTrace();
                status = false;
            }
        }

        return status;
    }

    public boolean updateUserEntityVisibility(String username) {
        boolean status = false;

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null){

            u.setVisible(!u.isVisible());

            status = true;
        }

        return status;
    }

    public boolean updateUserEntityRole(String username, int typeOfUser) {
        boolean status = false;

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null && u.getTypeOfUser() != typeOfUser){

            u.setTypeOfUser(typeOfUser);

            status = true;
        }

        return status;
    }

    public boolean isAuthenticated(String token) {

        boolean validUser = false;
        UserEntity user = userDao.findUserByToken(token);
        if (user != null && user.isVisible()) {
            validUser = true;
        }

        return validUser;
    }

    public boolean resetTokenIsAuth(String token) {

        boolean validToken = false;
        UserEntity user = userDao.findUserByResetToken(token);
        if (user != null) {
            validToken = true;
        }

        return validToken;
    }

    public boolean confirmTokenIsAuth(String token) {

        boolean validToken = false;
        UserEntity user = userDao.findUserByConfirmToken(token);
        if (user != null) {
            validToken = true;
        }

        return validToken;
    }


    public boolean isUsernameAvailable(User user) {

        UserEntity u = userDao.findUserByUsername(user.getUsername());
        boolean status = false;

        if (u == null) {
            status = true;
        }

        return status;
    }

    private boolean isEmailFormatValid(String email) {
        // Use a regular expression to perform email format validation
        // This regex is a basic example and may need to be adjusted
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public boolean isEmailValid(User user) {

        UserEntity u = userDao.findUserByEmail(user.getEmail());
        // Check if the email format is valid
        if (isEmailFormatValid(user.getEmail()) && u == null) {
            return true;
        }

        return false;
    }

    public boolean isEmailUpdatedValid(User user) {

        //Se for null é porque não houve nenhuma atualização
        if (user.getEmail() == null){
            return true;
        }

        UserEntity u = userDao.findUserByEmail(user.getEmail());
        // Check if the email format is valid
        if (isEmailFormatValid(user.getEmail()) && u == null) {
            return true;
        }

        return false;
    }


    public boolean isAnyFieldEmpty(User user) {
        boolean status = false;

        if (user.getUsername().isEmpty() ||
                user.getEmail().isEmpty() ||
                user.getFirstName().isEmpty() ||
                user.getLastName().isEmpty() ||
                user.getPhone().isEmpty()) {
            status = true;
        }
        return status;
    }

    public boolean isPhoneNumberValid(User user) {
        boolean status = true;
        int i = 0;

        UserEntity u = userDao.findUserByPhone(user.getPhone());

        while (status && i < user.getPhone().length() - 1) {
            if (user.getPhone().length() == 9) {
                for (; i < user.getPhone().length(); i++) {
                    if (!Character.isDigit(user.getPhone().charAt(i))) {
                        status = false;
                    }
                }
            } else {
                status = false;
            }
        }

        //Se existir contacto na base de dados retorna false
        if (u != null) {
            status = false;
        }

        return status;
    }

    public boolean isPhoneNumberUpdatedValid(User user) {
        boolean status = true;

        //Se for null é porque não houve nenhuma atualização
        if (user.getPhone()==null){
            return true;
        }

        int i = 0;

        UserEntity u = userDao.findUserByPhone(user.getPhone());

        while (status && i < user.getPhone().length() - 1) {
            if (user.getPhone().length() == 9) {
                for (; i < user.getPhone().length(); i++) {
                    if (!Character.isDigit(user.getPhone().charAt(i))) {
                        status = false;
                    }
                }
            } else {
                status = false;
            }
        }

        //Se existir contacto na base de dados retorna false
        if (u != null) {
            status = false;
        }

        return status;
    }

    public boolean isImageUrlValid(String url) {
        boolean status = true;

        if (url == null) {
            status = false;
        }

        try {
            BufferedImage img = ImageIO.read(new URL(url));
            if (img == null) {
                status = false;
            }
        } catch (IOException e) {
            status = false;
        }

        return status;
    }

    public boolean isImageUrlUpdatedValid(String url) {
        boolean status = true;

        //Se for null é porque não houve nenhuma alteração
        if (url == null) {
            return true;
        }

        try {
            BufferedImage img = ImageIO.read(new URL(url));
            if (img == null) {
                status = false;
            }
        } catch (IOException e) {
            status = false;
        }

        return status;
    }


    public ArrayList<Task> getUserAndHisTasks(String username) {

        UserEntity u = userDao.findUserByUsername(username);

        if (u != null) {
            ArrayList<TaskEntity> taskEntities = taskDao.findTasksByUser(u);
            if (taskEntities != null) {
                ArrayList<Task> userTasks = new ArrayList<>();
                for (TaskEntity taskEntity : taskEntities) {

                    userTasks.add(convertTaskEntitytoTaskDto(taskEntity));

                }
                return userTasks;
            }
        }
        //Retorna uma lista vazia se não forem encontradas tarefas
        return new ArrayList<>();
    }

    public boolean userIsTaskOwner(String token, String id) {
        UserEntity userEntity = userDao.findUserByToken(token);
        TaskEntity taskEntity = taskDao.findTaskById(id);
        boolean authorized = false;
        if (userEntity != null) {
            if (taskEntity.getOwner().getUsername().equals(userEntity.getUsername())) {
                authorized = true;
            }
        }
        return authorized;
    }

    public boolean userIsDeveloper(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;
        if (userEntity != null) {
            if (userEntity.getTypeOfUser() == User.DEVELOPER) {
                authorized = true;
            }
        }
        return authorized;
    }

    public boolean userIsScrumMaster(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;
        if (userEntity != null) {
            if (userEntity.getTypeOfUser() == User.SCRUMMASTER) {
                authorized = true;
            }
        }
        return authorized;
    }

    public boolean userIsProductOwner(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        boolean authorized = false;
        if (userEntity != null) {
            if (userEntity.getTypeOfUser() == User.PRODUCTOWNER) {
                authorized = true;
            }
        }
        return authorized;
    }

    //Chamar método no Bean


    //Converte a Entidade com o token "token" para DTO
    public User convertEntityByToken (String token){

        UserEntity currentUserEntity = userDao.findUserByToken(token);
        User currentUser = convertUserEntitytoUserDto(currentUserEntity);

        if (currentUser != null){
            return currentUser;
        }else return null;

    }

    //Converte a Entidade com o token "token" para DTO
    public User convertEntityByConfirmToken (String token){

        UserEntity currentUserEntity = userDao.findUserByConfirmToken(token);
        User currentUser = convertUserEntitytoUserDto(currentUserEntity);

        if (currentUser != null){
            return currentUser;
        }else return null;

    }

    //Converte a Entidade com o email "email" para DTO
    public User convertEntityByEmail (String email){

        UserEntity userEntity = userDao.findUserByEmail(email);
        User user = convertUserEntitytoUserDto(userEntity);

        if (user != null){
            return user;
        }else return null;

    }

    public boolean thisTokenIsFromThisUsername(String token, String username){

        if(userDao.findUserByToken(token).getUsername().equals(username)){
            return true;
        }else return false;

    }

    public boolean verifyOldPassword(String username, String oldPassword){

        UserEntity user = userDao.findUserByUsername(username);
        if (user!=null){
            return BCrypt.checkpw(oldPassword, user.getPassword());
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {

        UserEntity user = userDao.findUserByUsername(username);
        if (user != null) {
            //Encripta a password usando BCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            //Define a password encriptada
            user.setPassword(hashedPassword);
            return true;
        }
        return false;
    }

    public boolean resetPassword(String token, String newPassword) {

        UserEntity user = userDao.findUserByResetToken(token);
        if (user != null) {
            //Encripta a password usando BCrypt
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            //Define a password encriptada
            user.setPassword(hashedPassword);

            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            return true;
        }
        return false;
    }

}