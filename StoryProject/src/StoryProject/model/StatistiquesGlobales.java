package StoryProject.model;

import java.util.Map;

/** Statistiques globales de tout l'atelier, toutes histoires confondues. */
public record StatistiquesGlobales(
        int storyCount,
        int characterCount,
        int sceneCount,
        int completionPercentage,
        Map<String, Integer> scenesByStory,
        Map<StatutScene, Long> statusDistribution,
        Map<Personnage.Role, Long> charactersByRole,
        Map<String, Long> topTags
) {}
