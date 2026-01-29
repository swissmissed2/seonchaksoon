package com.example.seonchaksoon.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
@RequiredArgsConstructor
public class NamedLockRepository {

    private final DataSource dataSource;

    public Connection tryLock(String lockName, int timeoutSeconds) {
        try {
            Connection conn = dataSource.getConnection();

            try (PreparedStatement ps = conn.prepareStatement("SELECT GET_LOCK(?, ?)")) {
                ps.setString(1, lockName);
                ps.setInt(2, timeoutSeconds);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int result = rs.getInt(1); // 1=성공, 0=timeout, NULL=에러
                    if (result == 1) return conn;
                }
            }

            conn.close();
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("GET_LOCK failed: " + lockName, e);
        }
    }

    public void unlock(Connection conn, String lockName) {
        if (conn == null) return;

        try (PreparedStatement ps = conn.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            ps.setString(1, lockName);
            ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException("RELEASE_LOCK failed: " + lockName, e);
        } finally {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
