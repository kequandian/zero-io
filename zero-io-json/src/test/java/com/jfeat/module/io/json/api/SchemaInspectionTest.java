package com.jfeat.module.io.json.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = com.jfeat.AmApplication.class)
@ActiveProfiles("dev")
public class SchemaInspectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void printFrontPageColumns() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_front_page' ORDER BY ORDINAL_POSITION";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                System.out.println("Columns in t_front_page:");
                while (rs.next()) {
                    String name = rs.getString("COLUMN_NAME");
                    String type = rs.getString("COLUMN_TYPE");
                    String nullable = rs.getString("IS_NULLABLE");
                    String def = rs.getString("COLUMN_DEFAULT");
                    System.out.println("- " + name + " (" + type + ") nullable=" + nullable + ", default=" + def);
                }
            }
        }
    }

    @Test
    public void assertNotesColumnExists() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_front_page' AND COLUMN_NAME = 'notes'";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                int count = rs.getInt(1);
                if (count == 1) {
                    System.out.println("ASSERT: 'notes' column exists in t_front_page");
                } else {
                    throw new AssertionError("ASSERT FAILED: 'notes' column not found in t_front_page");
                }
            }
        }
    }

    @Test
    public void printServerAndSchemaInfo() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // server variables
            try (PreparedStatement ps = conn.prepareStatement("SELECT @@hostname AS hostname, @@port AS port, @@server_id AS server_id, @@version AS version");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Server info: hostname=" + rs.getString("hostname")
                            + ", port=" + rs.getInt("port")
                            + ", server_id=" + rs.getLong("server_id")
                            + ", version=" + rs.getString("version"));
                }
            }

            // DATABASE()
            try (PreparedStatement ps = conn.prepareStatement("SELECT DATABASE()");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("Connection info: database=" + rs.getString(1));
            }
            // CURRENT_USER()
            try (PreparedStatement ps = conn.prepareStatement("SELECT CURRENT_USER()");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("Connection info: current_user=" + rs.getString(1));
            }
            // USER()
            try (PreparedStatement ps = conn.prepareStatement("SELECT USER()");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("Connection info: user=" + rs.getString(1));
            }

            // show create table
            try (PreparedStatement ps = conn.prepareStatement("SHOW CREATE TABLE t_front_page");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String table = rs.getString(1);
                    String create = rs.getString(2);
                    System.out.println("SHOW CREATE TABLE " + table + ":\n" + create);
                }
            }
        }
    }

    @Test
    public void printReadOnlyStatus() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // read_only
            try (PreparedStatement ps = conn.prepareStatement("SELECT @@read_only");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("@@read_only=" + rs.getInt(1));
            }
            // super_read_only
            try (PreparedStatement ps = conn.prepareStatement("SELECT @@super_read_only");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("@@super_read_only=" + rs.getInt(1));
            }
        }
    }
}