package application;

public class SessionManager {
    private static String username;
    private static String role;
    private static int    userId;

    // Instructor grade filter — set when instructor clicks a class in the tree
    private static String selectedClassId   = null;
    private static String selectedClassName = null;

    public static void setUser(String username, String role, int userId) {
        SessionManager.username = username;
        SessionManager.role     = role;
        SessionManager.userId   = userId;
    }

    public static String getUsername() { return username; }
    public static String getRole()     { return role; }
    public static int    getUserId()   { return userId; }

    public static void setSelectedClass(String classId, String className) {
        selectedClassId   = classId;
        selectedClassName = className;
    }
    public static String getSelectedClassId()   { return selectedClassId; }
    public static String getSelectedClassName() { return selectedClassName; }
    public static void clearSelectedClass() {
        selectedClassId   = null;
        selectedClassName = null;
    }

    public static void clear() {
        username          = null;
        role              = null;
        userId            = 0;
        selectedClassId   = null;
        selectedClassName = null;
    }
}
