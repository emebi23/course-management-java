package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class Course {
    private final StringProperty  courseId;
    private final StringProperty  courseName;
    private final IntegerProperty creditHour;
    private final StringProperty  instructorId;

    public Course(String courseId, String courseName, int creditHour, String instructorId) {
        this.courseId     = new SimpleStringProperty(courseId);
        this.courseName   = new SimpleStringProperty(courseName);
        this.creditHour   = new SimpleIntegerProperty(creditHour);
        this.instructorId = new SimpleStringProperty(instructorId);
    }

    public String getCourseId()     { return courseId.get(); }
    public String getCourseName()   { return courseName.get(); }
    public int    getCreditHour()   { return creditHour.get(); }
    public String getInstructorId() { return instructorId.get(); }

    public StringProperty  courseIdProperty()     { return courseId; }
    public StringProperty  courseNameProperty()   { return courseName; }
    public IntegerProperty creditHourProperty()   { return creditHour; }
    public StringProperty  instructorIdProperty() { return instructorId; }
}
