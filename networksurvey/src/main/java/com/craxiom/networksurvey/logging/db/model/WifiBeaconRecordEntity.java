package com.craxiom.networksurvey.logging.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wifi_survey_records")
public class WifiBeaconRecordEntity
{
    @PrimaryKey(autoGenerate = true)
    public long id;

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
    public int accuracy;
    public Float speed;

    public String sourceAddress;
    public String destinationAddress;
    public String bssid;

    // Wi-Fi-specific fields (nullable due to protobuf Int32Value and FloatValue)
    public Integer beaconInterval;
    public String serviceSetType;  // Enum stored as string
    public String ssid;
    public String supportedRates;
    public String extendedSupportedRates;
    public String cipherSuites;  // Stored as a comma-separated string
    public String akmSuites;     // Stored as a comma-separated string
    public String encryptionType; // Enum stored as string
    public Boolean wps;
    public Boolean passpoint;
    public String bandwidth;  // Enum stored as string

    public Integer channel;
    public Integer frequencyMhz;
    public Float signalStrength;
    public Float snr;
    public String nodeType;  // Enum stored as string
    public String standard;  // Enum stored as string
}
