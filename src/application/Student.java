package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Student {
    private final StringProperty studentId;
    private final StringProperty name;
    private final StringProperty gender;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty department;

    public Student(String studentId, String name, String gender, String email, String phone, String department) {
        this.studentId  = new SimpleStringProperty(studentId);
        this.name       = new SimpleStringProperty(name);
        this.gender     = new SimpleStringProperty(gender);
        this.email      = new SimpleStringProperty(email);
        this.phone      = new SimpleStringProperty(phone);
        this.department = new SimpleStringProperty(department);
    }

    public String getStudentId()  { return studentId.get(); }
    public String getName()       { return name.get(); }
    public String getGender()     { return gender.get(); }
    public String getEmail()      { return email.get(); }
    public String getPhone()      { return phone.get(); }
    public String getDepartment() { return department.get(); }

    public StringProperty studentIdProperty()  { return studentId; }
    public StringProperty nameProperty()       { return name; }
    public StringProperty genderProperty()     { return gender; }
    public StringProperty emailProperty()      { return email; }
    public StringProperty phoneProperty()      { return phone; }
    public StringProperty departmentProperty() { return department; }
}
