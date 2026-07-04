package StoryProject.service;

import StoryProject.model.Scene;
import StoryProject.model.StatutScene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SceneFilterServiceTest {

    private SceneFilterService sceneFilterService;
    private List<Scene> scenes;

    @BeforeEach
    void setUp() {
        sceneFilterService = new SceneFilterService();

        Scene s1 = new Scene(1L, "La découverte du livre", 1, "Bibliothèque");
        s1.setStatus(StatutScene.BROUILLON);
        s1.setSummary("Emma découvre un livre ancien.");
        s1.setCharacterIds(List.of(10L));

        Scene s2 = new Scene(1L, "Recherche dans les archives", 2, "Salle des archives");
        s2.setStatus(StatutScene.EN_COURS);
        s2.setSummary("Emma et Lucas fouillent les archives.");
        s2.setCharacterIds(List.of(10L, 20L));

        Scene s3 = new Scene(1L, "Le témoignage de Victor", 3, "Université");
        s3.setStatus(StatutScene.EN_COURS);
        s3.setSummary("Victor apporte des informations.");
        s3.setCharacterIds(List.of(20L, 30L));

        scenes = new ArrayList<>(List.of(s1, s2, s3));
    }

    @Test
    void shouldFilterByStatus() {
        List<Scene> resultat = sceneFilterService.filterByStatus(scenes, StatutScene.EN_COURS);

        assertEquals(2, resultat.size());
    }

    @Test
    void shouldReturnAllScenesWhenStatusIsNull() {
        List<Scene> resultat = sceneFilterService.filterByStatus(scenes, null);

        assertEquals(3, resultat.size());
    }

    @Test
    void shouldFilterByCharacter() {
        List<Scene> resultat = sceneFilterService.filterByCharacter(scenes, 30L);

        assertEquals(1, resultat.size());
        assertEquals("Le témoignage de Victor", resultat.get(0).getTitle());
    }

    @Test
    void shouldSearchByKeywordInSummary() {
        List<Scene> resultat = sceneFilterService.searchByKeyword(scenes, "archives");

        assertEquals(1, resultat.size());
        assertEquals("Recherche dans les archives", resultat.get(0).getTitle());
    }

    @Test
    void shouldReturnEmptyListWhenKeywordNotFound() {
        List<Scene> resultat = sceneFilterService.searchByKeyword(scenes, "inconnu");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void shouldCombineAllFilters() {
        List<Scene> resultat = sceneFilterService.applyFilters(scenes, StatutScene.EN_COURS, 20L, "archives");

        assertEquals(1, resultat.size());
        assertEquals("Recherche dans les archives", resultat.get(0).getTitle());
    }
}
