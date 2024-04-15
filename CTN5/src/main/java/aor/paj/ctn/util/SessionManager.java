package aor.paj.ctn.util;

import aor.paj.ctn.bean.UserBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SessionManager {
    private ConcurrentHashMap<String, Long> userActivity;
    private long timeout;
    private ScheduledExecutorService executorService;
    private UserBean userBean;

    public SessionManager() {
    }

    @Inject
    public SessionManager(UserBean userBean) {
        this.userActivity = new ConcurrentHashMap<>();
        this.timeout = 60000; // 5 minutes in milliseconds
        this.userBean = userBean;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::checkInactiveUsers, timeout, timeout, TimeUnit.MILLISECONDS);
    }

    public void userActivity(String userId) {
        userActivity.put(userId, System.currentTimeMillis());
    }

    private void checkInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : userActivity.entrySet()) {
            if (currentTime - entry.getValue() > timeout) {
                userBean.logout(entry.getKey());
                userActivity.remove(entry.getKey());
            }
        }
    }
}
