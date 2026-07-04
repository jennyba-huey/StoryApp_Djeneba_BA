package StoryProject.model;

public class Personnage {

    public enum Genre {
        Homme,
        Femme,
        NonBinaire
    }

    public enum Role {
        AntagonistePrincipal,
        AntagonisteSecondaire,
        ProtagonistePrincipal,
        ProtagonisteSecondaire,
        Figurant
    }

    private Long   id;
    private Long   storyId;
    private String prenom;
    private String nom;
    private int    age;
    private Role   role;
    private String descriptionPhysique;
    private Genre  genre;
    private String descriptionPersonnalite;

    public Personnage() {}

    public Personnage(Long id, Long storyId, String prenom, String nom, int age,
                      Role role, String descriptionPhysique, Genre genre,
                      String descriptionPersonnalite) {
        this.id                     = id;
        this.storyId                = storyId;
        this.prenom                 = prenom;
        this.nom                    = nom;
        this.age                    = age;
        this.role                   = role;
        this.descriptionPhysique    = descriptionPhysique;
        this.genre                  = genre;
        this.descriptionPersonnalite = descriptionPersonnalite;
    }

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public Long getStoryId()               { return storyId; }
    public void setStoryId(Long storyId)   { this.storyId = storyId; }

    public String getFirstName()           { return prenom; }
    public void setFirstName(String prenom){ this.prenom = prenom; }

    public String getLastName()            { return nom; }
    public void setLastName(String nom)    { this.nom = nom; }

    public int getAge()                    { return age; }
    public void setAge(int age)            { this.age = age; }

    public Role getRole()                  { return role; }
    public void setRole(Role role)         { this.role = role; }

    public String getPhysicalDescription()       { return descriptionPhysique; }
    public void setPhysicalDescription(String d) { this.descriptionPhysique = d; }

    public Genre getGender()               { return genre; }
    public void setGender(Genre genre)     { this.genre = genre; }

    public String getPersonality()       { return descriptionPersonnalite; }
    public void setPersonality(String d) { this.descriptionPersonnalite = d; }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + role + ")";
    }
}