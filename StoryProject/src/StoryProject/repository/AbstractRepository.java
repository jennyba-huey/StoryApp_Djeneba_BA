package StoryProject.repository;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractRepository<T, ID> implements CrudRepository<T, ID> {

    protected final DBManager db;

    public AbstractRepository(DBManager db) {
        this.db = db;
    }

    protected Connection getConnection() throws SQLException {
        return db.getConnection();
    }
}