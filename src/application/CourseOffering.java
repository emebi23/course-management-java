package application;

import javafx.beans.property.*;

public class CourseOffering {
    private final IntegerProperty offeringId;
    private final StringProperty  classId;
    private final StringProperty  classDisplay;
    private final StringProperty  courseId;
    private final StringProperty  courseName;
    private final StringProperty  instructorId;
    private final StringProperty  instructorName;
    private final StringProperty  semester;
    private final StringProperty  academicYear;
    private final IntegerProperty enrolledCount;

    public CourseOffering(int offeringId, String classId, String classDisplay,
                          String courseId, String courseName,
                          String instructorId, String instructorName,
                          String semester, String academicYear, int enrolledCount) {
        this.offeringId     = new SimpleIntegerProperty(offeringId);
        this.classId        = new SimpleStringProperty(classId);
        this.classDisplay   = new SimpleStringProperty(classDisplay);
        this.courseId       = new SimpleStringProperty(courseId);
        this.courseName     = new SimpleStringProperty(courseName);
        this.instructorId   = new SimpleStringProperty(instructorId != null ? instructorId : "");
        this.instructorName = new SimpleStringProperty(instructorName != null ? instructorName : "—");
        this.semester       = new SimpleStringProperty(semester);
        this.academicYear   = new SimpleStringProperty(academicYear != null ? academicYear : "");
        this.enrolledCount  = new SimpleIntegerProperty(enrolledCount);
    }

    public int    getOfferingId()     { return offeringId.get(); }
    public String getClassId()        { return classId.get(); }
    public String getClassDisplay()   { return classDisplay.get(); }
    public String getCourseId()       { return courseId.get(); }
    public String getCourseName()     { return courseName.get(); }
    public String getInstructorId()   { return instructorId.get(); }
    public String getInstructorName() { return instructorName.get(); }
    public String getSemester()       { return semester.get(); }
    public String getAcademicYear()   { return academicYear.get(); }
    public int    getEnrolledCount()  { return enrolledCount.get(); }

    public IntegerProperty offeringIdProperty()    { return offeringId; }
    public StringProperty  classIdProperty()       { return classId; }
    public StringProperty  classDisplayProperty()  { return classDisplay; }
    public StringProperty  courseIdProperty()      { return courseId; }
    public StringProperty  courseNameProperty()    { return courseName; }
    public StringProperty  instructorIdProperty()  { return instructorId; }
    public StringProperty  instructorNameProperty(){ return instructorName; }
    public StringProperty  semesterProperty()      { return semester; }
    public StringProperty  academicYearProperty()  { return academicYear; }
    public IntegerProperty enrolledCountProperty() { return enrolledCount; }
}
