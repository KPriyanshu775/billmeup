package com.medivault.model;

public class BillingRecord {
    private final String billingId;
    private final String patientId;
    private final String description;
    private final double amount;
    private boolean paid;

    public BillingRecord(String billingId, String patientId, String description, double amount) {
        this.billingId = billingId;
        this.patientId = patientId;
        this.description = description;
        this.amount = amount;
        this.paid = false;
    }

    public String getBillingId() {
        return billingId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void markPaid() {
        this.paid = true;
    }

    @Override
    public String toString() {
        return "BillingRecord{" +
                "billingId='" + billingId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", paid=" + paid +
                '}';
    }
}
