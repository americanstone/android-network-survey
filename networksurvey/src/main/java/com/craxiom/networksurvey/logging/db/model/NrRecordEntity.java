package com.craxiom.networksurvey.logging.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "nr_survey_records")
public class NrRecordEntity
{
    @PrimaryKey(autoGenerate = true)
    public long id;

    public boolean uploaded = false;

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

    // NR specific fields (nullable due to protobuf Int32Value and FloatValue)
    public Integer mcc;
    public Integer mnc;
    public Integer tac;
    public Long nci;
    public Integer narfcn;
    public Integer pci;
    public Float ssRsrp;
    public Float ssRsrq;
    public Float ssSinr;
    public Float csiRsrp;
    public Float csiRsrq;
    public Float csiSinr;
    public Integer ta;
    public Boolean servingCell;
    public String provider;
    public Integer slot;
}

