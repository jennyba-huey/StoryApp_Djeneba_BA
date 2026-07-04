package StoryProject.repository;

import StoryProject.model.Story;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StoryRepository extends AbstractRepository<Story, Long> {

    private static final String SQL_SAVE = "INSERT INTO story (titre, auteur, resume, genre_histoire) VALUES (?, ?, ?, ?)";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM story WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM story ORDER BY id DESC";
    private static final String SQL_UPDATE = "UPDATE story SET titre = ?, auteur = ?, resume = ?, genre_histoire = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM story WHERE id = ?";
    private static final String SQL_FIND_BY_TITRE = "SELECT * FROM story WHERE titre LIKE ?";
    private static final String SQL_FIND_BY_AUTEUR = "SELECT * FROM story WHERE auteur = ?";
    private static final String SQL_COUNT_PERSONNAGES = "SELECT COUNT(*) FROM personnage WHERE story_id = ?";

    public StoryRepository(DBManager db) {
        super(db);
    }

    @Override
    public void save(Story story) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, story.getTitle());
            stmt.setString(2, story.getAuthor());
            stmt.setString(3, story.getSummary());
            stmt.setString(4, story.getStoryGenre() != null ? story.getStoryGenre().name() : null);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    story.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Story findById(Long id) throws SQLException {
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
    public List<Story> findAll() throws SQLException {
        List<Story> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resultats.add(mapResultSet(rs));
            }
        }
        return resultats;
    }

    @Override
    public void update(Story story) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, story.getTitle());
            stmt.setString(2, story.getAuthor());
            stmt.setString(3, story.getSummary());
            stmt.setString(4, story.getStoryGenre() != null ? story.getStoryGenre().name() : null);
            stmt.setLong(5, story.getId());
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

    public List<Story> findByTitle(String motCle) throws SQLException {
        List<Story> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_TITRE)) {
            stmt.setString(1, "%" + motCle + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        }
        return resultats;
    }

    public List<Story> findByAuthor(String auteur) throws SQLException {
        List<Story> resultats = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_FIND_BY_AUTEUR)) {
            stmt.setString(1, auteur);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        }
        return resultats;
    }

    public int countCharacters(Long storyId) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(SQL_COUNT_PERSONNAGES)) {
            stmt.setLong(1, storyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    private Story mapResultSet(ResultSet rs) throws SQLException {
        Story story = new Story();
        story.setId(rs.getLong("id"));
        story.setTitle(rs.getString("titre"));
        story.setAuthor(rs.getString("auteur"));
        story.setSummary(rs.getString("resume"));
        String genreHistoireStr = rs.getString("genre_histoire");
        System.out.println("DEBUG genreHistoire lu : " + genreHistoireStr);
        if (genreHistoireStr != null) {
            story.setStoryGenre(Story.GenreHistoire.valueOf(genreHistoireStr));
        }
        return story;
    }
}
