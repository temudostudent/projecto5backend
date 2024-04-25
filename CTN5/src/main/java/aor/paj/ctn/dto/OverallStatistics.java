package aor.paj.ctn.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Map;

@XmlRootElement
public class OverallStatistics extends UserStatistics{

    @XmlElement
    private int users;
    @XmlElement
    private int devs;
    @XmlElement
    private int scrumMasters;
    @XmlElement
    private int productOwners;
    @XmlElement
    private int confirmedUsers;
    @XmlElement
    private int notConfirmedUsers;
    @XmlElement
    private double avgTasksPerUser;
    @XmlElement
    private double avgTaskDone;
    @XmlElement
    private Map<String, String> usersByTime;
    @XmlElement
    private Map<String, String> tasksDoneByTime;
    @XmlElement
    private static final String dtoType = "Statistics";

    public OverallStatistics() {
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }

    public int getDevs() {
        return devs;
    }

    public void setDevs(int devs) {
        this.devs = devs;
    }

    public int getScrumMasters() {
        return scrumMasters;
    }

    public void setScrumMasters(int scrumMasters) {
        this.scrumMasters = scrumMasters;
    }

    public int getProductOwners() {
        return productOwners;
    }

    public void setProductOwners(int productOwners) {
        this.productOwners = productOwners;
    }

    public int getConfirmedUsers() {
        return confirmedUsers;
    }

    public void setConfirmedUsers(int confirmedUsers) {
        this.confirmedUsers = confirmedUsers;
    }

    public int getNotConfirmedUsers() {
        return notConfirmedUsers;
    }

    public void setNotConfirmedUsers(int notConfirmedUsers) {
        this.notConfirmedUsers = notConfirmedUsers;
    }

    public double getAvgTasksPerUser() {
        return avgTasksPerUser;
    }

    public void setAvgTasksPerUser(double avgTasksPerUser) {
        this.avgTasksPerUser = avgTasksPerUser;
    }

    public double getAvgTaskDone() {
        return avgTaskDone;
    }

    public void setAvgTaskDone(double avgTaskDone) {
        this.avgTaskDone = avgTaskDone;
    }

    public Map<String, String> getUsersByTime() {
        return usersByTime;
    }

    public void setUsersByTime(Map<String, String> usersByTime) {
        this.usersByTime = usersByTime;
    }

    public Map<String, String> getTasksDoneByTime() {
        return tasksDoneByTime;
    }

    public void setTasksDoneByTime(Map<String, String> tasksDoneByTime) {
        this.tasksDoneByTime = tasksDoneByTime;
    }

    public String getDtoType() {
        return dtoType;
    }
}
