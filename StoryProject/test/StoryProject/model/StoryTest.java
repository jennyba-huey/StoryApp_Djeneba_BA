package StoryProject.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoryTest {

    @Test
    void shouldValidateCompleteStory() {
        Story story = new Story("Le Secret de la Bibliothèque",
                Story.GenreHistoire.Thriller, "Djeneba Ba", "Résumé");

        assertTrue(story.validate());
    }

    @Test
    void shouldRejectStoryWithoutTitle() {
        Story story = new Story("", Story.GenreHistoire.Thriller, "Djeneba Ba", "Résumé");

        assertFalse(story.validate());
    }

    @Test
    void shouldRejectStoryWithoutAuthor() {
        Story story = new Story("Le Secret de la Bibliothèque",
                Story.GenreHistoire.Thriller, "", "Résumé");

        assertFalse(story.validate());
    }

    @Test
    void shouldRejectStoryWithoutGenre() {
        Story story = new Story("Le Secret de la Bibliothèque", null, "Djeneba Ba", "Résumé");

        assertFalse(story.validate());
    }
}
