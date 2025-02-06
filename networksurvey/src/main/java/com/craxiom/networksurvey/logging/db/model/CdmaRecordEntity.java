package com.craxiom.networksurvey.logging.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cdma_survey_records")
public class CdmaRecordEntity
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

    // CDMA-specific fields (nullable due to protobuf Int32Value and FloatValue)
    public Integer sid;
    public Integer nid;
    public Integer zone;
    public Integer bsid;
    public Integer channel;
    public Integer pnOffset;
    public Float signalStrength;
    public Float ecio;
    public Boolean servingCell;
    public String provider;
    public Integer slot;
}
