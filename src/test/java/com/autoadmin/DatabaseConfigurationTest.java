package com.autoadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class DatabaseConfigurationTest {
    @Test
    void convertsManagedMysqlDsnToJdbcAndRequiresTlsByDefault() {
        String jdbcUrl = DatabaseConfiguration.jdbcUrl("mysql://dealer:p%40ss@db.example:3306/autoadmin");
        assertEquals("jdbc:mysql://db.example:3306/autoadmin?sslMode=REQUIRED", jdbcUrl);
    }

    @Test
    void preservesExplicitMysqlTlsOptions() {
        String jdbcUrl = DatabaseConfiguration.jdbcUrl("mysql://dealer:secret@db.example/autoadmin?sslMode=VERIFY_IDENTITY");
        assertEquals("jdbc:mysql://db.example/autoadmin?sslMode=VERIFY_IDENTITY", jdbcUrl);
    }

    @Test
    void convertsNodeTlsObjectOptionToConnectorTlsMode() {
        String jdbcUrl = DatabaseConfiguration.jdbcUrl(
            "mysql://user:pass@db.example:4000/autoadmin?ssl={\"rejectUnauthorized\":true}");
        assertEquals("jdbc:mysql://db.example:4000/autoadmin?sslMode=VERIFY_IDENTITY", jdbcUrl);
    }

    @Test
    void leavesJdbcUrlsUnchanged() {
        String jdbcUrl = DatabaseConfiguration.jdbcUrl("jdbc:mysql://db.example/autoadmin?sslMode=REQUIRED");
        assertEquals("jdbc:mysql://db.example/autoadmin?sslMode=REQUIRED", jdbcUrl);
    }

    @Test
    void acceptsTheLocalStsMysqlUrl() {
        String jdbcUrl = DatabaseConfiguration.jdbcUrl("jdbc:mysql://localhost:3306/autoadmin");
        assertEquals("jdbc:mysql://localhost:3306/autoadmin", jdbcUrl);
    }

    @Test
    void rejectsNonMysqlUrls() {
        assertThrows(IllegalArgumentException.class,
            () -> DatabaseConfiguration.jdbcUrl("jdbc:postgresql://db.example/autoadmin"));
    }

    @Test
    void rejectsMissingDatabaseUrl() {
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfiguration.jdbcUrl(null));
    }
}
