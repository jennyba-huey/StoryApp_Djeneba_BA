package StoryProject.service;

import StoryProject.model.Story;
import StoryProject.repository.StoryRepository;

import java.sql.SQLException;
import java.util.List;

public class StoryService {

    private final StoryRepository storyRepository;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    public Story createStory(String titre, Story.GenreHistoire genreHistoire, String auteur, String resume) throws SQLException {
        Story story = new Story(titre, genreHistoire, auteur, resume);
        if (!story.validate()) {
            throw new IllegalArgumentException("Le titre et l'auteur sont obligatoires");
        }
        storyRepository.save(story);
        return story;
    }

    public List<Story> getAllStories() throws SQLException {
        return storyRepository.findAll();
    }

    public Story getStoryById(Long id) throws SQLException {
        return storyRepository.findById(id);
    }

    public void updateStory(Story story) throws SQLException {
        if (!story.validate()) {
            throw new IllegalArgumentException("Le titre et l'auteur sont obligatoires");
        }
        storyRepository.update(story);
    }

    public void deleteStory(Long id) throws SQLException {
        storyRepository.delete(id);
    }

    public List<Story> findByTitle(String motCle) throws SQLException {
        return storyRepository.findByTitle(motCle);
    }
}