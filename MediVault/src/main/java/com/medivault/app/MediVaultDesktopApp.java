package com.medivault.app;

import com.medivault.model.Appointment;
import com.medivault.model.BillingRecord;
import com.medivault.model.Gender;
import com.medivault.model.Patient;
import com.medivault.model.Prescription;
import com.medivault.service.MediVaultService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

public class MediVaultDesktopApp extends JFrame {
    private static final DateTimeFormatter APPOINTMENT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MediVaultService service = new MediVaultService();
    private final DefaultComboBoxModel<String> patientIdsModel = new DefaultComboBoxModel<>();
    private final JTextArea dashboard = new JTextArea();

    public MediVaultDesktopApp() {
        super("MediVault - Digital Health Record System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Register Patient", buildRegisterPanel());
        tabs.addTab("Appointment", buildAppointmentPanel());
        tabs.addTab("Prescription", buildPrescriptionPanel());
        tabs.addTab("Billing", buildBillingPanel());

        dashboard.setEditable(false);
        dashboard.setLineWrap(true);
        dashboard.setWrapStyleWord(true);

        JPanel root = new JPanel(new BorderLayout());
        root.add(tabs, BorderLayout.CENTER);
        root.add(new JScrollPane(dashboard), BorderLayout.SOUTH);

        add(root);
        refreshDashboard();
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = createFormPanel();

        JTextField nameField = new JTextField();
        JTextField dobField = new JTextField("2000-01-01");
        JComboBox<Gender> genderBox = new JComboBox<>(Gender.values());
        JTextField bloodField = new JTextField("O+");
        JTextField phoneField = new JTextField();
        JTextField historyField = new JTextField();

        panel.add(label("Full Name"));
        panel.add(nameField);
        panel.add(label("Date of Birth (yyyy-MM-dd)"));
        panel.add(dobField);
        panel.add(label("Gender"));
        panel.add(genderBox);
        panel.add(label("Blood Group"));
        panel.add(bloodField);
        panel.add(label("Phone"));
        panel.add(phoneField);
        panel.add(label("Medical History Note"));
        panel.add(historyField);

        JButton submit = new JButton("Register Patient");
        submit.addActionListener(e -> {
            try {
                String patientId = "PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Patient patient = new Patient(
                        patientId,
                        nameField.getText().trim(),
                        LocalDate.parse(dobField.getText().trim()),
                        (Gender) genderBox.getSelectedItem(),
                        bloodField.getText().trim(),
                        phoneField.getText().trim()
                );
                patient.addMedicalHistory(historyField.getText().trim());
                service.registerPatient(patient);
                patientIdsModel.addElement(patientId);
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Patient registered: " + patientId);
            } catch (Exception ex) {
                showError(ex);
            }
        });

        panel.add(submit);
        return panel;
    }

    private JPanel buildAppointmentPanel() {
        JPanel panel = createFormPanel();

        JComboBox<String> patientBox = new JComboBox<>(patientIdsModel);
        JTextField doctorField = new JTextField();
        JTextField reasonField = new JTextField();
        JTextField dateTimeField = new JTextField("2026-02-16 10:30");

        panel.add(label("Patient ID"));
        panel.add(patientBox);
        panel.add(label("Doctor Name"));
        panel.add(doctorField);
        panel.add(label("Reason"));
        panel.add(reasonField);
        panel.add(label("Appointment DateTime (yyyy-MM-dd HH:mm)"));
        panel.add(dateTimeField);

        JButton submit = new JButton("Schedule Appointment");
        submit.addActionListener(e -> {
            try {
                String patientId = (String) patientBox.getSelectedItem();
                ensurePatientSelected(patientId);
                Appointment appointment = new Appointment(
                        "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        patientId,
                        doctorField.getText().trim(),
                        reasonField.getText().trim(),
                        LocalDateTime.parse(dateTimeField.getText().trim(), APPOINTMENT_FORMAT)
                );
                service.addAppointment(appointment);
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Appointment created: " + appointment.getAppointmentId());
            } catch (Exception ex) {
                showError(ex);
            }
        });

        panel.add(submit);
        return panel;
    }

    private JPanel buildPrescriptionPanel() {
        JPanel panel = createFormPanel();

        JComboBox<String> patientBox = new JComboBox<>(patientIdsModel);
        JTextField doctorField = new JTextField();
        JTextField medsField = new JTextField("Paracetamol 500mg, Vitamin D");
        JTextField instructionField = new JTextField();

        panel.add(label("Patient ID"));
        panel.add(patientBox);
        panel.add(label("Doctor Name"));
        panel.add(doctorField);
        panel.add(label("Medications (comma separated)"));
        panel.add(medsField);
        panel.add(label("Instructions"));
        panel.add(instructionField);

        JButton submit = new JButton("Add Prescription");
        submit.addActionListener(e -> {
            try {
                String patientId = (String) patientBox.getSelectedItem();
                ensurePatientSelected(patientId);
                Prescription prescription = new Prescription(
                        "RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        patientId,
                        doctorField.getText().trim(),
                        Arrays.stream(medsField.getText().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .toList(),
                        instructionField.getText().trim(),
                        LocalDate.now()
                );
                service.addPrescription(prescription);
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Prescription created: " + prescription.getPrescriptionId());
            } catch (Exception ex) {
                showError(ex);
            }
        });

        panel.add(submit);
        return panel;
    }

    private JPanel buildBillingPanel() {
        JPanel panel = createFormPanel();

        JComboBox<String> patientBox = new JComboBox<>(patientIdsModel);
        JTextField descriptionField = new JTextField();
        JTextField amountField = new JTextField("0.0");

        panel.add(label("Patient ID"));
        panel.add(patientBox);
        panel.add(label("Description"));
        panel.add(descriptionField);
        panel.add(label("Amount"));
        panel.add(amountField);

        JButton submit = new JButton("Add Billing Record");
        submit.addActionListener(e -> {
            try {
                String patientId = (String) patientBox.getSelectedItem();
                ensurePatientSelected(patientId);
                BillingRecord billing = new BillingRecord(
                        "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        patientId,
                        descriptionField.getText().trim(),
                        Double.parseDouble(amountField.getText().trim())
                );
                service.addBillingRecord(billing);
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Billing added: " + billing.getBillingId());
            } catch (Exception ex) {
                showError(ex);
            }
        });

        JButton summaryButton = new JButton("Show Patient Billing Summary");
        summaryButton.addActionListener(e -> {
            String patientId = (String) patientBox.getSelectedItem();
            if (patientId == null) {
                JOptionPane.showMessageDialog(this, "Please register a patient first.");
                return;
            }
            double outstanding = service.getOutstandingAmount(patientId);
            JOptionPane.showMessageDialog(this,
                    "Patient: " + patientId + "\nOutstanding Amount: " + String.format("%.2f", outstanding));
        });

        panel.add(submit);
        panel.add(summaryButton);
        return panel;
    }

    private JLabel label(String text) {
        return new JLabel(text);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setPreferredSize(new Dimension(820, 420));
        return panel;
    }

    private void ensurePatientSelected(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalStateException("Please register a patient first.");
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void refreshDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("Patients: ").append(service.getAllPatients().size()).append("\n");

        for (Patient patient : service.getAllPatients()) {
            int appointmentCount = service.getAppointmentsByPatient(patient.getPatientId()).size();
            int prescriptionCount = service.getPrescriptionsByPatient(patient.getPatientId()).size();
            double outstanding = service.getOutstandingAmount(patient.getPatientId());
            sb.append(patient.getPatientId())
                    .append(" | ")
                    .append(patient.getFullName())
                    .append(" | Appointments: ").append(appointmentCount)
                    .append(" | Prescriptions: ").append(prescriptionCount)
                    .append(" | Outstanding: ").append(String.format("%.2f", outstanding))
                    .append("\n");
        }

        dashboard.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MediVaultDesktopApp().setVisible(true));
    }
}
