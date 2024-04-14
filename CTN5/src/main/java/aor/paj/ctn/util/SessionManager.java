package aor.paj.ctn.util;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static final long DEFAULT_TIMEOUT = 300000; // 5 minutos
    private Map<String, Long> userLastActivity = new HashMap<>();
    private long timeout;

    public SessionManager(long timeout) {
        this.timeout = timeout;
    }

    public void userActivity(String userId) {
        userLastActivity.put(userId, System.currentTimeMillis());
    }

    public void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        for (String userId : userLastActivity.keySet()) {
            long lastActivityTime = userLastActivity.get(userId);
            if (currentTime - lastActivityTime > timeout) {
                // Invalidar a sessão do usuário aqui
                logoutUser(userId);
            }
        }
    }

    private void logoutUser(String userId) {


        // Implemente a lógica de logout do user aqui!!!


        System.out.println("User " + userId + " desconectado devido a inatividade.");
        userLastActivity.remove(userId);
    }

    public static void main(String[] args) {
        long timeout = readTimeoutFromConfig(); // Implemente a leitura do tempo de timeout a partir da configuração
        SessionManager sessionManager = new SessionManager(timeout);

        // Simulação de atividade de usuário
        sessionManager.userActivity("user1");
        sessionManager.userActivity("user2");

        // Verificação de inatividade
        sessionManager.checkInactiveUsers();
    }

    private static long readTimeoutFromConfig() {
        // Implemente a leitura do timeout a partir da configuração
        return DEFAULT_TIMEOUT;
    }
}
