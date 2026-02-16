package com.medivault.service;

import com.medivault.model.Appointment;
import com.medivault.model.BillingRecord;
import com.medivault.model.Patient;
import com.medivault.model.Prescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MediVaultService {
    private final Map<String, Patient> patients = new HashMap<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private final List<Prescription> prescriptions = new ArrayList<>();
    private final List<BillingRecord> billingRecords = new ArrayList<>();

    public void registerPatient(Patient patient) {
        patients.put(patient.getPatientId(), patient);
    }

    public Optional<Patient> findPatient(String patientId) {
        return Optional.ofNullable(patients.get(patientId));
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }

    public void addBillingRecord(BillingRecord billingRecord) {
        billingRecords.add(billingRecord);
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return appointments.stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<Prescription> getPrescriptionsByPatient(String patientId) {
        return prescriptions.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<BillingRecord> getBillingByPatient(String patientId) {
        return billingRecords.stream()
                .filter(b -> b.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public double getOutstandingAmount(String patientId) {
        return getBillingByPatient(patientId).stream()
                .filter(b -> !b.isPaid())
                .mapToDouble(BillingRecord::getAmount)
                .sum();
    }
}
