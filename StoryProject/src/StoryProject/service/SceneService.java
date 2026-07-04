package StoryProject.service;

import StoryProject.model.Personnage;
import StoryProject.model.Scene;
import StoryProject.model.StatutScene;
import StoryProject.repository.PersonnageRepository;
import StoryProject.repository.SceneRepository;

import java.sql.SQLException;
import java.util.List;

public class SceneService {

    private final SceneRepository sceneRepository;
    private final PersonnageRepository personnageRepository;

    public SceneService(SceneRepository sceneRepository, PersonnageRepository personnageRepository) {
        this.sceneRepository = sceneRepository;
        this.personnageRepository = personnageRepository;
    }

    public Scene createScene(String titre, Long storyId, int ordre) throws SQLException {
        if (titre == null || titre.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de la scène ne peut pas être vide");
        }
        if (ordre < 0) {
            throw new IllegalArgumentException("L'ordre de la scène doit être positif");
        }
        Scene scene = new Scene();
        scene.setStoryId(storyId);
        scene.setTitle(titre);
        scene.setOrder(ordre);
        sceneRepository.save(scene);
        return scene;
    }

    public List<Scene> getScenesByStory(Long storyId) throws SQLException {
        return sceneRepository.findByStory(storyId);
    }

    public Scene getSceneById(Long id) throws SQLException {
        return sceneRepository.findById(id);
    }

    public void updateScene(Scene scene) throws SQLException {
        if (scene.getTitle() == null || scene.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de la scène ne peut pas être vide");
        }
        sceneRepository.update(scene);
    }

    public void deleteScene(Long id) throws SQLException {
        sceneRepository.delete(id);
    }

    public List<Scene> filterByStatus(StatutScene statut) throws SQLException {
        return sceneRepository.findByStatus(statut);
    }

    public List<Scene> filterByCharacter(Long personnageId) throws SQLException {
        if (personnageId == null) {
            throw new IllegalArgumentException("L'id du personnage ne peut pas être null");
        }
        if (personnageRepository.findById(personnageId) == null) {
            throw new IllegalArgumentException("Le personnage avec l'id " + personnageId + " n'existe pas");
        }
        return sceneRepository.findByCharacter(personnageId);
    }

    public List<Scene> searchByKeyword(String motCle) throws SQLException {
        if (motCle == null || motCle.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot-clé de recherche ne peut pas être vide");
        }
        return sceneRepository.searchByKeyword(motCle.trim());
    }

    public void linkCharacter(Long sceneId, Long personnageId) throws SQLException {
        if (personnageRepository.findById(personnageId) == null) {
            throw new IllegalArgumentException("Le personnage avec l'id " + personnageId + " n'existe pas");
        }
        sceneRepository.linkCharacter(sceneId, personnageId);
    }

    public void unlinkCharacter(Long sceneId, Long personnageId) throws SQLException {
        sceneRepository.unlinkCharacter(sceneId, personnageId);
    }

    public List<Personnage> getCharactersByScene(Long sceneId) throws SQLException {
        return sceneRepository.findCharactersByScene(sceneId);
    }
}