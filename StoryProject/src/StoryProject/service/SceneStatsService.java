package StoryProject.service;

import StoryProject.model.Personnage;
import StoryProject.model.Scene;
import StoryProject.model.StatistiquesHistoire;
import StoryProject.model.StatutScene;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Statistiques d'une histoire (personnages, scènes, répartition par statut), calculées en mémoire. */
public class SceneStatsService {

    /** Nombre total de personnages. */
    public int countCharacters(List<Personnage> personnages) {
        return personnages == null ? 0 : personnages.size();
    }

    /** Nombre total de scènes. */
    public int countScenes(List<Scene> scenes) {
        return scenes == null ? 0 : scenes.size();
    }

    /** Nombre de scènes par statut (tous les statuts sont présents, même à 0). */
    public Map<StatutScene, Long> scenesByStatus(List<Scene> scenes) {
        Map<StatutScene, Long> resultat = new EnumMap<>(StatutScene.class);
        for (StatutScene s : StatutScene.values()) resultat.put(s, 0L);
        if (scenes == null) return resultat;

        resultat.putAll(scenes.stream()
                .filter(s -> s.getStatus() != null)
                .collect(Collectors.groupingBy(Scene::getStatus, Collectors.counting())));
        return resultat;
    }

    /** Regroupe les statistiques générales en un seul objet. */
    public StatistiquesHistoire buildStats(List<Personnage> personnages, List<Scene> scenes) {
        return new StatistiquesHistoire(
                countCharacters(personnages),
                countScenes(scenes),
                scenesByStatus(scenes));
    }
}
