package StoryProject.model;

public enum StatutScene {

    BROUILLON("Brouillon"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    PUBLIE("Publié");

    private final String libelle;

    StatutScene(String libelle) {
        this.libelle = libelle;
    }

    public String getLabel() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}