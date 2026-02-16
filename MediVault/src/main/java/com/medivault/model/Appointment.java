package com.medivault.model;

import java.time.LocalDateTime;

public class Appointment {
    private final String appointmentId;
    private final String patientId;
    private final String doctorName;
    private final String reason;
    private final LocalDateTime dateTime;
    private AppointmentStatus status;

    public Appointment(String appointmentId, String patientId, String doctorName,
                       String reason, LocalDateTime dateTime) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.reason = reason;
        this.dateTime = dateTime;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", reason='" + reason + '\'' +
                ", dateTime=" + dateTime +
                ", status=" + status +
                '}';
    }
}
