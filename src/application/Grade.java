package application;

import javafx.beans.property.*;

public class Grade {
    private final IntegerProperty gradeId;
    private final IntegerProperty enrollmentId;
    private final StringProperty  studentId;
    private final StringProperty  studentName;
    private final StringProperty  courseId;
    private final StringProperty  courseName;
    private final StringProperty  grade;
    private final StringProperty  gradePoint;   // "4.0", "3.7" etc — for instructor table
    private final StringProperty  studentGpa;   // cumulative GPA — shown in instructor table

    // Full constructor (used by Gradecontroller)
    public Grade(int gradeId, int enrollmentId,
                 String studentId, String studentName,
                 String courseId,  String courseName,
                 String grade, String gradePoint, String studentGpa) {
        this.gradeId      = new SimpleIntegerProperty(gradeId);
        this.enrollmentId = new SimpleIntegerProperty(enrollmentId);
        this.studentId    = new SimpleStringProperty(studentId);
        this.studentName  = new SimpleStringProperty(studentName);
        this.courseId     = new SimpleStringProperty(courseId);
        this.courseName   = new SimpleStringProperty(courseName);
        this.grade        = new SimpleStringProperty(grade);
        this.gradePoint   = new SimpleStringProperty(gradePoint);
        this.studentGpa   = new SimpleStringProperty(studentGpa);
    }

    // Short constructor (used by StudentDashboardcontroller)
    public Grade(int gradeId, int enrollmentId,
                 String studentId, String studentName,
                 String courseId,  String courseName,
                 String grade) {
        this(gradeId, enrollmentId, studentId, studentName,
             courseId, courseName, grade, "—", "—");
    }

    public int    getGradeId()      { return gradeId.get(); }
    public int    getEnrollmentId() { return enrollmentId.get(); }
    public String getStudentId()    { return studentId.get(); }
    public String getStudentName()  { return studentName.get(); }
    public String getCourseId()     { return courseId.get(); }
    public String getCourseName()   { return courseName.get(); }
    public String getGrade()        { return grade.get(); }
    public String getGradePoint()   { return gradePoint.get(); }
    public String getStudentGpa()   { return studentGpa.get(); }
    public void   setGrade(String g){ grade.set(g); }

    public IntegerProperty gradeIdProperty()      { return gradeId; }
    public IntegerProperty enrollmentIdProperty() { return enrollmentId; }
    public StringProperty  studentIdProperty()    { return studentId; }
    public StringProperty  studentNameProperty()  { return studentName; }
    public StringProperty  courseIdProperty()     { return courseId; }
    public StringProperty  courseNameProperty()   { return courseName; }
    public StringProperty  gradeProperty()        { return grade; }
    public StringProperty  gradePointProperty()   { return gradePoint; }
    public StringProperty  studentGpaProperty()   { return studentGpa; }
}
