package birh.model;

public class Registration {
    public Student st; 
    public Course course; 
    public double grade; 
    public int year, sem;
    
    public Registration(Student s, Course c, double g, int y, int sm) { 
        st = s; course = c; grade = g; year = y; sem = sm; 
    }
}
