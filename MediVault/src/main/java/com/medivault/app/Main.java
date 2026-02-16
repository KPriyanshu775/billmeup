package com.medivault.app;

import com.medivault.model.Appointment;
import com.medivault.model.BillingRecord;
import com.medivault.model.Gender;
import com.medivault.model.Patient;
import com.medivault.model.Prescription;
import com.medivault.service.MediVaultService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final DateTimeFormatter APPOINTMENT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        MediVaultService service = new MediVaultService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> registerPatient(service, scanner);
                case "2" -> viewPatient(service, scanner);
                case "3" -> scheduleAppointment(service, scanner);
                case "4" -> addPrescription(service, scanner);
                case "5" -> addBilling(service, scanner);
                case "6" -> viewBillingSummary(service, scanner);
                case "0" -> {
                    System.out.println("Exiting MediVault.");
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n==== MediVault: Digital Health Record System ====");
        System.out.println("1. Register Patient");
        System.out.println("2. View Patient Details");
        System.out.println("3. Schedule Appointment");
        System.out.println("4. Add Prescription");
        System.out.println("5. Add Billing Record");
        System.out.println("6. View Patient Billing Summary");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void registerPatient(MediVaultService service, Scanner scanner) {
        System.out.print("Patient Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Date of Birth (yyyy-MM-dd): ");
        LocalDate dob = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Gender (MALE/FEMALE/OTHER): ");
        Gender gender = Gender.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Blood Group: ");
        String bloodGroup = scanner.nextLine().trim();
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine().trim();

        String patientId = "PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Patient patient = new Patient(patientId, name, dob, gender, bloodGroup, phone);
        service.registerPatient(patient);

        System.out.print("Add initial medical history note (optional): ");
        String history = scanner.nextLine().trim();
        patient.addMedicalHistory(history);

        System.out.println("Patient registered with ID: " + patientId);
    }

    private static void viewPatient(MediVaultService service, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine().trim();

        service.findPatient(patientId).ifPresentOrElse(patient -> {
            System.out.println(patient);
            System.out.println("Appointments: " + service.getAppointmentsByPatient(patientId).size());
            System.out.println("Prescriptions: " + service.getPrescriptionsByPatient(patientId).size());
        }, () -> System.out.println("Patient not found."));
    }

    private static void scheduleAppointment(MediVaultService service, Scanner scanner) {
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine().trim();

        if (service.findPatient(patientId).isEmpty()) {
            System.out.println("Patient not found. Register patient first.");
            return;
        }

        System.out.print("Doctor Name: ");
        String doctorName = scanner.nextLine().trim();
        System.out.print("Reason: ");
        String reason = scanner.nextLine().trim();
        System.out.print("Appointment DateTime (yyyy-MM-dd HH:mm): ");
        LocalDateTime dateTime = LocalDateTime.parse(scanner.nextLine().trim(), APPOINTMENT_FORMAT);

        Appointment appointment = new Appointment(
                "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                patientId,
                doctorName,
                reason,
                dateTime
        );
        service.addAppointment(appointment);
        System.out.println("Appointment scheduled: " + appointment.getAppointmentId());
    }

    private static void addPrescription(MediVaultService service, Scanner scanner) {
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine().trim();

        if (service.findPatient(patientId).isEmpty()) {
            System.out.println("Patient not found.");
            return;
        }

        System.out.print("Doctor Name: ");
        String doctorName = scanner.nextLine().trim();
        System.out.print("Medications (comma separated): ");
        String meds = scanner.nextLine().trim();
        System.out.print("Instructions: ");
        String instructions = scanner.nextLine().trim();

        Prescription prescription = new Prescription(
                "RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                patientId,
                doctorName,
                Arrays.stream(meds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList(),
                instructions,
                LocalDate.now()
        );

        service.addPrescription(prescription);
        System.out.println("Prescription added: " + prescription.getPrescriptionId());
    }

    private static void addBilling(MediVaultService service, Scanner scanner) {
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine().trim();

        if (service.findPatient(patientId).isEmpty()) {
            System.out.println("Patient not found.");
            return;
        }

        System.out.print("Billing Description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Amount: ");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        BillingRecord billing = new BillingRecord(
                "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                patientId,
                description,
                amount
        );

        service.addBillingRecord(billing);
        System.out.println("Billing record created: " + billing.getBillingId());
    }

    private static void viewBillingSummary(MediVaultService service, Scanner scanner) {
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine().trim();

        if (service.findPatient(patientId).isEmpty()) {
            System.out.println("Patient not found.");
            return;
        }

        var billings = service.getBillingByPatient(patientId);
        if (billings.isEmpty()) {
            System.out.println("No billing records found.");
            return;
        }

        System.out.println("Billing Records:");
        for (BillingRecord billing : billings) {
            System.out.println(billing);
        }

        double outstanding = service.getOutstandingAmount(patientId);
        System.out.printf("Outstanding Amount: %.2f%n", outstanding);
    }
}
