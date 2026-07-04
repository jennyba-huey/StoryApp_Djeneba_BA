package StoryProject.service;

import StoryProject.model.Personnage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonnageServiceTest {

    private PersonnageService personnageService;

    @BeforeEach
    void setUp() {
        // Le repository n'est jamais sollicité ici : ces tests ne vérifient
        // que la validation métier, avant tout accès à la base.
        personnageService = new PersonnageService(null);
    }

    @Test
    void shouldAcceptValidName() {
        assertTrue(personnageService.isNameValid("Dupont"));
    }

    @Test
    void shouldRejectEmptyName() {
        assertFalse(personnageService.isNameValid(""));
    }

    @Test
    void shouldRejectBlankName() {
        assertFalse(personnageService.isNameValid("   "));
    }

    @Test
    void shouldRejectNullName() {
        assertFalse(personnageService.isNameValid(null));
    }

    @Test
    void shouldRejectCharacterWithoutName() {
        Personnage personnage = new Personnage();
        personnage.setFirstName("Alice");
        personnage.setLastName("");

        assertThrows(IllegalArgumentException.class,
                () -> personnageService.updateCharacter(personnage));
    }
}
