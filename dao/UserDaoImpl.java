package com.hsbc.application.Dao;

import com.hsbc.application.Dao.UserDao;
import com.hsbc.application.config.DBConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class UserDaoImpl implements UserDao{

    public static void main(String[] args) {
        System.out.println(hashPassword("hey"));
    }

    private static final Key SIGNING_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static String hashPassword(String password) {
        String compactJws = Jwts.builder()
                .setSubject(password)
                .signWith(SIGNING_KEY)
                .compact();

        return Base64.getEncoder().encodeToString(compactJws.getBytes());
    }
    @Override
    public void registerUser(String userName, String password, String role) {
        try (Connection conn = DBConfig.connection()) {
//            String hashedPassword = hashPassword(password);
            String sql = "INSERT INTO users (userName, hashedPassword, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userName);
//            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
//            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String loginUser(String userName, String password) {
        try (Connection conn = DBConfig.connection()) {
            String sql = "SELECT hashedPassword, role FROM users WHERE userName = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHashedPassword = rs.getString("hashedPassword");
                if (storedHashedPassword.equals(hashPassword(password))) {
                    // Login successful, get the role
                    String role = rs.getString("role");

                    // Store the login time in the database
                    storeLoginTime(conn, userName);

                    // Generate JWT token with role
                    String jwt = generateJwtToken(userName, role);
                    return jwt; // Return the token
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private String generateJwtToken(String userName, String role) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(SIGNING_KEY)
                .compact();
    }

    public void restrictedEndpoint(String jwt) {
        // Parse and validate JWT
        String role = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .get("role", String.class);

        // Check if the role is authorized to access the restricted endpoint
        if ("ADMIN".equals(role)) {
            // Proceed with admin-specific functionality
            System.out.println("Access granted. Role: " + role);
        } else {
            throw new RuntimeException("Unauthorized access");
        }
    }








    // Store the login time in the database
    private void storeLoginTime(Connection conn, String userName) {
        try {
            String sql = "UPDATE users SET lastLogin = ? WHERE userName = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.setString(2, userName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }





}
