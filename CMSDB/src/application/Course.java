package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Course {
    private final StringProperty code;
    private final StringProperty title;
    private final StringProperty instructor;

    public Course(String code, String title, String instructor) {
        this.code = new SimpleStringProperty(code);
        this.title = new SimpleStringProperty(title);
        this.instructor = new SimpleStringProperty(instructor);
    }

    public String getCode() {
        return code.get();
    }

    public String getTitle() {
        return title.get();
    }

    public String getInstructor() {
        return instructor.get();
    }

    public StringProperty codeProperty() {
        return code;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty instructorProperty() {
        return instructor;
    }
}

