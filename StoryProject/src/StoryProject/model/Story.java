package StoryProject.model;

import java.util.ArrayList;
import java.util.List;

public class Story {

    public enum GenreHistoire {
        Fantasy,
        ScienceFiction,
        Fantastique,
        Aventure,
        Action,
        Romance,
        Drame,
        Comedie,
        Policier,
        Thriller,
        Horreur,
        Historique,
        Dystopie,
        Utopie,
        SliceOfLife
    }

    private Long             id;
    private String           titre;
    private String           auteur;
    private String           resume;
    private List<Personnage> personnages;
    private List<Scene>      scenes;
    private GenreHistoire    genreHistoire;

    public Story() {
        this.personnages = new ArrayList<>();
        this.scenes      = new ArrayList<>();
    }

    public Story(String titre, GenreHistoire genreHistoire, String auteur, String resume) {
        this.titre         = titre;
        this.genreHistoire = genreHistoire;
        this.auteur        = auteur;
        this.resume        = resume;
        this.personnages   = new ArrayList<>();
        this.scenes        = new ArrayList<>();
    }

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public String getTitle()               { return titre; }
    public void setTitle(String titre)     { this.titre = titre; }

    public String getAuthor()              { return auteur; }
    public void setAuthor(String auteur)   { this.auteur = auteur; }

    public String getSummary()             { return resume; }
    public void setSummary(String resume)  { this.resume = resume; }

    public GenreHistoire getStoryGenre()   { return genreHistoire; }
    public void setStoryGenre(GenreHistoire g) { this.genreHistoire = g; }

    public List<Personnage> getCharacters()       { return personnages; }
    public void setCharacters(List<Personnage> p) { this.personnages = p; }

    public List<Scene> getScenes()            { return scenes; }
    public void setScenes(List<Scene> scenes) { this.scenes = scenes; }

    public boolean validate() {
        return titre         != null && !titre.trim().isEmpty()
                && auteur        != null && !auteur.trim().isEmpty()
                && genreHistoire != null;
    }

    @Override
    public String toString() {
        return titre + " - " + genreHistoire + " - " + auteur;
    }
}