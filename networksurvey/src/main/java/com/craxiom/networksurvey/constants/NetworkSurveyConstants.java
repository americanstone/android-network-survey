package com.craxiom.networksurvey.constants;

/**
 * Some constants used in the Network Survey App.
 *
 * @since 0.0.4
 */
public class NetworkSurveyConstants
{
    private NetworkSurveyConstants()
    {
    }

    public static final int DEFAULT_GRPC_PORT = 2621;

    public static final String NOTIFICATION_CHANNEL_ID = "network_survey_notification";
    public static final int CONNECTION_NOTIFICATION_ID = 3;
    public static final int LOGGING_NOTIFICATION_ID = 1;

    public static final String GPRS = "GRPS";
    public static final String EDGE = "EDGE";
    public static final String UMTS = "UMTS";
    public static final String CDMA = "CDMA";
    public static final String EVDO_0 = "EVDO 0";
    public static final String EVDO_A = "EVDO A";
    public static final String RTT1x = "CDMA - 1xRTT";
    public static final String HSDPA = "HSDPA";
    public static final String HSUPA = "HSUPA";
    public static final String HSPA = "HSPA";
    public static final String IDEN = "IDEN";
    public static final String EVDO_B = "EVDO B";
    public static final String LTE = "LTE";
    public static final String EHRPD = "CDMA - eHRPD";
    public static final String HSPAP = "HSPA+";
    public static final String GSM = "GSM";
    public static final String TD_SCDMA = "TD-SCDMA";
    public static final String IWLAN = "IWLAN";
    public static final String LTE_CA = "LTE-CA";
    public static final String NR = "NR";

    // Preferences
    public static final String PROPERTY_AUTO_START_CELLULAR_LOGGING = "auto_start_logging";
    public static final String PROPERTY_AUTO_START_GNSS_LOGGING = "auto_start_gnss_logging";
    public static final String PROPERTY_CONNECTION_TIMEOUT = "connection_timeout";
    public static final String PROPERTY_MQTT_MDM_OVERRIDE = "mqtt_mdm_override";
    // The following 4 keys are used in the app_restrictions.xml file as well.
    public static final String PROPERTY_MQTT_BROKER_URL = "mqtt_broker_url";
    public static final String PROPERTY_MQTT_CLIENT_ID = "mqtt_client_id";
    public static final String PROPERTY_MQTT_USERNAME = "mqtt_username";
    public static final String PROPERTY_MQTT_PASSWORD = "mqtt_password";

    // Stored Preferences not exposed via the Settings UI
    public static final String PROPERTY_NETWORK_SURVEY_CONNECTION_HOST = "connection_host";
    public static final String PROPERTY_NETWORK_SURVEY_CONNECTION_PORT = "connection_port";
    public static final String PROPERTY_NETWORK_SURVEY_DEVICE_NAME = "device_name";
}
