package birh.model;

public class Course {
    public String code, name;
    public int credit, lecture, lab, tutorial;
    public Module module;
    public Course prerequisite;

    public Course(String c, String n, int cr, int lec, int lb, int tut, Module m, Course pre) {
        code = c; name = n; credit = cr;
        lecture = lec; lab = lb; tutorial = tut;
        module = m; prerequisite = pre;
    }
}
