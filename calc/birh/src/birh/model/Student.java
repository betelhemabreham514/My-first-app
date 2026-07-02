package birh.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    public String id, name, dept;
    public List<Registration> regs = new ArrayList<>();
    
    public Student(String i, String n, String d) { 
        id = i; name = n; dept = d; 
    }

    public String getLetter(double m) {
        if(m>=90) return "A+"; if(m>=85) return "A"; if(m>=80) return "A-";
        if(m>=75) return "B+"; if(m>=70) return "B"; if(m>=65) return "B-";
        if(m>=60) return "C+"; if(m>=50) return "C"; if(m>=45) return "C-";
        if(m>=40) return "D"; return "F";
    }

    public double getGP(double m) {
        if(m>=90) return 4.0; if(m>=85) return 4.0; if(m>=80) return 3.75;
        if(m>=75) return 3.5; if(m>=70) return 3.0; if(m>=65) return 2.75;
        if(m>=60) return 2.5; if(m>=50) return 2.0; if(m>=45) return 1.75;
        if(m>=40) return 1.0; return 0.0;
    }

    public String generateReport(int targetYear, int targetSem) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append(" OFFICIAL STUDENT REPORT \n");
        sb.append("============================================================\n");
        sb.append("Student: ").append(name).append(" (").append(id).append(")\n");
        sb.append("Dept : ").append(dept).append("\n");
        sb.append("Period : Year ").append(targetYear).append(", Sem ").append(targetSem).append("\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("%-18s %-4s %-8s %-5s %-6s\n", "Course", "Cr.", "L/Lb/T", "Mark", "Grade"));
        sb.append("------------------------------------------------------------\n");
        double semPoints = 0; int semCr = 0;
        double totalPoints = 0; int totalCr = 0;
        for (Registration r : regs) {
            double gp = getGP(r.grade);
            int cr = r.course.credit;
            totalPoints += (gp * cr);
            totalCr += cr;
            if (r.year == targetYear && r.sem == targetSem) {
                sb.append(String.format("%-18s %-4d %1d/%1d/%1d %-5.1f %-6s\n",
                        r.course.name, cr, r.course.lecture, r.course.lab, r.course.tutorial, r.grade, getLetter(r.grade)));
                semPoints += (gp * cr);
                semCr += cr;
            }
        }
        double gpa = (semCr == 0) ? 0 : semPoints / semCr;
        double cgpa = (totalCr == 0) ? 0 : totalPoints / totalCr; 
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("SEMESTER GPA: %.2f\n", gpa));
        sb.append(String.format("CGPA: %.2f\n", cgpa));
        return sb.toString();
    }
}
