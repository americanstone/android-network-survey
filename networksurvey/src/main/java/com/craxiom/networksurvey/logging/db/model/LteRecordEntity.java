package com.craxiom.networksurvey.logging.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lte_survey_records")
public class LteRecordEntity
{
    @PrimaryKey(autoGenerate = true)
    public long id;

    public boolean ocidUploaded = false;
    public boolean beaconDbUploaded = false;

    @NonNull
    public String deviceSerialNumber;
    @NonNull
    public String deviceName;
    @NonNull
    public String deviceTime;
    public double latitude;
    public double longitude;
    public float altitude;
    public String missionId;
    public int recordNumber;
    public int groupNumber;
    public int accuracy;
    public Float speed;

    // LTE-specific fields with nullable support
    public Integer mcc;
    public Integer mnc;
    public Integer tac;
    public Integer eci;
    public Integer earfcn;
    public Integer pci;
    public Float rsrp;
    public Float rsrq;
    public Integer ta;
    public Boolean servingCell;
    public String lteBandwidth; // Consider storing as a string or mapping to an enum
    public String provider;
    public Float signalStrength;
    public Integer cqi;
    public Integer slot;
    public Float snr;
}

