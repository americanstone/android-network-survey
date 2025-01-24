package com.craxiom.networksurvey.logging.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "gsm_survey_records")
public class GsmRecordEntity
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

    // GSM-specific fields (nullable due to protobuf Int32Value and FloatValue)
    public Integer mcc;
    public Integer mnc;
    public Integer lac;
    public Integer ci;
    public Integer arfcn;
    public Integer bsic;
    public Float signalStrength;
    public Integer ta;
    public Boolean servingCell;
    public String provider;
    public Integer slot;
}
