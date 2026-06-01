package javafxapplication4;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * Hospital Management System — JavaFX UI with MySQL (JDBC).
 */
public class HospitalManagementSystem extends Application {

    private final HospitalRepository repository = new HospitalRepository();

    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private final ObservableList<Staff> staff = FXCollections.observableArrayList();
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private final ObservableList<String> notifications = FXCollections.observableArrayList();

    private TableView<Patient> patientTable;
    private TableView<Doctor> doctorTable;
    private TableView<Staff> staffTable;
    private TableView<Appointment> appointmentTable;
    private TableView<Bill> billTable;
    private ListView<String> notificationList;

    private Label patientCountLabel;
    private Label doctorCountLabel;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String ICON_HOSPITAL = "https://cdn-icons-png.flaticon.com/512/2991/2991106.png";
    private final String ICON_DOCTOR = "https://cdn-icons-png.flaticon.com/512/194/194938.png";
    private final String ICON_PATIENT = "https://cdn-icons-png.flaticon.com/512/194/194933.png";
    private final String ICON_STAFF = "https://cdn-icons-png.flaticon.com/512/3011/3011270.png";
    private final String ICON_APPOINT = "https://cdn-icons-png.flaticon.com/512/2913/2913463.png";
    private final String ICON_NOTIFY = "https://cdn-icons-png.flaticon.com/512/1828/1828665.png";
    private final String ICON_ADD = "https://cdn-icons-png.flaticon.com/512/1828/1828817.png";
    private final String ICON_DELETE = "https://cdn-icons-png.flaticon.com/512/6861/6861362.png";
    private final String ICON_WARNING = "https://cdn-icons-png.flaticon.com/512/565/565547.png";
    private final String ICON_DONE = "https://cdn-icons-png.flaticon.com/512/190/190411.png";

    @Override
    public void start(Stage stage) {
        try {
            DatabaseConnection.testConnection();
        } catch (SQLException ex) {
            showError("Database connection failed.\n\n"
                    + "1. Start MySQL Server\n"
                    + "2. Run sql/hospital_schema.sql in MySQL Workbench\n"
                    + "3. Set db.password in db.properties\n\n"
                    + ex.getMessage());
            return;
        }

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox leftSidebar = createSidebar();

        SplitPane mainPane = new SplitPane();
        mainPane.getItems().addAll(leftSidebar, tabs);
        mainPane.setDividerPositions(0.18);

        tabs.getTabs().addAll(
                new Tab("Patients", createPatientTab()),
                new Tab("Doctors", createDoctorTab()),
                new Tab("Staff", createStaffTab()),
                new Tab("Appointments", createAppointmentTab()),
                new Tab("Billing", createBillingTab()),
                new Tab("Notifications", createNotificationTab())
        );

        VBox root = new VBox(mainPane);
        Scene scene = new Scene(root, 1200, 720);
        scene.getRoot().setStyle(
                "-fx-font-family: 'Segoe UI', Verdana; "
                + "-fx-background-color: linear-gradient(to bottom, #f4fbff, #e8f4ff);");

        stage.setTitle("Hospital Management System — MySQL");
        stage.setScene(scene);
        stage.show();

        refreshAll();
        startNotificationChecker();
    }

    private VBox createSidebar() {
        VBox vb = new VBox(14);
        vb.setPadding(new Insets(18));
        vb.setStyle("-fx-background-color: linear-gradient(to bottom, #e6f7ff, #d8f0ff); "
                + "-fx-border-color:#c7e8ff; -fx-border-width:1px;");
        vb.setAlignment(Pos.TOP_CENTER);

        ImageView logo = loadIcon(ICON_HOSPITAL, 90);
        Label title = new Label("CityCare\nHospital");
        title.setFont(Font.font(18));
        title.setTextFill(Color.web("#003b66"));
        title.setAlignment(Pos.CENTER);
        title.setWrapText(true);

        Label desc = new Label("MySQL-backed HMS\nAppointments • Billing • Staff");
        desc.setFont(Font.font(12));
        desc.setTextFill(Color.web("#16527a"));
        desc.setAlignment(Pos.CENTER);
        desc.setWrapText(true);

        Separator sep = new Separator();

        patientCountLabel = new Label("Patients: 0");
        patientCountLabel.setFont(Font.font(13));
        patientCountLabel.setTextFill(Color.web("#003b66"));

        doctorCountLabel = new Label("Doctors: 0");
        doctorCountLabel.setFont(Font.font(13));
        doctorCountLabel.setTextFill(Color.web("#003b66"));

        HBox nav1 = newHBoxIconLabel(ICON_PATIENT, "Patients");
        HBox nav2 = newHBoxIconLabel(ICON_DOCTOR, "Doctors");
        HBox nav3 = newHBoxIconLabel(ICON_APPOINT, "Appointments");
        HBox nav4 = newHBoxIconLabel(ICON_NOTIFY, "Notifications");

        vb.getChildren().addAll(logo, title, desc, sep, patientCountLabel, doctorCountLabel,
                new Separator(), nav1, nav2, nav3, nav4);
        return vb;
    }

    private HBox newHBoxIconLabel(String iconUrl, String text) {
        ImageView iv = loadIcon(iconUrl, 20);
        Label lbl = new Label(text);
        lbl.setFont(Font.font(12));
        lbl.setTextFill(Color.web("#0b4d6b"));
        HBox h = new HBox(8, iv, lbl);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private BorderPane createPatientTab() {
        BorderPane bp = new BorderPane();
        bp.setPadding(new Insets(12));

        Label header = headerLabelWithIcon("PATIENTS", ICON_PATIENT);
        patientTable = new TableView<>(patients);
        patientTable.getColumns().addAll(
                createColumn("Name", "name", 260),
                createColumn("Age", "age", 80),
                createColumn("Gender", "gender", 110),
                createColumn("Phone", "phone", 160)
        );
        styleTable(patientTable);

        VBox form = new VBox(10);
        form.setPadding(new Insets(12));
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle(signStyle());

        TextField name = new TextField();
        name.setPromptText("Full Name");
        TextField age = new TextField();
        age.setPromptText("Age");
        ChoiceBox<String> gender = new ChoiceBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        gender.getSelectionModel().select(0);
        TextField phone = new TextField();
        phone.setPromptText("Phone Number");

        Button addBtn = iconButton("Add Patient", ICON_ADD);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            if (!validateText(name.getText()) || !validateText(age.getText()) || !validateText(phone.getText())) {
                showAlert("Please fill all fields.");
                return;
            }
            try {
                int a = Integer.parseInt(age.getText());
                Patient p = new Patient(0, name.getText(), a, gender.getValue(), phone.getText());
                int id = repository.insertPatient(p);
                patients.add(new Patient(id, p.getName(), p.getAge(), p.getGender(), p.getPhone()));
                updateSidebarCounts();
                clear(name, age, phone);
            } catch (NumberFormatException ex) {
                showAlert("Age must be a number.");
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        Button delBtn = iconButton("Delete Selected", ICON_DELETE);
        delBtn.setOnAction(e -> {
            Patient selected = patientTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                repository.deletePatient(selected.getId());
                patients.remove(selected);
                reloadAppointments();
                reloadBills();
                updateSidebarCounts();
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        form.getChildren().addAll(header, name, age, gender, phone, addBtn, delBtn);
        bp.setCenter(patientTable);
        bp.setRight(form);
        return bp;
    }

    private BorderPane createDoctorTab() {
        BorderPane bp = new BorderPane();
        bp.setPadding(new Insets(12));

        Label header = headerLabelWithIcon("DOCTORS", ICON_DOCTOR);
        doctorTable = new TableView<>(doctors);
        doctorTable.getColumns().addAll(
                createColumn("Name", "name", 260),
                createColumn("Specialty", "specialty", 180),
                createColumn("Phone", "phone", 160)
        );
        styleTable(doctorTable);

        VBox form = new VBox(10);
        form.setPadding(new Insets(12));
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle(signStyle());

        TextField name = new TextField();
        name.setPromptText("Full Name");
        TextField specialty = new TextField();
        specialty.setPromptText("Specialty");
        TextField phone = new TextField();
        phone.setPromptText("Phone Number");

        Button addBtn = iconButton("Add Doctor", ICON_ADD);
        addBtn.setOnAction(e -> {
            if (!validateText(name.getText()) || !validateText(specialty.getText()) || !validateText(phone.getText())) {
                showAlert("Please fill all fields.");
                return;
            }
            try {
                Doctor d = new Doctor(0, name.getText(), specialty.getText(), phone.getText());
                int id = repository.insertDoctor(d);
                doctors.add(new Doctor(id, d.getName(), d.getSpecialty(), d.getPhone()));
                updateSidebarCounts();
                clear(name, specialty, phone);
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        Button delBtn = iconButton("Delete Selected", ICON_DELETE);
        delBtn.setOnAction(e -> {
            Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                repository.deleteDoctor(selected.getId());
                doctors.remove(selected);
                reloadAppointments();
                updateSidebarCounts();
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        form.getChildren().addAll(header, name, specialty, phone, addBtn, delBtn);
        bp.setCenter(doctorTable);
        bp.setRight(form);
        return bp;
    }
    private BorderPane createStaffTab() {
        BorderPane bp = new BorderPane();
        bp.setPadding(new Insets(12));

        Label header = headerLabelWithIcon("STAFF", ICON_STAFF);
        staffTable = new TableView<>(staff);
        staffTable.getColumns().addAll(
                createColumn("Name", "name", 260),
                createColumn("Role", "role", 180),
                createColumn("Phone", "phone", 160)
        );
        styleTable(staffTable);

        VBox form = new VBox(10);
        form.setPadding(new Insets(12));
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle(signStyle());

        TextField name = new TextField();
        name.setPromptText("Full Name");
        TextField roleF = new TextField();
        roleF.setPromptText("Role");
        TextField phone = new TextField();
        phone.setPromptText("Phone Number");

        Button addBtn = iconButton("Add Staff", ICON_ADD);
        addBtn.setOnAction(e -> {
            if (!validateText(name.getText()) || !validateText(roleF.getText()) || !validateText(phone.getText())) {
                showAlert("Please fill all fields.");
                return;
            }
            try {
                Staff s = new Staff(0, name.getText(), roleF.getText(), phone.getText());
                int id = repository.insertStaff(s);
                staff.add(new Staff(id, s.getName(), s.getRole(), s.getPhone()));
                clear(name, roleF, phone);
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        Button delBtn = iconButton("Delete Selected", ICON_DELETE);
        delBtn.setOnAction(e -> {
            Staff selected = staffTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                repository.deleteStaff(selected.getId());
                staff.remove(selected);
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        form.getChildren().addAll(header, name, roleF, phone, addBtn, delBtn);
        bp.setCenter(staffTable);
        bp.setRight(form);
        return bp;
    }

    private BorderPane createAppointmentTab() {
        BorderPane bp = new BorderPane();
        bp.setPadding(new Insets(12));

        Label header = headerLabelWithIcon("APPOINTMENTS", ICON_APPOINT);
        appointmentTable = new TableView<>(appointments);
        appointmentTable.getColumns().addAll(
                createColumn("Patient", "patientName", 260),
                createColumn("Doctor", "doctorName", 260),
                createColumn("Time", "timeStr", 200),
                createColumn("Status", "statusStr", 120)
        );
        styleTable(appointmentTable);

        VBox form = new VBox(10);
        form.setPadding(new Insets(12));
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle(signStyle());

        ChoiceBox<Patient> patientChoice = new ChoiceBox<>(patients);
        ChoiceBox<Doctor> doctorChoice = new ChoiceBox<>(doctors);
        TextField datetime = new TextField();
        datetime.setPromptText("YYYY-MM-DD HH:mm");

        Button addBtn = iconButton("Add Appointment", ICON_ADD);
        addBtn.setOnAction(e -> {
            Patient p = patientChoice.getValue();
            Doctor d = doctorChoice.getValue();
            if (p == null || d == null || datetime.getText().isEmpty()) {
                showAlert("Select patient, doctor and enter datetime.");
                return;
            }
            try {
                LocalDateTime dt = LocalDateTime.parse(datetime.getText(), formatter);
                Appointment a = new Appointment(0, p, d, dt, Appointment.Status.SCHEDULED);
                int id = repository.insertAppointment(a);
                appointments.add(new Appointment(id, p, d, dt, Appointment.Status.SCHEDULED));
                clear(datetime);
            } catch (Exception ex) {
                showAlert("Invalid datetime. Use YYYY-MM-DD HH:mm");
            }
        });

        Button delBtn = iconButton("Delete Selected", ICON_DELETE);
        delBtn.setOnAction(e -> {
            Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                repository.deleteAppointment(selected.getId());
                appointments.remove(selected);
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        Button markDelayedBtn = iconButton("Mark Selected as Delayed", ICON_WARNING);
        markDelayedBtn.setOnAction(e -> updateAppointmentStatus(Appointment.Status.DELAYED, true));

        Button markDoneBtn = iconButton("Mark Selected as Completed", ICON_DONE);
        markDoneBtn.setOnAction(e -> updateAppointmentStatus(Appointment.Status.COMPLETED, true));

        form.getChildren().addAll(header, new Label("Patient"), patientChoice, new Label("Doctor"),
                doctorChoice, datetime, addBtn, delBtn, markDelayedBtn, markDoneBtn);
        bp.setCenter(appointmentTable);
        bp.setRight(form);
        return bp;
    }

    private BorderPane createBillingTab() {
        BorderPane bp = new BorderPane();
        bp.setPadding(new Insets(12));

        Label header = headerLabelWithIcon("BILLING", ICON_APPOINT);
        billTable = new TableView<>(bills);
        billTable.getColumns().addAll(
                createColumn("Patient", "patientName", 200),
                createColumn("Amount", "amountStr", 100),
                createColumn("Description", "description", 200),
                createColumn("Status", "paymentStatusStr", 100),
                createColumn("Date", "billDateStr", 120)
        );
        styleTable(billTable);

        VBox form = new VBox(10);
        form.setPadding(new Insets(12));
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle(signStyle());

        ChoiceBox<Patient> patientChoice = new ChoiceBox<>(patients);
        ChoiceBox<Appointment> appointmentChoice = new ChoiceBox<>(appointments);
        TextField amount = new TextField();
        amount.setPromptText("Amount (BDT)");
        TextField description = new TextField();
        description.setPromptText("Description");
        DatePicker billDate = new DatePicker(LocalDate.now());

        Button addBtn = iconButton("Create Bill", ICON_ADD);
        addBtn.setOnAction(e -> {
            Patient p = patientChoice.getValue();
            if (p == null || !validateText(amount.getText()) || billDate.getValue() == null) {
                showAlert("Select patient, amount, and bill date.");
                return;
            }
            try {
                BigDecimal amt = new BigDecimal(amount.getText().trim());
                Appointment appt = appointmentChoice.getValue();
                Integer apptId = appt != null ? appt.getId() : null;
                Bill bill = new Bill(0, p.getId(), p.getName(), apptId, amt,
                        description.getText(), Bill.PaymentStatus.PENDING, billDate.getValue());
                int id = repository.insertBill(bill);
                bills.add(0, new Bill(id, bill.getPatientId(), bill.getPatientName(), bill.getAppointmentId(),
                        bill.getAmount(), bill.getDescription(), bill.getPaymentStatus(), bill.getBillDate()));
                clear(amount, description);
            } catch (NumberFormatException ex) {
                showAlert("Amount must be a valid number.");
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        Button markPaidBtn = iconButton("Mark Selected as Paid", ICON_DONE);
        markPaidBtn.setOnAction(e -> {
            Bill selected = billTable.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getPaymentStatus() == Bill.PaymentStatus.PAID) {
                return;
            }
            try {
                repository.updateBillPaymentStatus(selected.getId(), Bill.PaymentStatus.PAID);
                selected.setPaymentStatus(Bill.PaymentStatus.PAID);
                billTable.refresh();
                addNotification("Payment received: " + selected.getPatientName() + " — " + selected.getAmountStr());
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        Button delBtn = iconButton("Delete Selected", ICON_DELETE);
        delBtn.setOnAction(e -> {
            Bill selected = billTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                repository.deleteBill(selected.getId());
                bills.remove(selected);
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        form.getChildren().addAll(header, new Label("Patient"), patientChoice, new Label("Appointment (optional)"),
                appointmentChoice, amount, description, new Label("Bill date"), billDate,
                addBtn, markPaidBtn, delBtn);
        bp.setCenter(billTable);
        bp.setRight(form);
        return bp;
    }

    private BorderPane createNotificationTab() {
        BorderPane bp = new BorderPane();
        bp.setPadding(new Insets(12));

        Label header = headerLabelWithIcon("NOTIFICATIONS", ICON_NOTIFY);
        notificationList = new ListView<>(notifications);
        notificationList.setPrefHeight(420);
        notificationList.setStyle("-fx-border-color: #005b88; -fx-border-width: 2; "
                + "-fx-background-radius: 6; -fx-cell-size: 40px;");

        Button clear = iconButton("Clear All Notifications", ICON_DELETE);
        clear.setOnAction(e -> {
            try {
                repository.clearNotifications();
                notifications.clear();
            } catch (SQLException ex) {
                showError(sqlMessage(ex));
            }
        });

        VBox right = new VBox(10, header, notificationList, clear);
        right.setPadding(new Insets(12));
        right.setAlignment(Pos.TOP_CENTER);
        right.setStyle(signStyle());

        bp.setCenter(right);
        return bp;
    }

    private void updateAppointmentStatus(Appointment.Status status, boolean manual) {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getStatus() == status) {
            return;
        }
        try {
            repository.updateAppointmentStatus(selected.getId(), status);
            selected.setStatus(status);
            String prefix = status == Appointment.Status.DELAYED
                    ? (manual ? "Manual Delay" : "Delayed")
                    : "Completed";
            String icon = status == Appointment.Status.DELAYED ? "!" : "OK";
            addNotification(icon + " " + prefix + ": " + selected.getPatientName()
                    + " with Dr. " + selected.getDoctorName() + " @ " + selected.getTimeStr());
            appointmentTable.refresh();
        } catch (SQLException ex) {
            showError(sqlMessage(ex));
        }
    }

    private void refreshAll() {
        try {
            patients.setAll(repository.findAllPatients());
            doctors.setAll(repository.findAllDoctors());
            staff.setAll(repository.findAllStaff());
            appointments.setAll(repository.findAllAppointments(patients, doctors));
            bills.setAll(repository.findAllBills());
            notifications.setAll(repository.findAllNotifications());
            updateSidebarCounts();
        } catch (SQLException ex) {
            showError(sqlMessage(ex));
        }
    }

    private void reloadAppointments() {
        try {
            appointments.setAll(repository.findAllAppointments(patients, doctors));
        } catch (SQLException ex) {
            showError(sqlMessage(ex));
        }
    }

    private void reloadBills() {
        try {
            bills.setAll(repository.findAllBills());
        } catch (SQLException ex) {
            showError(sqlMessage(ex));
        }
    }

    private void updateSidebarCounts() {
        if (patientCountLabel != null) {
            patientCountLabel.setText("Patients: " + patients.size());
        }
        if (doctorCountLabel != null) {
            doctorCountLabel.setText("Doctors: " + doctors.size());
        }
    }

    private void addNotification(String message) {
        try {
            repository.insertNotification(message);
            notifications.add(0, message);
        } catch (SQLException ex) {
            showError(sqlMessage(ex));
        }
    }

    private void startNotificationChecker() {
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(10), e -> checkAppointments()));
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    private void checkAppointments() {
        LocalDateTime now = LocalDateTime.now();
        for (Appointment a : appointments) {
            if (a.getStatus() == Appointment.Status.SCHEDULED && a.getTime().isBefore(now)) {
                try {
                    repository.updateAppointmentStatus(a.getId(), Appointment.Status.DELAYED);
                    a.setStatus(Appointment.Status.DELAYED);
                    addNotification("Auto Delayed: " + a.getPatientName()
                            + " with Dr. " + a.getDoctorName() + " @ " + a.getTimeStr());
                    appointmentTable.refresh();
                } catch (SQLException ex) {
                    showError(sqlMessage(ex));
                    return;
                }
            }
        }
    }

    private <T> TableColumn<T, String> createColumn(String title, String prop, int width) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private Label headerLabelWithIcon(String text, String iconUrl) {
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER_LEFT);
        ImageView iv = loadIcon(iconUrl, 28);
        Label l = new Label(text);
        l.setFont(Font.font("Verdana", 18));
        l.setTextFill(Color.web("#002e5b"));
        hb.getChildren().addAll(iv, l);
        Label wrapper = new Label();
        wrapper.setGraphic(hb);
        return wrapper;
    }

    private void styleTable(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefWidth(760);
        table.setStyle("-fx-border-color: #00668f; -fx-border-width: 2; -fx-background-radius: 6; "
                + "-fx-border-radius: 6; -fx-selection-bar: #cdeeff; -fx-selection-bar-text: #002e5b;");
    }

    private String signStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #eaf9ff, #dff2fb); "
                + "-fx-border-color: #005b88; -fx-border-width: 2; -fx-background-radius: 8; "
                + "-fx-border-radius: 8; -fx-padding: 10;";
    }

    private boolean validateText(String s) {
        return s != null && !s.isBlank();
    }

    private void clear(TextField... fields) {
        for (TextField f : fields) {
            if (f != null) {
                f.clear();
            }
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Database Error");
        a.showAndWait();
    }

    private String sqlMessage(SQLException ex) {
        return ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
    }

    private Button iconButton(String text, String iconUrl) {
        ImageView iv = loadIcon(iconUrl, 16);
        Button b = new Button(text, iv);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-font-size:12; -fx-padding:8;");
        return b;
    }

    private ImageView loadIcon(String url, double size) {
        try {
            Image img = new Image(url, size, size, true, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            ImageView iv = new ImageView();
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            return iv;
        }
    }

    // ------------------ Data classes ------------------

    public static class Patient {
        private final int id;
        private final String name;
        private final int age;
        private final String gender;
        private final String phone;

        public Patient(int id, String name, int age, String gender, String phone) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.phone = phone;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getGender() {
            return gender;
        }

        public String getPhone() {
            return phone;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Doctor {
        private final int id;
        private final String name;
        private final String specialty;
        private final String phone;

        public Doctor(int id, String name, String specialty, String phone) {
            this.id = id;
            this.name = name;
            this.specialty = specialty;
            this.phone = phone;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSpecialty() {
            return specialty;
        }

        public String getPhone() {
            return phone;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Staff {
        private final int id;
        private final String name;
        private final String role;
        private final String phone;

        public Staff(int id, String name, String role, String phone) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.phone = phone;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }

        public String getPhone() {
            return phone;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Appointment {
        public enum Status {
            SCHEDULED, DELAYED, COMPLETED
        }

        private final int id;
        private final Patient patient;
        private final Doctor doctor;
        private final LocalDateTime time;
        private Status status;

        public Appointment(int id, Patient patient, Doctor doctor, LocalDateTime time, Status status) {
            this.id = id;
            this.patient = patient;
            this.doctor = doctor;
            this.time = time;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public Patient getPatient() {
            return patient;
        }

        public Doctor getDoctor() {
            return doctor;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public String getPatientName() {
            return patient.getName();
        }

        public String getDoctorName() {
            return doctor.getName();
        }

        public String getTimeStr() {
            return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        public String getStatusStr() {
            return status.name();
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status s) {
            this.status = s;
        }
    }

    public static class Bill {
        public enum PaymentStatus {
            PENDING, PAID
        }

        private final int id;
        private final int patientId;
        private final String patientName;
        private final Integer appointmentId;
        private final BigDecimal amount;
        private final String description;
        private PaymentStatus paymentStatus;
        private final LocalDate billDate;

        public Bill(int id, int patientId, String patientName, Integer appointmentId,
                    BigDecimal amount, String description, PaymentStatus paymentStatus, LocalDate billDate) {
            this.id = id;
            this.patientId = patientId;
            this.patientName = patientName;
            this.appointmentId = appointmentId;
            this.amount = amount;
            this.description = description == null ? "" : description;
            this.paymentStatus = paymentStatus;
            this.billDate = billDate;
        }

        public int getId() {
            return id;
        }

        public int getPatientId() {
            return patientId;
        }

        public String getPatientName() {
            return patientName;
        }

        public Integer getAppointmentId() {
            return appointmentId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public LocalDate getBillDate() {
            return billDate;
        }

        public String getAmountStr() {
            return amount.toPlainString();
        }

        public String getPaymentStatusStr() {
            return paymentStatus.name();
        }

        public String getBillDateStr() {
            return billDate.toString();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
