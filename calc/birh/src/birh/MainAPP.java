package birh;

import birh.db.DatabaseHandler;
import birh.model.Course;
import birh.model.Module;
import birh.model.Registration;
import birh.model.Student;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.*;
import java.util.*;

public class MainAPP extends Application {

    static Map<String, Student> students = new HashMap<>();
    static Map<String, Course> courses = new HashMap<>();
    static Map<String, Module> modules = new HashMap<>();

    ComboBox<String> studentCombo = new ComboBox<>();
    ComboBox<String> courseCombo = new ComboBox<>();
    ComboBox<String> preReqCombo = new ComboBox<>();
    ComboBox<String> moduleCombo = new ComboBox<>();
    ComboBox<Integer> yearCombo = new ComboBox<>();
    ComboBox<Integer> semCombo = new ComboBox<>();

    private final String HEADER_STYLE = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-border-color: #3498db; -fx-border-width: 0 0 2 0; -fx-padding: 5;";
    private final String BUTTON_STYLE = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;";
    private final String BACK_STYLE = "-fx-background-color: #bdc3c7; -fx-text-fill: black; -fx-background-radius: 5;";
    private final String LABEL_STYLE = "-fx-font-weight: bold; -fx-text-fill: #34495e;";
    private final String DANGER_STYLE = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;";

    @Override
    public void start(Stage stage) {
        DatabaseHandler.initializeDatabase();
        loadDataFromDatabase();
        yearCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7));
        semCombo.setItems(FXCollections.observableArrayList(1, 2));
        showHomepage(stage);
    }

    void showHomepage(Stage stage) {
        StackPane root = new StackPane();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("w-gatw.jpg"));
            BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
            );
            root.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            System.err.println("Homepage background not found: " + e.getMessage());
            root.setStyle("-fx-background-color: #f4f4f4;");
        }

        Region darkOverlay = new Region();
        darkOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        Label title = new Label("Student Course Management System");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 10, 0.5, 0, 0);");
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(60, 0, 0, 0));

        Button continueBtn = new Button("Continue to System");
        continueBtn.setStyle(BUTTON_STYLE + " -fx-font-size: 18px; -fx-padding: 10 20;");
        continueBtn.setOnAction(e -> showLogin(stage));
        
        StackPane.setAlignment(continueBtn, Pos.BOTTOM_CENTER);
        StackPane.setMargin(continueBtn, new Insets(0, 0, 80, 0));
        
        root.getChildren().addAll(darkOverlay, title, continueBtn);

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("Student Management System");
        stage.show();
    }

    private void loadDataFromDatabase() {
        modules.clear();
        courses.clear();
        students.clear();

        try (Connection conn = DatabaseHandler.getConnection()) {
            if (conn == null) return;
            ResultSet rsMod = conn.createStatement().executeQuery("SELECT * FROM modules");
            while (rsMod.next()) {
                modules.put(rsMod.getString("module_code"), new Module(rsMod.getString("module_code"), rsMod.getString("module_name")));
            }

            ResultSet rsCr = conn.createStatement().executeQuery("SELECT * FROM courses");
            while (rsCr.next()) {
                String mCode = rsCr.getString("module_code");
                courses.put(rsCr.getString("course_code"), new Course(
                        rsCr.getString("course_code"),
                        rsCr.getString("course_name"),
                        rsCr.getInt("credit_hours"),
                        rsCr.getInt("lecture"),
                        rsCr.getInt("lab"),
                        rsCr.getInt("tutorial"),
                        modules.get(mCode),
                        null
                ));
            }

            ResultSet rsPre = conn.createStatement().executeQuery("SELECT course_code, prerequisite_code FROM courses WHERE prerequisite_code IS NOT NULL AND prerequisite_code != ''");
            while (rsPre.next()) {
                Course c = courses.get(rsPre.getString("course_code"));
                Course pre = courses.get(rsPre.getString("prerequisite_code"));
                if (c != null && pre != null) {
                    c.prerequisite = pre;
                }
            }

            ResultSet rsSt = conn.createStatement().executeQuery("SELECT * FROM students");
            while (rsSt.next()) {
                students.put(rsSt.getString("student_id"), new Student(rsSt.getString("student_id"), rsSt.getString("full_name"), rsSt.getString("department")));
            }

            ResultSet rsReg = conn.createStatement().executeQuery("SELECT * FROM registrations");
            while (rsReg.next()) {
                Student s = students.get(rsReg.getString("student_id"));
                Course c = courses.get(rsReg.getString("course_code"));
                if (s != null && c != null) {
                    s.regs.add(new Registration(s, c, rsReg.getDouble("grade"), rsReg.getInt("year"), rsReg.getInt("semester")));
                }
            }
        } catch (Exception e) {
            System.err.println("Database loading error: " + e.getMessage());
        }
    }

    void refreshAll() {
        studentCombo.setItems(FXCollections.observableArrayList(new TreeSet<>(students.keySet())));
        courseCombo.setItems(FXCollections.observableArrayList(new TreeSet<>(courses.keySet())));
        List<String> preList = new ArrayList<>(new TreeSet<>(courses.keySet()));
        preList.add(0, "");
        preReqCombo.setItems(FXCollections.observableArrayList(preList));
        moduleCombo.setItems(FXCollections.observableArrayList(new TreeSet<>(modules.keySet())));
    }

    void showLogin(Stage stage) {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f4f4;");
        try {
            Image logo = new Image(getClass().getResourceAsStream("wdu.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(150);
            logoView.setPreserveRatio(true);
            root.getChildren().add(logoView);
        } catch (Exception e) {
            System.err.println("WDU Logo not found.");
        }

        Label title = new Label("STUDENT MANAGEMENT SYSTEM");
        title.setFont(Font.font("System", 20));
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        TextField u = new TextField();
        PasswordField p = new PasswordField();
        Label msg = new Label();
        try {
            Image lockIcon = new Image(getClass().getResourceAsStream("lock_unlock_15064.png"));
            ImageView lockView = new ImageView(lockIcon);
            lockView.setFitWidth(35);
            lockView.setPreserveRatio(true);
            grid.add(lockView, 0, 0, 1, 2);
            grid.add(new Label("Username:"), 1, 0);
            grid.add(u, 2, 0);
        } catch (Exception e) {
            grid.add(new Label("Username:"), 0, 0); grid.add(u, 1, 0);
        }
        grid.add(new Label("Password:"), 1, 1); grid.add(p, 2, 1);
        Button login = new Button("LOGIN");
        login.setStyle(BUTTON_STYLE);
        login.setMinWidth(150);
        login.setOnAction(e -> {
            if (!u.getText().isEmpty()) {
                showDashboard(stage, u.getText().toUpperCase());
            } else {
                msg.setText("Please enter a username");
                msg.setTextFill(Color.RED);
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setStyle(BACK_STYLE);
        backBtn.setOnAction(e -> showHomepage(stage));

        root.getChildren().addAll(title, grid, login, msg);
        
        StackPane mainRoot = new StackPane();
        mainRoot.setStyle("-fx-background-color: #f4f4f4;");
        root.setStyle("-fx-background-color: transparent;");
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(15));
        
        mainRoot.getChildren().addAll(root, backBtn);

        stage.setScene(new Scene(mainRoot, 500, 600));
        stage.setTitle("Login");
        stage.show();
    }

    void showDashboard(Stage stage, String role) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");
        Label header = new Label("WELCOME, " + role);
        header.setStyle(HEADER_STYLE);
        String bStyle = "-fx-min-width: 250px; -fx-padding: 10; " + BUTTON_STYLE;
        Button s = new Button("Manage Students"); s.setStyle(bStyle);
        Button m = new Button("Manage Modules"); m.setStyle(bStyle);
        Button c = new Button("Manage Courses"); c.setStyle(bStyle);
        Button r = new Button("Registration & Grading"); r.setStyle(bStyle);
        Button rep = new Button("Generate Reports"); rep.setStyle(bStyle);
        Button logout = new Button("Logout"); logout.setStyle("-fx-min-width: 250px; " + BACK_STYLE);
        s.setOnAction(e -> showStudentUI(stage, role));
        m.setOnAction(e -> showModuleUI(stage, role));
        c.setOnAction(e -> showCourseUI(stage, role));
        r.setOnAction(e -> showRegisterUI(stage, role));
        rep.setOnAction(e -> showReportUI(stage, role));
        logout.setOnAction(e -> showLogin(stage));
        root.getChildren().addAll(header, s, m, c, r, rep, logout);
        stage.setScene(new Scene(root, 450, 550));
    }

    private boolean isInvalidString(String input) {
        if (input == null || input.trim().isEmpty()) return true;
        return input.trim().matches("-?\\d+");
    }

    void showStudentUI(Stage stage, String role) {
        GridPane grid = createGrid("STUDENT REGISTRATION (CRUD)");
        TextField id = new TextField();
        TextField name = new TextField();
        TextField dept = new TextField();
        grid.add(createLabel("Student ID:"), 0, 1); grid.add(id, 1, 1);
        grid.add(createLabel("Full Name:"), 0, 2); grid.add(name, 1, 2);
        grid.add(createLabel("Department:"), 0, 3); grid.add(dept, 1, 3);

        TableView<Student> table = new TableView<>();
        TableColumn<Student, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().id));
        TableColumn<Student, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        TableColumn<Student, String> colDept = new TableColumn<>("Dept");
        colDept.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().dept));
        
        // ማስተካከያ የተደረገበት መስመር (Warnings ለማጥፋት)
        table.getColumns().add(colId);
        table.getColumns().add(colName);
        table.getColumns().add(colDept);

        table.setItems(FXCollections.observableArrayList(students.values()));
        table.setPrefHeight(200);
        grid.add(table, 0, 6, 2, 1);

        Button add = new Button("Add"); add.setStyle(BUTTON_STYLE);
        Button update = new Button("Update"); update.setStyle(BUTTON_STYLE);
        Button delete = new Button("Delete"); delete.setStyle(DANGER_STYLE);
        Button search = new Button("Search ID"); search.setStyle(BUTTON_STYLE);
        Button back = new Button("Back"); back.setStyle(BACK_STYLE);
        
        add.setOnAction(e -> {
            if(isInvalidString(id.getText())){
                showAlert("Input Error", "Student ID must be a non-numeric string and cannot be empty.");
                return;
            }
            if(name.getText().isEmpty()){
                showAlert("Input Error", "Please fill Name.");
                return;
            }
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO students VALUES(?,?,?)")) {
                ps.setString(1, id.getText().trim()); ps.setString(2, name.getText()); ps.setString(3, dept.getText());
                ps.executeUpdate();
                students.put(id.getText().trim(), new Student(id.getText().trim(), name.getText(), dept.getText()));
                refreshAll();
                table.setItems(FXCollections.observableArrayList(students.values()));
                showAlert("Done", "Student added!");
            } catch (Exception ex) { showAlert("Error", "Duplicate ID or DB Error"); }
        });
        search.setOnAction(e -> {
            Student st = students.get(id.getText().trim());
            if(st != null){ name.setText(st.name); dept.setText(st.dept); }
            else { showAlert("Not Found", "ID not in system."); }
        });
        update.setOnAction(e -> {
            if(students.containsKey(id.getText().trim())){
                try (Connection conn = DatabaseHandler.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE students SET full_name=?, department=? WHERE student_id=?")) {
                    ps.setString(1, name.getText()); ps.setString(2, dept.getText()); ps.setString(3, id.getText().trim());
                    ps.executeUpdate();
                    Student s = students.get(id.getText().trim());
                    s.name = name.getText();
                    s.dept = dept.getText();
                    table.refresh();
                    showAlert("Updated", "Student data changed.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        delete.setOnAction(e -> {
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE student_id=?")) {
                ps.setString(1, id.getText().trim());
                ps.executeUpdate();
                students.remove(id.getText().trim());
                refreshAll();
                table.setItems(FXCollections.observableArrayList(students.values()));
                id.clear(); name.clear(); dept.clear();
                showAlert("Deleted", "Student removed.");
            } catch (Exception ex) { showAlert("Error", "Student has records, can't delete."); }
        });
        grid.add(new HBox(10, add, update, delete, search), 1, 4);
        grid.add(back, 1, 5);
        back.setOnAction(e -> showDashboard(stage, role));
        stage.setScene(new Scene(grid, 650, 700));
    }

    void showModuleUI(Stage stage, String role) {
        GridPane grid = createGrid("MODULE SETUP");
        TextField code = new TextField();
        TextField name = new TextField();
        grid.add(createLabel("Module Code:"), 0, 1); grid.add(code, 1, 1);
        grid.add(createLabel("Module Name:"), 0, 2); grid.add(name, 1, 2);

        TableView<Module> table = new TableView<>();
        TableColumn<Module, String> colCode = new TableColumn<>("Code");
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code));
        TableColumn<Module, String> colName = new TableColumn<>("Module Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        
        // ማስተካከያ የተደረገበት መስመር
        table.getColumns().add(colCode);
        table.getColumns().add(colName);

        table.setItems(FXCollections.observableArrayList(modules.values()));
        table.setPrefHeight(200);
        grid.add(table, 0, 4, 2, 1);

        Button add = new Button("Add"); add.setStyle(BUTTON_STYLE);
        Button delete = new Button("Delete"); delete.setStyle(DANGER_STYLE);
        Button back = new Button("Back"); back.setStyle(BACK_STYLE);

        add.setOnAction(e -> {
            if (isInvalidString(code.getText())) {
                showAlert("Input Error", "Module Code must be a non-numeric string and cannot be empty.");
                return;
            }
            if (name.getText().isEmpty()) {
                showAlert("Input Error", "Please fill Module Name.");
                return;
            }
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO modules VALUES(?,?)")) {
                ps.setString(1, code.getText().trim()); ps.setString(2, name.getText());
                ps.executeUpdate();
                modules.put(code.getText().trim(), new Module(code.getText().trim(), name.getText()));
                refreshAll();
                table.setItems(FXCollections.observableArrayList(modules.values()));
                showAlert("Success", "Module Saved");
                code.clear(); name.clear();
            } catch (Exception ex) { showAlert("Error", "Code already exists!"); }
        });

        delete.setOnAction(e -> {
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM modules WHERE module_code=?")) {
                ps.setString(1, code.getText().trim());
                if (ps.executeUpdate() > 0) {
                    modules.remove(code.getText().trim());
                    refreshAll();
                    table.setItems(FXCollections.observableArrayList(modules.values()));
                    showAlert("Deleted", "Module removed.");
                }
            } catch (Exception ex) { showAlert("Error", "Module is linked to courses."); }
        });

        grid.add(new HBox(10, add, delete, back), 1, 3);
        back.setOnAction(e -> showDashboard(stage, role));
        stage.setScene(new Scene(grid, 600, 600));
    }

    void showCourseUI(Stage stage, String role) {
        GridPane grid = createGrid("COURSE MANAGEMENT (CRUD)");
        refreshAll();
        TextField code = new TextField();
        TextField name = new TextField();
        TextField credit = new TextField();
        TextField lec = new TextField("3");
        TextField lab = new TextField("0");
        TextField tut = new TextField("1");
        grid.add(createLabel("Course Code:"), 0, 1); grid.add(code, 1, 1);
        grid.add(createLabel("Course Name:"), 0, 2); grid.add(name, 1, 2);
        grid.add(createLabel("Credit Hours:"), 0, 3); grid.add(credit, 1, 3);
        grid.add(createLabel("Lec/Lab/Tut hrs:"), 0, 4); grid.add(new HBox(5, lec, lab, tut), 1, 4);
        grid.add(createLabel("Module:"), 0, 5); grid.add(moduleCombo, 1, 5);
        grid.add(createLabel("Prerequisite:"), 0, 6); grid.add(preReqCombo, 1, 6);

        TableView<Course> table = new TableView<>();
        TableColumn<Course, String> colCode = new TableColumn<>("Code");
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code));
        TableColumn<Course, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        TableColumn<Course, Integer> colCr = new TableColumn<>("Cr");
        colCr.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().credit).asObject());
        TableColumn<Course, String> colMod = new TableColumn<>("Module");
        colMod.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().module != null ? d.getValue().module.code : "-"));
        
        // ማስተካከያ የተደረገበት መስመር
        table.getColumns().add(colCode);
        table.getColumns().add(colName);
        table.getColumns().add(colCr);
        table.getColumns().add(colMod);

        table.setItems(FXCollections.observableArrayList(courses.values()));
        table.setPrefHeight(200);
        grid.add(table, 0, 9, 2, 1);

        Button add = new Button("Add"); add.setStyle(BUTTON_STYLE);
        Button delete = new Button("Delete"); delete.setStyle(DANGER_STYLE);
        Button search = new Button("Search"); search.setStyle(BUTTON_STYLE);
        Button update = new Button("Update"); update.setStyle(BUTTON_STYLE);
        Button back = new Button("Back"); back.setStyle(BACK_STYLE);

        add.setOnAction(e -> {
            try {
                if (isInvalidString(code.getText())) {
                    showAlert("Input Error", "Course Code must be a non-numeric string and cannot be empty.");
                    return;
                }
                if (name.getText().trim().isEmpty() || credit.getText().trim().isEmpty() || moduleCombo.getValue() == null) {
                    showAlert("Input Error", "Please fill Name, Credit, and Module.");
                    return;
                }
                int c_hr = Integer.parseInt(credit.getText().trim());
                int l_hr = Integer.parseInt(lec.getText().trim());
                int lb_hr = Integer.parseInt(lab.getText().trim());
                int t_hr = Integer.parseInt(tut.getText().trim());
                String preReqValue = preReqCombo.getValue();
                if (preReqValue == null || preReqValue.trim().isEmpty()) preReqValue = null;

                try (Connection conn = DatabaseHandler.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO courses (course_code, course_name, credit_hours, lecture, lab, tutorial, module_code, prerequisite_code) VALUES(?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, code.getText().trim());
                    ps.setString(2, name.getText().trim());
                    ps.setInt(3, c_hr);
                    ps.setInt(4, l_hr);
                    ps.setInt(5, lb_hr);
                    ps.setInt(6, t_hr);
                    ps.setString(7, moduleCombo.getValue());
                    if (preReqValue == null) ps.setNull(8, java.sql.Types.VARCHAR);
                    else ps.setString(8, preReqValue);
                    ps.executeUpdate();
                    Course preObject = (preReqValue != null) ? courses.get(preReqValue) : null;
                    courses.put(code.getText().trim(), new Course(code.getText().trim(), name.getText(), c_hr, l_hr, lb_hr, t_hr, modules.get(moduleCombo.getValue()), preObject));
                    refreshAll();
                    table.setItems(FXCollections.observableArrayList(courses.values()));
                    showAlert("Success", "Course registered.");
                }
            } catch (Exception ex) { showAlert("Error", "Error: " + ex.getMessage()); }
        });

        update.setOnAction(e -> {
            try {
                if (courses.containsKey(code.getText().trim())) {
                    try (Connection conn = DatabaseHandler.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                                 "UPDATE courses SET course_name=?, credit_hours=?, lecture=?, lab=?, tutorial=?, module_code=?, prerequisite_code=? WHERE course_code=?")) {
                        ps.setString(1, name.getText());
                        ps.setInt(2, Integer.parseInt(credit.getText()));
                        ps.setInt(3, Integer.parseInt(lec.getText()));
                        ps.setInt(4, Integer.parseInt(lab.getText()));
                        ps.setInt(5, Integer.parseInt(tut.getText()));
                        ps.setString(6, moduleCombo.getValue());
                        String preReq = preReqCombo.getValue();
                        if (preReq == null || preReq.isEmpty()) ps.setNull(7, java.sql.Types.VARCHAR);
                        else ps.setString(7, preReq);
                        ps.setString(8, code.getText().trim());
                        ps.executeUpdate();
                        Course c = courses.get(code.getText().trim());
                        c.name = name.getText();
                        c.credit = Integer.parseInt(credit.getText());
                        c.module = modules.get(moduleCombo.getValue());
                        c.prerequisite = courses.get(preReq);
                        table.refresh();
                        showAlert("Updated", "Course data updated.");
                    }
                }
            } catch (Exception ex) { showAlert("Error", "Update failed."); }
        });

        search.setOnAction(e -> {
            Course c = courses.get(code.getText().trim());
            if(c != null){
                name.setText(c.name); credit.setText(String.valueOf(c.credit));
                lec.setText(String.valueOf(c.lecture)); lab.setText(String.valueOf(c.lab)); tut.setText(String.valueOf(c.tutorial));
                moduleCombo.setValue(c.module != null ? c.module.code : null);
                preReqCombo.setValue(c.prerequisite != null ? c.prerequisite.code : null);
            } else { showAlert("Not Found", "Course code not found."); }
        });

        delete.setOnAction(e -> {
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE course_code=?")) {
                ps.setString(1, code.getText().trim());
                ps.executeUpdate();
                courses.remove(code.getText().trim());
                refreshAll();
                table.setItems(FXCollections.observableArrayList(courses.values()));
                showAlert("Deleted", "Course removed.");
            } catch (Exception ex) { showAlert("Error", "Course is a prerequisite or has records."); }
        });

        grid.add(new HBox(10, add, update, delete, search), 1, 7);
        grid.add(back, 1, 8);
        back.setOnAction(e -> showDashboard(stage, role));
        stage.setScene(new Scene(grid, 700, 850));
    }

    void showRegisterUI(Stage stage, String role) {
        GridPane grid = createGrid("ENROLLMENT & GRADES");
        refreshAll();
        TextField gradeVal = new TextField();
        Label errorMsg = new Label();
        errorMsg.setTextFill(Color.RED);
        grid.add(createLabel("Student:"), 0, 1); grid.add(studentCombo, 1, 1);
        grid.add(createLabel("Course:"), 0, 2); grid.add(courseCombo, 1, 2);
        grid.add(createLabel("Year:"), 0, 3); grid.add(yearCombo, 1, 3);
        grid.add(createLabel("Semester:"), 0, 4); grid.add(semCombo, 1, 4);
        grid.add(createLabel("Result (0-100):"), 0, 5); grid.add(gradeVal, 1, 5);
        grid.add(errorMsg, 1, 7);
        Button reg = new Button("Submit Result"); reg.setStyle(BUTTON_STYLE);
        Button back = new Button("Back"); back.setStyle(BACK_STYLE);
        reg.setOnAction(e -> {
            errorMsg.setText("");
            Student s = students.get(studentCombo.getValue());
            Course c = courses.get(courseCombo.getValue());
            Integer y = yearCombo.getValue();
            Integer sm = semCombo.getValue();
            if (s != null && c != null && y != null && sm != null && !gradeVal.getText().isEmpty()) {
                if (c.prerequisite != null) {
                    boolean passed = s.regs.stream().anyMatch(r -> r.course.code.equals(c.prerequisite.code) && r.grade >= 40);
                    if (!passed) {
                        errorMsg.setText("BLOCKED: Must pass " + c.prerequisite.name + " first!");
                        return;
                    }
                }
                try (Connection conn = DatabaseHandler.getConnection();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO registrations (student_id, course_code, grade, year, semester) VALUES(?,?,?,?,?)")) {
                    double g = Double.parseDouble(gradeVal.getText());
                    ps.setString(1, s.id); ps.setString(2, c.code); ps.setDouble(3, g); ps.setInt(4, y); ps.setInt(5, sm);
                    ps.executeUpdate();
                    s.regs.add(new Registration(s, c, g, y, sm));
                    showAlert("Success", "Registration Saved.");
                } catch (NumberFormatException nfe) { showAlert("Error", "Grade must be a number!"); }
                catch (Exception ex) { showAlert("Error", "Registration exists or DB error."); }
            } else { showAlert("Error", "Select all fields and enter a grade!"); }
        });
        grid.add(new HBox(10, reg, back), 1, 6);
        back.setOnAction(e -> showDashboard(stage, role));
        stage.setScene(new Scene(grid, 500, 550));
    }

    void showReportUI(Stage stage, String role) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        refreshAll();
        ComboBox<Integer> rYear = new ComboBox<>(); rYear.setItems(yearCombo.getItems());
        ComboBox<Integer> rSem = new ComboBox<>(); rSem.setItems(semCombo.getItems());
        HBox top = new HBox(10, createLabel("Student:"), studentCombo, new Label("Year:"), rYear, new Label("Sem:"), rSem);
        top.setAlignment(Pos.CENTER);
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefHeight(400);
        area.setFont(Font.font("Monospaced", 12));
        Button gen = new Button("Show Report"); gen.setStyle(BUTTON_STYLE);
        Button back = new Button("Back"); back.setStyle(BACK_STYLE);
        gen.setOnAction(e -> {
            Student s = students.get(studentCombo.getValue());
            if (s != null && rYear.getValue() != null && rSem.getValue() != null) {
                area.setText(s.generateReport(rYear.getValue(), rSem.getValue()));
            } else { area.setText("Select Student, Year, and Semester"); }
        });
        root.getChildren().addAll(top, new HBox(10, gen, back), area);
        back.setOnAction(e -> showDashboard(stage, role));
        stage.setScene(new Scene(root, 650, 700));
    }

    private Label createLabel(String text) { Label l = new Label(text); l.setStyle(LABEL_STYLE); return l; }
    private GridPane createGrid(String title) {
        GridPane g = new GridPane();
        g.setPadding(new Insets(30)); g.setHgap(15); g.setVgap(15);
        g.setAlignment(Pos.TOP_CENTER);
        Label t = new Label(title); t.setStyle(HEADER_STYLE);
        g.add(t, 0, 0, 2, 1);
        return g;
    }
    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
