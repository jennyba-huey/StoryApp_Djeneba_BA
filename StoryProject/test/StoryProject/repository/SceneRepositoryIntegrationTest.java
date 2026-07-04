package StoryProject.repository;

import StoryProject.model.Scene;
import StoryProject.model.StatutScene;
import StoryProject.model.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration : vérifie que SceneRepository lit et écrit correctement
 * dans la vraie base MySQL storyforge (nécessite une base accessible).
 */
class SceneRepositoryIntegrationTest {

    private StoryRepository storyRepository;
    private SceneRepository sceneRepository;
    private Story storyDeTest;

    @BeforeEach
    void setUp() throws SQLException {
        storyRepository = new StoryRepository(DBManager.getInstance());
        sceneRepository = new SceneRepository(DBManager.getInstance());

        storyDeTest = new Story("Histoire de test JUnit", Story.GenreHistoire.Thriller,
                "Test", "Histoire créée pour un test d'intégration.");
        storyRepository.save(storyDeTest);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // La suppression de l'histoire entraîne, par cascade, celle de ses scènes.
        storyRepository.delete(storyDeTest.getId());
    }

    @Test
    void shouldSaveAndReloadScene() throws SQLException {
        Scene scene = new Scene(storyDeTest.getId(), "Scène de test", 1, "Lieu de test");
        scene.setSummary("Résumé de test");
        scene.setStatus(StatutScene.BROUILLON);
        scene.setTags(new ArrayList<>(List.of("test", "junit")));

        sceneRepository.save(scene);
        Scene relue = sceneRepository.findById(scene.getId());

        assertNotNull(relue);
        assertEquals("Scène de test", relue.getTitle());
        assertEquals(StatutScene.BROUILLON, relue.getStatus());
    }

    @Test
    void shouldUpdateScene() throws SQLException {
        Scene scene = new Scene(storyDeTest.getId(), "Titre initial", 1, "Lieu initial");
        scene.setStatus(StatutScene.BROUILLON);
        sceneRepository.save(scene);

        scene.setTitle("Titre modifié");
        scene.setStatus(StatutScene.TERMINE);
        sceneRepository.update(scene);

        Scene relue = sceneRepository.findById(scene.getId());

        assertEquals("Titre modifié", relue.getTitle());
        assertEquals(StatutScene.TERMINE, relue.getStatus());
    }

    @Test
    void shouldDeleteScene() throws SQLException {
        Scene scene = new Scene(storyDeTest.getId(), "Scène à supprimer", 1, "Lieu");
        sceneRepository.save(scene);
        Long id = scene.getId();

        sceneRepository.delete(id);

        assertNull(sceneRepository.findById(id));
    }

    @Test
    void shouldFindScenesByStory() throws SQLException {
        Scene scene = new Scene(storyDeTest.getId(), "Scène liée", 1, "Lieu");
        sceneRepository.save(scene);

        List<Scene> scenes = sceneRepository.findByStory(storyDeTest.getId());

        assertEquals(1, scenes.size());
        assertEquals("Scène liée", scenes.get(0).getTitle());
    }
}
