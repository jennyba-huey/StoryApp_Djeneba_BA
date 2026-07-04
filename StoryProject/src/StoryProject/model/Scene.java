package StoryProject.model;

import java.util.ArrayList;
import java.util.List;

/** Une scène appartenant à une histoire, avec les personnages qui y apparaissent. */
public class Scene {

    private Long            id;
    private Long            storyId;
    private String          titre;
    private int             ordre;
    private String          lieu;
    private String          moment;
    private String          resume;
    private StatutScene     statut;
    private List<String>    tags;
    private List<Long>      personnagesIds;


    public Scene() {
        this.tags           = new ArrayList<>();
        this.personnagesIds = new ArrayList<>();
        this.statut         = StatutScene.BROUILLON;
    }

    public Scene(Long storyId, String titre, int ordre, String lieu) {
        this.storyId        = storyId;
        this.titre          = titre;
        this.ordre          = ordre;
        this.lieu           = lieu;
        this.statut         = StatutScene.BROUILLON;
        this.tags           = new ArrayList<>();
        this.personnagesIds = new ArrayList<>();
    }


    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag.trim())) {
            tags.add(tag.trim());
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public String getTitle() {
        return titre;
    }

    public void setTitle(String titre) {
        this.titre = titre;
    }

    public int getOrder() {
        return ordre;
    }

    public void setOrder(int ordre) {
        this.ordre = ordre;
    }

    public String getLocation() {
        return lieu;
    }

    public void setLocation(String lieu) {
        this.lieu = lieu;
    }

    public String getMoment() {
        return moment;
    }

    public void setMoment(String moment) {
        this.moment = moment;
    }

    public String getSummary() {
        return resume;
    }

    public void setSummary(String resume) {
        this.resume = resume;
    }

    public StatutScene getStatus() {
        return statut;
    }

    public void setStatus(StatutScene statut) {
        this.statut = statut;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Long> getCharacterIds() {
        return personnagesIds;
    }

    public void setCharacterIds(List<Long> personnagesIds) {
        this.personnagesIds = personnagesIds;
    }

    @Override
    public String toString() {
        return ordre + ". " + titre + " [" + statut.getLabel() + "]";
    }
}
