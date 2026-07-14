
	package application;


	public class Enrollment {
	    private int id;
	    private String studentId;
	    private String courseCode;
	    private String grade;

	   

		public Enrollment(int id, String studentId, String courseCode, String grade) {

			        this.id = id;
			        this.studentId = studentId;
			        this.courseCode = courseCode;
			        this.grade = grade;
			    }

			 
		public int getId() { return id; }
	    public String getStudentId() { return studentId; }
	    public String getCourseCode() { return courseCode; }
	    public String getGrade() { return grade; }
	}
