package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Instructor {
    private final StringProperty instructorId;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty specialization;

    public Instructor(String instructorId, String name, String email, String phone, String specialization) {
        this.instructorId    = new SimpleStringProperty(instructorId);
        this.name            = new SimpleStringProperty(name);
        this.email           = new SimpleStringProperty(email);
        this.phone           = new SimpleStringProperty(phone);
        this.specialization  = new SimpleStringProperty(specialization);
    }

    public String getInstructorId()   { return instructorId.get(); }
    public String getName()           { return name.get(); }
    public String getEmail()          { return email.get(); }
    public String getPhone()          { return phone.get(); }
    public String getSpecialization() { return specialization.get(); }

    public StringProperty instructorIdProperty()   { return instructorId; }
    public StringProperty nameProperty()           { return name; }
    public StringProperty emailProperty()          { return email; }
    public StringProperty phoneProperty()          { return phone; }
    public StringProperty specializationProperty() { return specialization; }
}
