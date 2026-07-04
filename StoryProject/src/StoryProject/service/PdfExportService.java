package StoryProject.service;

import StoryProject.model.Scene;
import StoryProject.model.Story;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Génère un document PDF contenant les scènes publiées d'une histoire.
 * Ne fait aucun accès à la base de données : elle reçoit des objets déjà chargés
 * et se contente de les mettre en page, ce qui la rend indépendante de l'interface
 * et facile à appeler depuis un traitement en arrière-plan.
 */
public class PdfExportService {

    private static final float MARGE          = 50f;
    private static final float LARGEUR_PAGE    = PDRectangle.A4.getWidth();
    private static final float HAUTEUR_PAGE    = PDRectangle.A4.getHeight();
    private static final float LARGEUR_TEXTE   = LARGEUR_PAGE - 2 * MARGE;
    private static final float TAILLE_TITRE    = 20f;
    private static final float TAILLE_SOUS_TITRE = 14f;
    private static final float TAILLE_TEXTE    = 11f;
    private static final float INTERLIGNE      = 15f;

    private final PDFont policeTitre    = PDType1Font.HELVETICA_BOLD;
    private final PDFont policeTexte    = PDType1Font.HELVETICA;
    private final PDFont policeItalique = PDType1Font.HELVETICA_OBLIQUE;

    private PDDocument document;
    private PDPageContentStream contenu;
    private float positionY;

    /**
     * Construit le PDF de l'histoire et l'enregistre à l'emplacement donné.
     *
     * @param story           l'histoire exportée
     * @param scenesPubliees  uniquement les scènes dont le statut est PUBLIE
     * @param nomsPersonnages association id de personnage -> nom affichable, pour ne pas
     *                        avoir à recharger les personnages depuis ce service
     * @param destination     fichier .pdf de destination sur la machine
     */
    public void exportStory(Story story, List<Scene> scenesPubliees,
                             Map<Long, String> nomsPersonnages, File destination) throws IOException {
        document = new PDDocument();
        try {
            nouvellePage();

            ecrireTitre(story.getTitle());
            ecrireLigne("Auteur : " + story.getAuthor(), policeItalique, TAILLE_TEXTE);
            positionY -= INTERLIGNE;

            if (scenesPubliees.isEmpty()) {
                ecrireLigne("Aucune scène publiée pour cette histoire.", policeTexte, TAILLE_TEXTE);
            } else {
                for (Scene scene : scenesPubliees) {
                    ecrireScene(scene, nomsPersonnages);
                }
            }

            contenu.close();
            document.save(destination);
        } finally {
            document.close();
        }
    }

    private void ecrireScene(Scene scene, Map<Long, String> nomsPersonnages) throws IOException {
        assurerPlace(4 * INTERLIGNE);

        ecrireLigne(scene.getOrder() + ". " + scene.getTitle(), policeTitre, TAILLE_SOUS_TITRE);

        String lieu   = scene.getLocation()   == null ? "-" : scene.getLocation();
        String moment = scene.getMoment()     == null ? "-" : scene.getMoment();
        ecrireLigne("Lieu : " + lieu + "      Moment : " + moment, policeTexte, TAILLE_TEXTE);

        String personnages = construirePersonnages(scene, nomsPersonnages);
        ecrireLignesMultiples("Personnages présents : " + personnages, policeTexte, TAILLE_TEXTE);

        String texte = (scene.getSummary() == null || scene.getSummary().isBlank())
                ? "(aucun contenu renseigné)"
                : scene.getSummary();
        ecrireLignesMultiples(texte, policeTexte, TAILLE_TEXTE);

        positionY -= INTERLIGNE;
    }

    /** Transforme les ids de personnages liés à la scène en une liste de noms lisibles. */
    private String construirePersonnages(Scene scene, Map<Long, String> nomsPersonnages) {
        List<Long> ids = scene.getCharacterIds();
        if (ids == null || ids.isEmpty()) {
            return "aucun";
        }
        List<String> noms = new ArrayList<>();
        for (Long id : ids) {
            noms.add(nomsPersonnages.getOrDefault(id, "Personnage #" + id));
        }
        return String.join(", ", noms);
    }

    // ---------- Utilitaires bas niveau d'écriture PDF ----------

    private void nouvellePage() throws IOException {
        if (contenu != null) {
            contenu.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        contenu = new PDPageContentStream(document, page);
        positionY = HAUTEUR_PAGE - MARGE;
    }

    /** Change de page si moins de "hauteurNecessaire" points ne restent avant le bas de page. */
    private void assurerPlace(float hauteurNecessaire) throws IOException {
        if (positionY - hauteurNecessaire < MARGE) {
            nouvellePage();
        }
    }

    private void ecrireTitre(String texte) throws IOException {
        assurerPlace(TAILLE_TITRE + INTERLIGNE);
        contenu.beginText();
        contenu.setFont(policeTitre, TAILLE_TITRE);
        contenu.newLineAtOffset(MARGE, positionY);
        contenu.showText(texte);
        contenu.endText();
        positionY -= TAILLE_TITRE + INTERLIGNE;
    }

    private void ecrireLigne(String texte, PDFont police, float taille) throws IOException {
        assurerPlace(INTERLIGNE);
        contenu.beginText();
        contenu.setFont(police, taille);
        contenu.newLineAtOffset(MARGE, positionY);
        contenu.showText(texte);
        contenu.endText();
        positionY -= INTERLIGNE;
    }

    /** Découpe un texte trop long en plusieurs lignes qui tiennent dans la largeur de la page. */
    private void ecrireLignesMultiples(String texte, PDFont police, float taille) throws IOException {
        for (String ligne : decouperEnLignes(texte, police, taille)) {
            ecrireLigne(ligne, police, taille);
        }
    }

    private List<String> decouperEnLignes(String texte, PDFont police, float taille) throws IOException {
        List<String> lignes = new ArrayList<>();
        String[] mots = texte.split(" ");
        StringBuilder ligneActuelle = new StringBuilder();

        for (String mot : mots) {
            String essai = ligneActuelle.isEmpty() ? mot : ligneActuelle + " " + mot;
            float largeur = police.getStringWidth(essai) / 1000 * taille;
            if (largeur > LARGEUR_TEXTE && !ligneActuelle.isEmpty()) {
                lignes.add(ligneActuelle.toString());
                ligneActuelle = new StringBuilder(mot);
            } else {
                ligneActuelle = new StringBuilder(essai);
            }
        }
        if (!ligneActuelle.isEmpty()) {
            lignes.add(ligneActuelle.toString());
        }
        return lignes;
    }
}
