package StoryProject.service;

import StoryProject.model.Scene;
import StoryProject.model.StatutScene;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Recherche et filtrage des scènes, en mémoire, à partir de la liste déjà chargée. */
public class SceneFilterService {

    /** Garde uniquement les scènes d'un statut donné (statut null = aucun filtre). */
    public List<Scene> filterByStatus(List<Scene> scenes, StatutScene statut) {
        if (scenes == null) return new ArrayList<>();
        if (statut == null) return new ArrayList<>(scenes);
        return scenes.stream()
                .filter(s -> s.getStatus() == statut)
                .collect(Collectors.toList());
    }

    /** Garde uniquement les scènes où apparaît un personnage donné (id null = aucun filtre). */
    public List<Scene> filterByCharacter(List<Scene> scenes, Long personnageId) {
        if (scenes == null) return new ArrayList<>();
        if (personnageId == null) return new ArrayList<>(scenes);
        return scenes.stream()
                .filter(s -> s.getCharacterIds() != null
                        && s.getCharacterIds().contains(personnageId))
                .collect(Collectors.toList());
    }

    /** Recherche un mot-clé dans le titre, le résumé et les tags (mot-clé vide = aucun filtre). */
    public List<Scene> searchByKeyword(List<Scene> scenes, String motCle) {
        if (scenes == null) return new ArrayList<>();
        if (motCle == null || motCle.isBlank()) return new ArrayList<>(scenes);
        String q = motCle.toLowerCase().trim();
        return scenes.stream()
                .filter(s -> matches(s, q))
                .collect(Collectors.toList());
    }

    /** Applique les trois critères ensemble (un critère null ou vide est ignoré). */
    public List<Scene> applyFilters(List<Scene> scenes, StatutScene statut, Long personnageId, String motCle) {
        if (scenes == null) return new ArrayList<>();
        String q = (motCle == null) ? null : motCle.toLowerCase().trim();
        return scenes.stream()
                .filter(s -> statut == null || s.getStatus() == statut)
                .filter(s -> personnageId == null
                        || (s.getCharacterIds() != null && s.getCharacterIds().contains(personnageId)))
                .filter(s -> q == null || q.isBlank() || matches(s, q))
                .collect(Collectors.toList());
    }

    /** Vrai si le mot-clé (déjà en minuscules) apparaît dans le titre, le résumé ou un tag. */
    private boolean matches(Scene s, String q) {
        if (s.getTitle() != null && s.getTitle().toLowerCase().contains(q)) return true;
        if (s.getSummary() != null && s.getSummary().toLowerCase().contains(q)) return true;
        return s.getTags() != null
                && s.getTags().stream().anyMatch(t -> t != null && t.toLowerCase().contains(q));
    }
}
