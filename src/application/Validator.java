package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralised validation utility.
 * Returns a list of error messages — empty list means everything is valid.
 */
public class Validator {

    // ── Phone ──────────────────────────────────────────────────
    /** Must be exactly 10 digits, numbers only */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return null; // phone is optional — only validate if provided
        String p = phone.trim();
        if (!p.matches("\\d{10}"))
            return "Phone must be exactly 10 digits (numbers only). Got: " + p;
        return null;
    }

    /** Check if phone already exists in the given table, excluding a specific ID */
    public static String checkPhoneDuplicate(String phone, String table,
                                             String idColumn, String excludeId) {
        if (phone == null || phone.trim().isEmpty()) return null;
        String sql = "SELECT " + idColumn + " FROM " + table +
                     " WHERE phone = ?" +
                     (excludeId != null ? " AND " + idColumn + " != ?" : "");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            if (excludeId != null) ps.setString(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return "Phone number " + phone.trim() +
                       " is already registered (used by " + rs.getString(1) + ").";
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Email ──────────────────────────────────────────────────
    /** Simple email format: must contain @ and a dot after @ */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return null; // email is optional
        String e = email.trim();
        if (!e.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            return "Invalid email format: " + e +
                   "  (expected format: name@domain.com)";
        return null;
    }

    /** Check if email already exists in the given table, excluding a specific ID */
    public static String checkEmailDuplicate(String email, String table,
                                             String idColumn, String excludeId) {
        if (email == null || email.trim().isEmpty()) return null;
        String sql = "SELECT " + idColumn + " FROM " + table +
                     " WHERE email = ?" +
                     (excludeId != null ? " AND " + idColumn + " != ?" : "");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            if (excludeId != null) ps.setString(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return "Email " + email.trim() +
                       " is already registered (used by " + rs.getString(1) + ").";
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Run all checks and return combined message ─────────────
    /**
     * Runs a list of validation checks.
     * Returns null if all pass, or a combined error message if any fail.
     */
    public static String runAll(String... checks) {
        List<String> errors = new ArrayList<>();
        for (String msg : checks)
            if (msg != null) errors.add("• " + msg);
        if (errors.isEmpty()) return null;
        return String.join("\n", errors);
    }
}
