package StoryProject.model;

import java.util.Map;

/** Statistiques générales d'une histoire sélectionnée. */
public record StatistiquesHistoire(
        int characterCount,
        int sceneCount,
        Map<StatutScene, Long> scenesByStatus
) {}
