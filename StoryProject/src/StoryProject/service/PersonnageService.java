package StoryProject.service;

import StoryProject.model.Personnage;
import StoryProject.model.Story;
import StoryProject.repository.PersonnageRepository;

import java.sql.SQLException;
import java.util.List;

public class PersonnageService {

    private final PersonnageRepository personnageRepository;

    public PersonnageService(PersonnageRepository personnageRepository) {
        this.personnageRepository = personnageRepository;
    }

    public void addCharacter(Personnage personnage, Story story) throws SQLException {
        if (!isNameValid(personnage.getLastName())) {
            throw new IllegalArgumentException("Le nom du personnage ne peut pas être vide");
        }
        personnage.setStoryId(story.getId());
        personnageRepository.save(personnage);
    }

    public List<Personnage> getCharactersByStory(Long storyId) throws SQLException {
        return personnageRepository.findByStory(storyId);
    }

    public Personnage getCharacterById(Long id) throws SQLException {
        return personnageRepository.findById(id);
    }

    public void updateCharacter(Personnage personnage) throws SQLException {
        if (!isNameValid(personnage.getLastName())) {
            throw new IllegalArgumentException("Le nom du personnage ne peut pas être vide");
        }
        personnageRepository.update(personnage);
    }

    public void deleteCharacter(Long id) throws SQLException {
        personnageRepository.delete(id);
    }

    public List<Personnage> findByRole(Personnage.Role role) throws SQLException {
        return personnageRepository.findByRole(role);
    }

    public boolean isNameValid(String nom) {
        return nom != null && !nom.trim().isEmpty();
    }
}