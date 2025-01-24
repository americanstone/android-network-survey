package com.craxiom.networksurvey.logging.db;

import android.content.Context;

import com.craxiom.messaging.CdmaRecord;
import com.craxiom.messaging.CdmaRecordData;
import com.craxiom.messaging.GsmRecord;
import com.craxiom.messaging.GsmRecordData;
import com.craxiom.messaging.LteRecord;
import com.craxiom.messaging.LteRecordData;
import com.craxiom.messaging.NrRecord;
import com.craxiom.messaging.NrRecordData;
import com.craxiom.messaging.UmtsRecord;
import com.craxiom.messaging.UmtsRecordData;
import com.craxiom.networksurvey.listeners.ICellularSurveyRecordListener;
import com.craxiom.networksurvey.logging.db.model.CdmaRecordEntity;
import com.craxiom.networksurvey.logging.db.model.GsmRecordEntity;
import com.craxiom.networksurvey.logging.db.model.LteRecordEntity;
import com.craxiom.networksurvey.logging.db.model.NrRecordEntity;
import com.craxiom.networksurvey.logging.db.model.UmtsRecordEntity;
import com.craxiom.networksurvey.model.CellularRecordWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DbUploadStore implements ICellularSurveyRecordListener
{
    private final SurveyDatabase database;
    private final ExecutorService executorService;

    public DbUploadStore(Context context)
    {
        database = SurveyDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onCellularBatch(List<CellularRecordWrapper> cellularGroup, int subscriptionId)
    {
        executorService.execute(() -> {
            //List<NrRecordEntity> allRecords = database.nrRecordDao().getAllRecords();
            //Timber.i("The database has %s NR records", allRecords.size());
            final List<GsmRecordEntity> gsmRecords = new ArrayList<>();
            final List<CdmaRecordEntity> cdmaRecords = new ArrayList<>();
            final List<UmtsRecordEntity> umtsRecords = new ArrayList<>();
            final List<LteRecordEntity> lteRecords = new ArrayList<>();
            final List<NrRecordEntity> nrRecords = new ArrayList<>();

            for (CellularRecordWrapper cellularRecordWrapper : cellularGroup)
            {
                switch (cellularRecordWrapper.cellularProtocol)
                {
                    case GSM:
                        GsmRecordData gsmRecordData = ((GsmRecord) cellularRecordWrapper.cellularRecord).getData();
                        GsmRecordEntity gsmEntity = mapGsmRecordToEntity(gsmRecordData);
                        gsmRecords.add(gsmEntity);
                        break;
                    case CDMA:
                        CdmaRecordData cdmaRecordData = ((CdmaRecord) cellularRecordWrapper.cellularRecord).getData();
                        CdmaRecordEntity cdmaEntity = mapCdmaRecordToEntity(cdmaRecordData);
                        cdmaRecords.add(cdmaEntity);
                        break;
                    case UMTS:
                        UmtsRecordData umtsRecordData = ((UmtsRecord) cellularRecordWrapper.cellularRecord).getData();
                        UmtsRecordEntity umtsEntity = mapUmtsRecordToEntity(umtsRecordData);
                        umtsRecords.add(umtsEntity);
                        break;
                    case LTE:
                        LteRecordData lteRecordData = ((LteRecord) cellularRecordWrapper.cellularRecord).getData();
                        LteRecordEntity lteEntity = mapLteRecordToEntity(lteRecordData);
                        lteRecords.add(lteEntity);
                        break;
                    case NR:
                        NrRecordData nrRecord = ((NrRecord) cellularRecordWrapper.cellularRecord).getData();
                        NrRecordEntity nrEntity = mapNrRecordToEntity(nrRecord);
                        nrRecords.add(nrEntity);
                        break;
                }
            }

            if (!gsmRecords.isEmpty())
            {
                database.gsmRecordDao().insertRecords(gsmRecords);
            }
            if (!cdmaRecords.isEmpty())
            {
                database.cdmaRecordDao().insertRecords(cdmaRecords);
            }
            if (!umtsRecords.isEmpty())
            {
                database.umtsRecordDao().insertRecords(umtsRecords);
            }
            if (!lteRecords.isEmpty())
            {
                database.lteRecordDao().insertRecords(lteRecords);
            }
            if (!nrRecords.isEmpty())
            {
                database.nrRecordDao().insertRecords(nrRecords);
            }
        });
    }

    private GsmRecordEntity mapGsmRecordToEntity(GsmRecordData record)
    {
        GsmRecordEntity entity = new GsmRecordEntity();
        entity.deviceSerialNumber = record.getDeviceSerialNumber();
        entity.deviceName = record.getDeviceName();
        entity.deviceTime = record.getDeviceTime();
        entity.latitude = record.getLatitude();
        entity.longitude = record.getLongitude();
        entity.altitude = record.getAltitude();
        entity.missionId = record.getMissionId();
        entity.recordNumber = record.getRecordNumber();
        entity.groupNumber = record.getGroupNumber();
        entity.accuracy = record.getAccuracy();
        entity.speed = record.getSpeed();

        entity.mcc = record.hasMcc() ? record.getMcc().getValue() : null;
        entity.mnc = record.hasMnc() ? record.getMnc().getValue() : null;
        entity.lac = record.hasLac() ? record.getLac().getValue() : null;
        entity.ci = record.hasCi() ? record.getCi().getValue() : null;
        entity.arfcn = record.hasArfcn() ? record.getArfcn().getValue() : null;
        entity.bsic = record.hasBsic() ? record.getBsic().getValue() : null;
        entity.signalStrength = record.hasSignalStrength() ? record.getSignalStrength().getValue() : null;
        entity.ta = record.hasTa() ? record.getTa().getValue() : null;
        entity.servingCell = record.hasServingCell() ? record.getServingCell().getValue() : null;
        entity.provider = record.getProvider();
        entity.slot = record.hasSlot() ? record.getSlot().getValue() : null;

        return entity;
    }

    private CdmaRecordEntity mapCdmaRecordToEntity(CdmaRecordData record)
    {
        CdmaRecordEntity entity = new CdmaRecordEntity();
        entity.deviceSerialNumber = record.getDeviceSerialNumber();
        entity.deviceName = record.getDeviceName();
        entity.deviceTime = record.getDeviceTime();
        entity.latitude = record.getLatitude();
        entity.longitude = record.getLongitude();
        entity.altitude = record.getAltitude();
        entity.missionId = record.getMissionId();
        entity.recordNumber = record.getRecordNumber();
        entity.groupNumber = record.getGroupNumber();
        entity.accuracy = record.getAccuracy();
        entity.speed = record.getSpeed();

        entity.sid = record.hasSid() ? record.getSid().getValue() : null;
        entity.nid = record.hasNid() ? record.getNid().getValue() : null;
        entity.zone = record.hasZone() ? record.getZone().getValue() : null;
        entity.bsid = record.hasBsid() ? record.getBsid().getValue() : null;
        entity.channel = record.hasChannel() ? record.getChannel().getValue() : null;
        entity.pnOffset = record.hasPnOffset() ? record.getPnOffset().getValue() : null;
        entity.signalStrength = record.hasSignalStrength() ? record.getSignalStrength().getValue() : null;
        entity.ecio = record.hasEcio() ? record.getEcio().getValue() : null;
        entity.servingCell = record.hasServingCell() ? record.getServingCell().getValue() : null;
        entity.provider = record.getProvider();
        entity.slot = record.hasSlot() ? record.getSlot().getValue() : null;

        return entity;
    }

    private UmtsRecordEntity mapUmtsRecordToEntity(UmtsRecordData record)
    {
        UmtsRecordEntity entity = new UmtsRecordEntity();
        entity.deviceSerialNumber = record.getDeviceSerialNumber();
        entity.deviceName = record.getDeviceName();
        entity.deviceTime = record.getDeviceTime();
        entity.latitude = record.getLatitude();
        entity.longitude = record.getLongitude();
        entity.altitude = record.getAltitude();
        entity.missionId = record.getMissionId();
        entity.recordNumber = record.getRecordNumber();
        entity.groupNumber = record.getGroupNumber();
        entity.accuracy = record.getAccuracy();
        entity.speed = record.getSpeed();

        entity.mcc = record.hasMcc() ? record.getMcc().getValue() : null;
        entity.mnc = record.hasMnc() ? record.getMnc().getValue() : null;
        entity.lac = record.hasLac() ? record.getLac().getValue() : null;
        entity.cid = record.hasCid() ? record.getCid().getValue() : null;
        entity.uarfcn = record.hasUarfcn() ? record.getUarfcn().getValue() : null;
        entity.psc = record.hasPsc() ? record.getPsc().getValue() : null;
        entity.rscp = record.hasRscp() ? record.getRscp().getValue() : null;
        entity.signalStrength = record.hasSignalStrength() ? record.getSignalStrength().getValue() : null;
        entity.ecno = record.hasEcno() ? record.getEcno().getValue() : null;
        entity.servingCell = record.hasServingCell() ? record.getServingCell().getValue() : null;
        entity.provider = record.getProvider();
        entity.slot = record.hasSlot() ? record.getSlot().getValue() : null;

        return entity;
    }

    private LteRecordEntity mapLteRecordToEntity(LteRecordData record)
    {
        LteRecordEntity entity = new LteRecordEntity();
        entity.deviceSerialNumber = record.getDeviceSerialNumber();
        entity.deviceName = record.getDeviceName();
        entity.deviceTime = record.getDeviceTime();
        entity.latitude = record.getLatitude();
        entity.longitude = record.getLongitude();
        entity.altitude = record.getAltitude();
        entity.missionId = record.getMissionId();
        entity.recordNumber = record.getRecordNumber();
        entity.groupNumber = record.getGroupNumber();
        entity.accuracy = record.getAccuracy();
        entity.speed = record.getSpeed();

        entity.mcc = record.hasMcc() ? record.getMcc().getValue() : null;
        entity.mnc = record.hasMnc() ? record.getMnc().getValue() : null;
        entity.tac = record.hasTac() ? record.getTac().getValue() : null;
        entity.eci = record.hasEci() ? record.getEci().getValue() : null;
        entity.earfcn = record.hasEarfcn() ? record.getEarfcn().getValue() : null;
        entity.pci = record.hasPci() ? record.getPci().getValue() : null;
        entity.rsrp = record.hasRsrp() ? record.getRsrp().getValue() : null;
        entity.rsrq = record.hasRsrq() ? record.getRsrq().getValue() : null;
        entity.ta = record.hasTa() ? record.getTa().getValue() : null;
        entity.servingCell = record.hasServingCell() ? record.getServingCell().getValue() : null;
        entity.lteBandwidth = record.getLteBandwidth().name();
        entity.provider = record.getProvider();
        entity.signalStrength = record.hasSignalStrength() ? record.getSignalStrength().getValue() : null;
        entity.cqi = record.hasCqi() ? record.getCqi().getValue() : null;
        entity.slot = record.hasSlot() ? record.getSlot().getValue() : null;
        entity.snr = record.hasSnr() ? record.getSnr().getValue() : null;

        return entity;
    }

    private NrRecordEntity mapNrRecordToEntity(NrRecordData record)
    {
        NrRecordEntity entity = new NrRecordEntity();
        entity.deviceSerialNumber = record.getDeviceSerialNumber();
        entity.deviceName = record.getDeviceName();
        entity.deviceTime = record.getDeviceTime();
        entity.latitude = record.getLatitude();
        entity.longitude = record.getLongitude();
        entity.altitude = record.getAltitude();
        entity.missionId = record.getMissionId();
        entity.recordNumber = record.getRecordNumber();
        entity.groupNumber = record.getGroupNumber();
        entity.accuracy = record.getAccuracy();
        entity.speed = record.getSpeed();

        entity.mcc = record.hasMcc() ? record.getMcc().getValue() : null;
        entity.mnc = record.hasMnc() ? record.getMnc().getValue() : null;
        entity.tac = record.hasTac() ? record.getTac().getValue() : null;
        entity.nci = record.hasNci() ? record.getNci().getValue() : null;
        entity.narfcn = record.hasNarfcn() ? record.getNarfcn().getValue() : null;
        entity.pci = record.hasPci() ? record.getPci().getValue() : null;
        entity.ssRsrp = record.hasSsRsrp() ? record.getSsRsrp().getValue() : null;
        entity.ssRsrq = record.hasSsRsrq() ? record.getSsRsrq().getValue() : null;
        entity.ssSinr = record.hasSsSinr() ? record.getSsSinr().getValue() : null;
        entity.csiRsrp = record.hasCsiRsrp() ? record.getCsiRsrp().getValue() : null;
        entity.csiRsrq = record.hasCsiRsrq() ? record.getCsiRsrq().getValue() : null;
        entity.csiSinr = record.hasCsiSinr() ? record.getCsiSinr().getValue() : null;
        entity.ta = record.hasTa() ? record.getTa().getValue() : null;
        entity.servingCell = record.hasServingCell() ? record.getServingCell().getValue() : null;
        entity.provider = record.getProvider();
        entity.slot = record.hasSlot() ? record.getSlot().getValue() : null;

        return entity;
    }

    // TODO Call this shutdown method from the survey service
    public void shutdown()
    {
        executorService.shutdown();
    }
}

