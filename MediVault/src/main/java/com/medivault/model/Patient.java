package com.medivault.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Patient {
    private final String patientId;
    private final String fullName;
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final String bloodGroup;
    private final String phone;
    private final List<String> medicalHistory;

    public Patient(String patientId, String fullName, LocalDate dateOfBirth, Gender gender,
                   String bloodGroup, String phone) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.medicalHistory = new ArrayList<>();
    }

    public String getPatientId() {
        return patientId;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getMedicalHistory() {
        return medicalHistory;
    }

    public void addMedicalHistory(String note) {
        if (note != null && !note.isBlank()) {
            medicalHistory.add(note.trim());
        }
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", phone='" + phone + '\'' +
                ", medicalHistory=" + medicalHistory +
                '}';
    }
}
