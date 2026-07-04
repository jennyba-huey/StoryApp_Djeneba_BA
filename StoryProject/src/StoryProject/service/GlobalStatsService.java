package StoryProject.service;

import StoryProject.model.Personnage;
import StoryProject.model.Scene;
import StoryProject.model.StatistiquesGlobales;
import StoryProject.model.StatutScene;
import StoryProject.model.Story;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Calcule les statistiques globales de tout l'atelier, toutes histoires confondues. */
public class GlobalStatsService {

    private final StoryService      storyService;
    private final SceneService      sceneService;
    private final PersonnageService personnageService;

    public GlobalStatsService(StoryService storyService,
                               SceneService sceneService,
                               PersonnageService personnageService) {
        this.storyService      = storyService;
        this.sceneService      = sceneService;
        this.personnageService = personnageService;
    }

    /** Parcourt toutes les histoires et agrège les chiffres. */
    public StatistiquesGlobales compute() throws SQLException {
        List<Story> histoires = storyService.getAllStories();

        int sceneCount = 0;
        int characterCount = 0;
        int nbTermine = 0;

        Map<String, Integer> scenesByStory = new LinkedHashMap<>();

        Map<StatutScene, Long> statuts = new EnumMap<>(StatutScene.class);
        for (StatutScene s : StatutScene.values()) statuts.put(s, 0L);

        Map<Personnage.Role, Long> roles = new EnumMap<>(Personnage.Role.class);
        for (Personnage.Role r : Personnage.Role.values()) roles.put(r, 0L);

        Map<String, Long> tags = new HashMap<>();

        for (Story histoire : histoires) {
            List<Scene> scenes = sceneService.getScenesByStory(histoire.getId());
            scenesByStory.put(histoire.getTitle(), scenes.size());
            sceneCount += scenes.size();

            for (Scene scene : scenes) {
                if (scene.getStatus() != null) {
                    statuts.merge(scene.getStatus(), 1L, Long::sum);
                    if (scene.getStatus() == StatutScene.TERMINE) nbTermine++;
                }
                if (scene.getTags() != null) {
                    for (String tag : scene.getTags()) {
                        if (tag == null) continue;
                        String cle = tag.trim().toLowerCase();
                        if (!cle.isEmpty()) tags.merge(cle, 1L, Long::sum);
                    }
                }
            }

            List<Personnage> personnages = personnageService.getCharactersByStory(histoire.getId());
            characterCount += personnages.size();
            for (Personnage p : personnages) {
                if (p.getRole() != null) roles.merge(p.getRole(), 1L, Long::sum);
            }
        }

        int completionPercentage = (sceneCount == 0) ? 0 : (int) Math.round(100.0 * nbTermine / sceneCount);

        // Top 8 des mots-clés, triés du plus fréquent au moins fréquent
        Map<String, Long> topTags = tags.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        return new StatistiquesGlobales(
                histoires.size(), characterCount, sceneCount, completionPercentage,
                scenesByStory, statuts, roles, topTags);
    }
}
