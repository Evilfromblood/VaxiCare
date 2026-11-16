package com.vaxicare.model;

import java.util.Objects;

/**
 * Model class representing an administered vaccination dose record.
 */
public class DoseRecord {

    private int doseId;
    private String benId;
    private String vaccineName;
    private int doseNo;
    private String doseDate;
    private String nextDue;
    private String certificateNo;

    public DoseRecord() {
    }

    public DoseRecord(int doseId, String benId, String vaccineName, int doseNo, String doseDate, String nextDue, String certificateNo) {
        this.doseId = doseId;
        this.benId = benId;
        this.vaccineName = vaccineName;
        this.doseNo = doseNo;
        this.doseDate = doseDate;
        this.nextDue = nextDue;
        this.certificateNo = certificateNo;
    }

    public DoseRecord(String benId, String vaccineName, int doseNo, String doseDate, String nextDue, String certificateNo) {
        this(0, benId, vaccineName, doseNo, doseDate, nextDue, certificateNo);
    }

    // Getters and Setters
    public int getDoseId() {
        return doseId;
    }

    public void setDoseId(int doseId) {
        this.doseId = doseId;
    }

    public String getBenId() {
        return benId;
    }

    public void setBenId(String benId) {
        this.benId = benId;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public int getDoseNo() {
        return doseNo;
    }

    public void setDoseNo(int doseNo) {
        this.doseNo = doseNo;
    }

    public String getDoseDate() {
        return doseDate;
    }

    public void setDoseDate(String doseDate) {
        this.doseDate = doseDate;
    }

    public String getNextDue() {
        return nextDue;
    }

    public void setNextDue(String nextDue) {
        this.nextDue = nextDue;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoseRecord that = (DoseRecord) o;
        return doseId == that.doseId &&
                doseNo == that.doseNo &&
                Objects.equals(benId, that.benId) &&
                Objects.equals(vaccineName, that.vaccineName) &&
                Objects.equals(doseDate, that.doseDate) &&
                Objects.equals(nextDue, that.nextDue) &&
                Objects.equals(certificateNo, that.certificateNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doseId, benId, vaccineName, doseNo, doseDate, nextDue, certificateNo);
    }

    @Override
    public String toString() {
        return "DoseRecord{" +
                "doseId=" + doseId +
                ", benId='" + benId + '\'' +
                ", vaccineName='" + vaccineName + '\'' +
                ", doseNo=" + doseNo +
                ", doseDate='" + doseDate + '\'' +
                ", nextDue='" + nextDue + '\'' +
                ", certificateNo='" + certificateNo + '\'' +
                '}';
    }
}
