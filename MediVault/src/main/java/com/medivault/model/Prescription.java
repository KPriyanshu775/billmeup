package com.medivault.model;

import java.time.LocalDate;
import java.util.List;

public class Prescription {
    private final String prescriptionId;
    private final String patientId;
    private final String doctorName;
    private final List<String> medications;
    private final String instructions;
    private final LocalDate issueDate;

    public Prescription(String prescriptionId, String patientId, String doctorName, List<String> medications,
                        String instructions, LocalDate issueDate) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.medications = medications;
        this.instructions = instructions;
        this.issueDate = issueDate;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public List<String> getMedications() {
        return medications;
    }

    public String getInstructions() {
        return instructions;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "prescriptionId='" + prescriptionId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", medications=" + medications +
                ", instructions='" + instructions + '\'' +
                ", issueDate=" + issueDate +
                '}';
    }
}
