package StoryProject.repository;

import StoryProject.model.Personnage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonnageRepository extends AbstractRepository<Personnage, Long> {

    private static final String SQL_SAVE        = "INSERT INTO personnage (story_id, prenom, nom, age, role, description_physique, genre, description_personnalite) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_FIND_BY_ID  = "SELECT * FROM personnage WHERE id = ?";
    private static final String SQL_FIND_ALL    = "SELECT * FROM personnage ORDER BY nom";
    private static final String SQL_FIND_BY_STORY = "SELECT * FROM personnage WHERE story_id = ? ORDER BY nom";
    private static final String SQL_UPDATE      = "UPDATE personnage SET prenom = ?, nom = ?, age = ?, role = ?, description_physique = ?, genre = ?, description_personnalite = ? WHERE id = ?";
    private static final String SQL_DELETE      = "DELETE FROM personnage WHERE id = ?";
    private static final String SQL_FIND_BY_ROLE = "SELECT * FROM personnage WHERE role = ?";

    public PersonnageRepository(DBManager db) {
        super(db);
    }

    @Override
    public void save(Personnage personnage) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1,   personnage.getStoryId());
            stmt.setString(2, personnage.getFirstName());
            stmt.setString(3, personnage.getLastName());
            stmt.setInt(4,    personnage.getAge());
            stmt.setString(5, personnage.getRole().name());
            stmt.setString(6, personnage.getPhysicalDescription());
            stmt.setString(7, personnage.getGender().name());
            stmt.setString(8, personnage.getPersonality());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    personnage.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Personnage findById(Long id) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
                return null;
            }
        }
    }

    @Override
    public List<Personnage> findAll() throws SQLException {
        List<Personnage> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) resultats.add(mapResultSet(rs));
        }
        return resultats;
    }

    @Override
    public void update(Personnage personnage) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, personnage.getFirstName());
            stmt.setString(2, personnage.getLastName());
            stmt.setInt(3,    personnage.getAge());
            stmt.setString(4, personnage.getRole().name());
            stmt.setString(5, personnage.getPhysicalDescription());
            stmt.setString(6, personnage.getGender().name());
            stmt.setString(7, personnage.getPersonality());
            stmt.setLong(8,   personnage.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_DELETE)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Personnage> findByStory(Long storyId) throws SQLException {
        List<Personnage> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_STORY)) {
            stmt.setLong(1, storyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultats.add(mapResultSet(rs));
            }
        }
        return resultats;
    }

    public List<Personnage> findByRole(Personnage.Role role) throws SQLException {
        List<Personnage> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_ROLE)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultats.add(mapResultSet(rs));
            }
        }
        return resultats;
    }

    private Personnage mapResultSet(ResultSet rs) throws SQLException {
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