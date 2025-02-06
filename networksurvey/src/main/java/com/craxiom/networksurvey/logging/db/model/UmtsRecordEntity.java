package com.craxiom.networksurvey.logging.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "umts_survey_records")
public class UmtsRecordEntity
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

    // UMTS-specific fields (nullable due to protobuf Int32Value and FloatValue)
    public Integer mcc;
    public Integer mnc;
    public Integer lac;
    public Integer cid;
    public Integer uarfcn;
    public Integer psc;
    public Float rscp;
    public Float signalStrength;
    public Float ecno;
    public Boolean servingCell;
    public String provider;
    public Integer slot;
}
