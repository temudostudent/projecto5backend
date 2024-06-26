package aor.paj.ctn.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name="task")
@NamedQuery(name="Task.findTaskById", query="SELECT a FROM TaskEntity a WHERE a.id = :id")// os : significam que, neste caso o id é um parametro/atributo
@NamedQuery(name="Task.findTasksByUser", query="SELECT a FROM TaskEntity a WHERE a.owner = :owner")
@NamedQuery(name="Task.findTasksByCategory", query="SELECT a FROM TaskEntity a WHERE a.category = :category")
@NamedQuery(name="Task.findTasksByCategoryID", query="SELECT a FROM TaskEntity a WHERE a.category.id = :categoryId")
@NamedQuery(name="Task.findErasedTasks", query="SELECT a FROM TaskEntity a WHERE a.erased = true")
@NamedQuery(name="Task.findActiveTasks", query="SELECT a FROM TaskEntity a WHERE a.erased = false")
@NamedQuery(name="Task.findTasksByErasedStatus", query="SELECT a FROM TaskEntity a WHERE a.erased = :erased ORDER BY a.priority DESC, a.startDate ASC, a.limitDate ASC")
@NamedQuery(name="Task.findAllTasks", query="SELECT a FROM TaskEntity a")
@NamedQuery(name="DeleteTask", query="DELETE FROM TaskEntity a WHERE a.id = :id")
@NamedQuery(name="Task.countAllTasks", query = "SELECT COUNT(*) FROM TaskEntity t")
@NamedQuery(name="Task.averageTasksPerUser", query = "SELECT AVG((SELECT COUNT(t) FROM TaskEntity t WHERE t.owner = u AND t.erased = false)) FROM UserEntity u WHERE u.visible=true")
@NamedQuery(name="Task.countAllTasksFromUser", query = "SELECT COUNT(t) FROM TaskEntity t WHERE t.owner = :owner")
@NamedQuery(name="Task.countTasksByState", query = "SELECT COUNT(t) FROM TaskEntity t WHERE t.stateId = :stateId")
@NamedQuery(name="Task.countAllTasksFromUserByState", query = "SELECT COUNT(t) FROM TaskEntity t WHERE t.owner = :owner AND t.stateId = :stateId")
@NamedQuery(name="Task.fetchStartAndConclusionDates", query="SELECT t.startDate, t.conclusionDate FROM TaskEntity t WHERE t.conclusionDate IS NOT NULL")
@NamedQuery(name="Task.countTasksByConclusionDate", query = "SELECT t.conclusionDate, COUNT(t) FROM TaskEntity t WHERE t.conclusionDate IS NOT NULL GROUP BY t.conclusionDate ORDER BY t.conclusionDate ASC")
public class TaskEntity implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="id", nullable = false, unique = true, updatable = false)
    private String id;

    @Column (name="title", nullable = false, unique = false, length = 100)
    private String title;

    @Column (name="description", nullable = false, unique = false, length = 20000, columnDefinition = "TEXT")
    private String description;

    @Column (name="stateId", nullable = false, unique = false, updatable = true)
    private int stateId;

    @Column (name="priority", nullable = false, unique = false, updatable = true)
    private int priority;

    @Column (name="creation_date", nullable = false, unique = false, updatable = false)
    private LocalDate creationDate;

    @Column (name="conclusion_date", nullable = true, unique = false, updatable = true)
    private LocalDate conclusionDate;

    @Column (name="startDate", nullable = false, unique = false, updatable = true)
    private LocalDate startDate;

    @Column (name="limitDate", nullable = false, unique = false, updatable = true)
    private LocalDate limitDate;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private CategoryEntity category;

    @Column (name="erased", nullable = false, unique = false, updatable = true)
    private boolean erased;

    //Owning Side User - task
    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "username")
    private UserEntity owner;


    public TaskEntity() {
        this.creationDate= LocalDate.now();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public LocalDate getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate)
    {
        this.creationDate = creationDate;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getLimitDate() {
        return limitDate;
    }

    public void setLimitDate(LocalDate limitDate) {
        this.limitDate = limitDate;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public boolean getErased() {
        return erased;
    }

    public void setErased(boolean erased) {
        this.erased = erased;
    }

    public LocalDate getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDate conclusionDate) {
        this.conclusionDate = conclusionDate;
    }
}