package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Enrollment {
    private final IntegerProperty enrollmentId;
    private final StringProperty  studentId;
    private final StringProperty  studentName;
    private final StringProperty  courseId;
    private final StringProperty  courseName;
    private final StringProperty  semester;
    private final StringProperty  academicYear;

    public Enrollment(int enrollmentId, String studentId, String studentName,
                      String courseId, String courseName,
                      String semester, String academicYear) {
        this.enrollmentId = new SimpleIntegerProperty(enrollmentId);
        this.studentId    = new SimpleStringProperty(studentId);
        this.studentName  = new SimpleStringProperty(studentName);
        this.courseId     = new SimpleStringProperty(courseId);
        this.courseName   = new SimpleStringProperty(courseName);
        this.semester     = new SimpleStringProperty(semester);
        this.academicYear = new SimpleStringProperty(academicYear);
    }

    public int    getEnrollmentId() { return enrollmentId.get(); }
    public String getStudentId()    { return studentId.get(); }
    public String getStudentName()  { return studentName.get(); }
    public String getCourseId()     { return courseId.get(); }
    public String getCourseName()   { return courseName.get(); }
    public String getSemester()     { return semester.get(); }
    public String getAcademicYear() { return academicYear.get(); }

    public IntegerProperty enrollmentIdProperty() { return enrollmentId; }
    public StringProperty  studentIdProperty()    { return studentId; }
    public StringProperty  studentNameProperty()  { return studentName; }
    public StringProperty  courseIdProperty()     { return courseId; }
    public StringProperty  courseNameProperty()   { return courseName; }
    public StringProperty  semesterProperty()     { return semester; }
    public StringProperty  academicYearProperty() { return academicYear; }
}
