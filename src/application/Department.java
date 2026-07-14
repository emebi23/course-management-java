package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Department {
    private final StringProperty deptId;
    private final StringProperty deptName;

    public Department(String deptId, String deptName) {
        this.deptId   = new SimpleStringProperty(deptId);
        this.deptName = new SimpleStringProperty(deptName);
    }

    public String getDeptId()   { return deptId.get(); }
    public String getDeptName() { return deptName.get(); }

    public StringProperty deptIdProperty()   { return deptId; }
    public StringProperty deptNameProperty() { return deptName; }

    @Override public String toString() { return deptId.get() + " – " + deptName.get(); }
}
