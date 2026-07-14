package application;

import javafx.beans.property.*;

public class ClassSection {
    private final StringProperty  classId;
    private final IntegerProperty yearLevel;
    private final StringProperty  section;
    private final StringProperty  deptId;
    private final StringProperty  deptName;
    private final StringProperty  academicYear;

    public ClassSection(String classId, int yearLevel, String section,
                        String deptId, String deptName, String academicYear) {
        this.classId      = new SimpleStringProperty(classId);
        this.yearLevel    = new SimpleIntegerProperty(yearLevel);
        this.section      = new SimpleStringProperty(section);
        this.deptId       = new SimpleStringProperty(deptId != null ? deptId : "");
        this.deptName     = new SimpleStringProperty(deptName != null ? deptName : "—");
        this.academicYear = new SimpleStringProperty(academicYear != null ? academicYear : "");
    }

    public String  getClassId()      { return classId.get(); }
    public int     getYearLevel()    { return yearLevel.get(); }
    public String  getSection()      { return section.get(); }
    public String  getDeptId()       { return deptId.get(); }
    public String  getDeptName()     { return deptName.get(); }
    public String  getAcademicYear() { return academicYear.get(); }

    public StringProperty  classIdProperty()      { return classId; }
    public IntegerProperty yearLevelProperty()    { return yearLevel; }
    public StringProperty  sectionProperty()      { return section; }
    public StringProperty  deptIdProperty()       { return deptId; }
    public StringProperty  deptNameProperty()     { return deptName; }
    public StringProperty  academicYearProperty() { return academicYear; }

    // Label shown in combos: "Year 1 – A  (Computer Science)"
    public String getDisplayName() {
        return "Year " + yearLevel.get() + " – " + section.get() +
               "  (" + deptName.get() + ")  " + academicYear.get();
    }

    @Override public String toString() { return getDisplayName(); }
}
