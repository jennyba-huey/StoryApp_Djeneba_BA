package StoryProject.repository;

import StoryProject.model.Scene;
import StoryProject.model.Personnage;
import StoryProject.model.StatutScene;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SceneRepository extends AbstractRepository<Scene, Long> {

    private static final String SQL_SAVE = "INSERT INTO scene (story_id, titre, ordre, lieu, moment, resume, statut, tags) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM scene WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM scene ORDER BY ordre";
    private static final String SQL_FIND_BY_STORY = "SELECT * FROM scene WHERE story_id = ? ORDER BY ordre";
    private static final String SQL_UPDATE = "UPDATE scene SET titre = ?, ordre = ?, lieu = ?, moment = ?, resume = ?, statut = ?, tags = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM scene WHERE id = ?";
    private static final String SQL_FIND_BY_STATUT = "SELECT * FROM scene WHERE statut = ? ORDER BY ordre";
    private static final String SQL_FIND_BY_PERSONNAGE = "SELECT s.* FROM scene s JOIN scene_personnage sp ON s.id = sp.scene_id WHERE sp.personnage_id = ? ORDER BY s.ordre";
    private static final String SQL_SEARCH_KEYWORD = "SELECT * FROM scene WHERE titre LIKE ? OR resume LIKE ? ORDER BY ordre";

    private static final String SQL_LIER_PERSONNAGE = "INSERT INTO scene_personnage (scene_id, personnage_id) VALUES (?, ?)";
    private static final String SQL_DELIER_PERSONNAGE = "DELETE FROM scene_personnage WHERE scene_id = ? AND personnage_id = ?";
    private static final String SQL_DELETE_LIENS_SCENE = "DELETE FROM scene_personnage WHERE scene_id = ?";
    private static final String SQL_PERSONNAGES_SCENE = "SELECT p.* FROM personnage p JOIN scene_personnage sp ON p.id = sp.personnage_id WHERE sp.scene_id = ?";



    public SceneRepository(DBManager db) {
        super(db);
    }

    @Override
    public void save(Scene scene) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, scene.getStoryId());
            stmt.setString(2, scene.getTitle());
            stmt.setInt(3, scene.getOrder());
            stmt.setString(4, scene.getLocation());
            stmt.setString(5, scene.getMoment());
            stmt.setString(6, scene.getSummary());
            stmt.setString(7, scene.getStatus().name());
            stmt.setString(8, String.join(",", scene.getTags()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    scene.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Scene findById(Long id) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Scene> findAll() throws SQLException {
        List<Scene> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resultats.add(mapResultSet(rs));
            }
        }
        return resultats;
    }

    @Override
    public void update(Scene scene) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, scene.getTitle());
            stmt.setInt(2, scene.getOrder());
            stmt.setString(3, scene.getLocation());
            stmt.setString(4, scene.getMoment());
            stmt.setString(5, scene.getSummary());
            stmt.setString(6, scene.getStatus().name());
            stmt.setString(7, String.join(",", scene.getTags()));
            stmt.setLong(8, scene.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        try (PreparedStatement stmtLiens = getConnection().prepareStatement(SQL_DELETE_LIENS_SCENE)) {
            stmtLiens.setLong(1, id);
            stmtLiens.executeUpdate();
        }
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_DELETE)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Scene> findByStory(Long storyId) throws SQLException {
        List<Scene> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_STORY)) {
            stmt.setLong(1, storyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        }
        return resultats;
    }

    public List<Scene> findByStatus(StatutScene statut) throws SQLException {
        List<Scene> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_STATUT)) {
            stmt.setString(1, statut.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        }
        return resultats;
    }

    public List<Scene> findByCharacter(Long personnageId) throws SQLException {
        List<Scene> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_PERSONNAGE)) {
            stmt.setLong(1, personnageId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        }
        return resultats;
    }

    public List<Scene> searchByKeyword(String motCle) throws SQLException {
        List<Scene> resultats = new ArrayList<>();
        String motCleLike = "%" + motCle + "%";
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_SEARCH_KEYWORD)) {
            stmt.setString(1, motCleLike);
            stmt.setString(2, motCleLike);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        }
        return resultats;
    }

    public void linkCharacter(Long sceneId, Long personnageId) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_LIER_PERSONNAGE)) {
            stmt.setLong(1, sceneId);
            stmt.setLong(2, personnageId);
            stmt.executeUpdate();
        }
    }

    public void unlinkCharacter(Long sceneId, Long personnageId) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_DELIER_PERSONNAGE)) {
            stmt.setLong(1, sceneId);
            stmt.setLong(2, personnageId);
            stmt.executeUpdate();
        }
    }

    public List<Personnage> findCharactersByScene(Long sceneId) throws SQLException {
        List<Personnage> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_PERSONNAGES_SCENE)) {
            stmt.setLong(1, sceneId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapPersonnage(rs));
                }
            }
        }
        return resultats;
    }

    private Scene mapResultSet(ResultSet rs) throws SQLException {
        Scene scene = new Scene();
        scene.setId(rs.getLong("id"));
        scene.setStoryId(rs.getLong("story_id"));
        scene.setTitle(rs.getString("titre"));
        scene.setOrder(rs.getInt("ordre"));
        scene.setLocation(rs.getString("lieu"));
        scene.setMoment(rs.getString("moment"));
        scene.setSummary(rs.getString("resume"));
        scene.setStatus(StatutScene.valueOf(rs.getString("statut")));

        String tagsCsv = rs.getString("tags");
        if (tagsCsv != null && !tagsCsv.isEmpty()) {
            scene.setTags(new ArrayList<>(Arrays.asList(tagsCsv.split(","))));
        }
        return scene;
    }

    private Personnage mapPersonnage(ResultSet rs) throws SQLException {
        Personnage personnage = new Personnage();
        personnage.setId(rs.getLong("id"));
        personnage.setStoryId(rs.getLong("story_id"));
        personnage.setFirstName(rs.getString("prenom"));
        personnage.setLastName(rs.getString("nom"));
        personnage.setAge(rs.getInt("age"));
        personnage.setRole(Personnage.Role.valueOf(rs.getString("role")));
        personnage.setPhysicalDescription(rs.getString("description_physique"));
        personnage.setGender(Personnage.Genre.valueOf(rs.getString("genre")));
        personnage.setPersonality(rs.getString("description_personnalite"));
        return personnage;
    }
}