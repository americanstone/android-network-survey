package com.craxiom.networksurvey.logging.db.uploader;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.craxiom.networksurvey.BuildConfig;
import com.craxiom.networksurvey.constants.NetworkSurveyConstants;
import com.craxiom.networksurvey.logging.db.SurveyDatabase;
import com.craxiom.networksurvey.logging.db.dao.SurveyRecordDao;
import com.craxiom.networksurvey.logging.db.model.CdmaRecordEntity;
import com.craxiom.networksurvey.logging.db.model.CellularRecordsWrapper;
import com.craxiom.networksurvey.logging.db.model.GsmRecordEntity;
import com.craxiom.networksurvey.logging.db.model.LteRecordEntity;
import com.craxiom.networksurvey.logging.db.model.NrRecordEntity;
import com.craxiom.networksurvey.logging.db.model.UmtsRecordEntity;
import com.craxiom.networksurvey.logging.db.uploader.beacondb.BeaconDbUploadClient;
import com.craxiom.networksurvey.logging.db.uploader.ocid.OpenCelliDCsvFormatter;
import com.craxiom.networksurvey.logging.db.uploader.ocid.OpenCelliDUploadClient;
import com.craxiom.networksurvey.util.PreferenceUtils;

import java.util.List;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

/**
 * This class was pulled from the Tower Collector app and modified to work with Network Survey.
 * <p>
 * See: <a href="https://github.com/zamojski/TowerCollector/blob/7c8c4ff7bc2a536a94a34e059189f905ecd52b34/app/src/main/java/info/zamojski/soft/towercollector/uploader/UploaderWorker.java">here</a>
 */
public class NsUploaderWorker extends Worker
{
    public static final String SERVICE_FULL_NAME = NsUploaderWorker.class.getCanonicalName();
    public static final String WORKER_TAG = "NS_UPLOADER_WORKER";
    public static final MediaType MEDIA_TYPE_CSV = MediaType.parse("text/csv; charset=UTF-8");

    public static final String PROGRESS = "PROGRESS";
    public static final String PROGRESS_MAX = "PROGRESS_MAX";
    public static final String PROGRESS_STATUS_MESSAGE = "PROGRESS_STATUS_MESSAGE";
    public static final int PROGRESS_MIN_VALUE = 0;
    public static final int PROGRESS_MAX_VALUE = 100;
    public static final String OCID_RESULT = "OCID_RESULT";
    public static final String BEACONDB_RESULT = "OCID_RESULT";
    public static final String OCID_RESULT_MESSAGE = "OCID_RESULT_MESSAGE";
    public static final String BEACONDB_RESULT_MESSAGE = "BEACONDB_RESULT_MESSAGE";
    public static final int NOTIFICATION_ID = 102;
    private static final int LOCATIONS_PER_PART = 100; // Batch size for uploads
    public static final String OCID_APP_ID = "NetworkSurvey " + BuildConfig.VERSION_NAME;

    private final NotificationManager notificationManager;
    private final UploaderNotificationHelper notificationHelper;
    private final SurveyDatabase database;

    private boolean isOpenCellIdUploadEnabled;
    private boolean anonymousUploadToOcid;
    private boolean isBeaconDBUploadEnabled;
    private boolean isRetryEnabled;

    public NsUploaderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationHelper = new UploaderNotificationHelper(context);
        database = SurveyDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        try
        {
            Notification notification = notificationHelper.createNotification(notificationManager);
            notificationManager.notify(NOTIFICATION_ID, notification);

            Timber.d("Starting upload process...");
            // TODO Prevent a second trigger of this doWork somehow (Tower Collector uses an application static variable)

            // Read work input parameters
            isOpenCellIdUploadEnabled = getInputData().getBoolean(NetworkSurveyConstants.PROPERTY_UPLOAD_TO_OPENCELLID, false);
            anonymousUploadToOcid = getInputData().getBoolean(NetworkSurveyConstants.PROPERTY_ANONYMOUS_OPENCELLID_UPLOAD, false);
            isBeaconDBUploadEnabled = getInputData().getBoolean(NetworkSurveyConstants.PROPERTY_UPLOAD_TO_BEACONDB, false);
            isRetryEnabled = getInputData().getBoolean(NetworkSurveyConstants.PROPERTY_UPLOAD_RETRY_ENABLED, false);

            UploadResultBundle uploadResultBundle = new UploadResultBundle();
            int totalRecords = getTotalCellularRecordsForUpload(database.surveyRecordDao());
            if (totalRecords == 0)
            {
                Timber.d("No records to upload.");
                uploadResultBundle.setResult(UploadTarget.OpenCelliD, UploadResult.NoData);
                uploadResultBundle.setResult(UploadTarget.BeaconDB, UploadResult.NoData);
                return Result.success(getResultData(uploadResultBundle));
            }

            int partsCount = (int) Math.ceil((double) totalRecords / LOCATIONS_PER_PART);
            for (int i = 0; i < partsCount; i++)
            {
                if (isStopped())
                {
                    Timber.d("Upload cancelled, stopping upload processing loop");
                    uploadResultBundle.markAllCancelled();
                    return Result.failure(getResultData(uploadResultBundle));
                }

                int progress = (int) (100.0 * i / partsCount);
                reportProgress(progress, PROGRESS_MAX_VALUE, "Uploading records...");

                // TODO Keep track using partially uploaded status, and then switch to success at the end
                uploadResultBundle.merge(processUploadBatch(LOCATIONS_PER_PART));
                if (!uploadResultBundle.isAllSuccess())
                {
                    if (isRetryEnabled)
                    {
                        Timber.d("Upload failed, retry enabled.");
                        reportProgress(progress, PROGRESS_MAX_VALUE, "An error occurred, will retry later");
                        return Result.retry();
                    } else
                    {
                        Timber.e("Upload failed with no retry.");
                        return Result.failure(getResultData(uploadResultBundle));
                    }
                }

                // Progress update
                reportProgress((i + 1) * 100 / partsCount, PROGRESS_MAX_VALUE, "Uploading records...");
            }

            Timber.d("Upload process completed.");
            return Result.success(getResultData(uploadResultBundle));
        } catch (Exception e)
        {
            Timber.e(e, "Upload process failed.");
            UploadResultBundle uploadResultBundle = new UploadResultBundle();
            uploadResultBundle.markAllFailure();
            return Result.failure(getResultData(uploadResultBundle));
        } finally
        {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public void onStopped()
    {
        Timber.d("onStopped: Upload cancelled");
        notificationManager.cancel(NOTIFICATION_ID);
        super.onStopped();
    }

    private UploadResultBundle processUploadBatch(int batchSize)
    {
        List<GsmRecordEntity> gsmRecords = database.surveyRecordDao().getGsmRecordsForUpload(batchSize);
        List<CdmaRecordEntity> cdmaRecords = database.surveyRecordDao().getCdmaRecordsForUpload(batchSize);
        List<UmtsRecordEntity> umtsRecords = database.surveyRecordDao().getUmtsRecordsForUpload(batchSize);
        List<LteRecordEntity> lteRecords = database.surveyRecordDao().getLteRecordsForUpload(batchSize);
        List<NrRecordEntity> nrRecords = database.surveyRecordDao().getNrRecordsForUpload(batchSize);

        // Create a combined results bundle
        UploadResultBundle totalResultBundle = new UploadResultBundle();

        if (!nrRecords.isEmpty())
        {
            totalResultBundle.merge(processUpload(nrRecords));
        }
        if (!lteRecords.isEmpty())
        {
            totalResultBundle.merge(processUpload(lteRecords));
        }
        if (!umtsRecords.isEmpty())
        {
            totalResultBundle.merge(processUpload(umtsRecords));
        }
        if (!cdmaRecords.isEmpty())
        {
            totalResultBundle.merge(processUpload(cdmaRecords));
        }
        if (!gsmRecords.isEmpty())
        {
            totalResultBundle.merge(processUpload(gsmRecords));
        }

        return totalResultBundle;
    }

    private <T> UploadResultBundle processUpload(List<T> records)
    {
        UploadResultBundle result = uploadRecords(records);
        Timber.i("UploadResultBundle OCID=%s, BeaconDB=%s",
                result.getResult(UploadTarget.OpenCelliD),
                result.getResult(UploadTarget.BeaconDB)
        );

        if (result.getResult(UploadTarget.OpenCelliD) == UploadResult.Success)
        {
            markRecordsAsUploadedToOcid(records);
        }

        if (result.getResult(UploadTarget.BeaconDB) == UploadResult.Success)
        {
            markRecordsAsUploadedToBeaconDb(records);
        }

        return result;
    }

    private <T> UploadResultBundle uploadRecords(List<T> records)
    {
        final UploadResultBundle uploadResultBundle = new UploadResultBundle();
        try
        {
            final CellularRecordsWrapper recordsWrapper = CellularRecordsWrapper.createCellularRecordsWrapper(records);

            if (isOpenCellIdUploadEnabled)
            {
                final String ocidApiKeyString = PreferenceUtils.getOpenCelliDApiKey(getApplicationContext(), anonymousUploadToOcid);
                RequestBody apiKey = RequestBody.create(ocidApiKeyString, MultipartBody.FORM);
                RequestBody appId = RequestBody.create(OCID_APP_ID, MultipartBody.FORM);

                String csvContent = OpenCelliDCsvFormatter.formatRecords(recordsWrapper);
                RequestBody requestFile = RequestBody.create(csvContent, MEDIA_TYPE_CSV);
                MultipartBody.Part multipartFile = MultipartBody.Part.createFormData("datafile", "NetworkSurvey_measurements_" + System.currentTimeMillis() + ".csv", requestFile);

                OpenCelliDUploadClient ocidClient = OpenCelliDUploadClient.getInstance();
                Response<ResponseBody> response = ocidClient.uploadToOcid(apiKey, appId, multipartFile).execute();
                try (ResponseBody body = response.body())
                {
                    assert body != null;
                    RequestResult requestResult = OpenCelliDUploadClient.handleOcidResponse(response.code(), body);
                    Timber.d("Server response: %s", requestResult);
                    UploadResult uploadResult = OpenCelliDUploadClient.mapRequestResultToUploadResult(requestResult);
                    uploadResultBundle.setResult(UploadTarget.OpenCelliD, uploadResult);
                } catch (Exception e)
                {
                    Timber.e(e, "OpenCelliD upload failed due to exception.");
                    uploadResultBundle.setResult(UploadTarget.OpenCelliD, UploadResult.Failure);
                }
            } else
            {
                Timber.d("OpenCelliD upload not enabled.");
                // When the user does not enable a target, we still need to mark the records as uploaded so they can be deleted
                uploadResultBundle.markSuccessful(UploadTarget.OpenCelliD);
            }

            if (isBeaconDBUploadEnabled)
            {
                BeaconDbUploadClient beaconDbClient = BeaconDbUploadClient.getInstance();
                Response<ResponseBody> response = beaconDbClient.uploadToBeaconDB(recordsWrapper).execute();
                try (ResponseBody body = response.body())
                {
                    assert body != null;
                    RequestResult requestResult = BeaconDbUploadClient.handleBeaconDbResponse(response.code(), body);
                    Timber.d("Upload to BeaconDB: Server response: %s", requestResult);
                    UploadResult uploadResult = BeaconDbUploadClient.mapRequestResultToUploadResult(requestResult);
                    uploadResultBundle.setResult(UploadTarget.BeaconDB, uploadResult);
                } catch (Exception e)
                {
                    Timber.e(e, "BeaconDB upload failed due to exception.");
                    uploadResultBundle.setResult(UploadTarget.BeaconDB, UploadResult.Failure);
                }
            } else
            {
                Timber.d("BeaconDB upload not enabled.");
                // When the user does not enable a target, we still need to mark the records as uploaded so they can be deleted
                uploadResultBundle.markSuccessful(UploadTarget.BeaconDB);
            }

            return uploadResultBundle;
        } catch (Exception e)
        {
            Timber.e(e, "Upload failed due to exception.");
            uploadResultBundle.markAllFailure();
            return uploadResultBundle;
        }
    }

    private Data getResultData(UploadResultBundle resultBundle)
    {
        UploadResult ocidResult = resultBundle.getResult(UploadTarget.OpenCelliD);
        UploadResult beaconDbResult = resultBundle.getResult(UploadTarget.BeaconDB);
        Context applicationContext = getApplicationContext();

        return new Data.Builder()
                .putString(OCID_RESULT, applicationContext.getString(UploadResult.getMessage(ocidResult)))
                .putString(BEACONDB_RESULT, applicationContext.getString(UploadResult.getMessage(beaconDbResult)))
                .putString(OCID_RESULT_MESSAGE, applicationContext.getString(ocidResult.getDescription()))
                .putString(BEACONDB_RESULT_MESSAGE, applicationContext.getString(beaconDbResult.getDescription()))
                .build();
    }

    public void reportProgress(int value, int max, String message)
    {
        if (isStopped())
        {
            return;
        }
        setProgressAsync(new Data.Builder()
                .putInt(PROGRESS, value)
                .putInt(PROGRESS_MAX, max)
                .putString(PROGRESS_STATUS_MESSAGE, message)
                .build());
        Notification notification = notificationHelper.updateNotificationProgress(value, max);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private <T> void markRecordsAsUploadedToOcid(List<T> records)
    {
        if (records == null || records.isEmpty()) return;

        database.runInTransaction(() -> {
            if (records.get(0) instanceof NrRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((NrRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markNrRecordsAsUploadedToOcid(recordIds);
            } else if (records.get(0) instanceof LteRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((LteRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markLteRecordsAsUploadedToOcid(recordIds);
            } else if (records.get(0) instanceof UmtsRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((UmtsRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markUmtsRecordsAsUploadedToOcid(recordIds);
            } else if (records.get(0) instanceof GsmRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((GsmRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markGsmRecordsAsUploadedToOcid(recordIds);
            } else if (records.get(0) instanceof CdmaRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((CdmaRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markCdmaRecordsAsUploadedToOcid(recordIds);
            }

            Timber.d("%d records marked as uploaded to OCID", records.size());
        });
    }

    private <T> void markRecordsAsUploadedToBeaconDb(List<T> records)
    {
        if (records == null || records.isEmpty()) return;

        database.runInTransaction(() -> {
            if (records.get(0) instanceof NrRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((NrRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markNrRecordsAsUploadedToBeaconDb(recordIds);
            } else if (records.get(0) instanceof LteRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((LteRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markLteRecordsAsUploadedToBeaconDb(recordIds);
            } else if (records.get(0) instanceof UmtsRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((UmtsRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markUmtsRecordsAsUploadedToBeaconDb(recordIds);
            } else if (records.get(0) instanceof GsmRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((GsmRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markGsmRecordsAsUploadedToBeaconDb(recordIds);
            } else if (records.get(0) instanceof CdmaRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((CdmaRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markCdmaRecordsAsUploadedToBeaconDb(recordIds);
            }

            Timber.d("%d records marked as uploaded to BeaconDB", records.size());
        });
    }

    private ForegroundInfo createForegroundInfo(Notification notification)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            return new ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else
        {
            return new ForegroundInfo(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Sums up the total number of records to be uploaded for all cellular protocols.
     */
    public static int getTotalCellularRecordsForUpload(SurveyRecordDao surveyRecordDao)
    {
        return surveyRecordDao.getNrRecordCountForUpload()
                + surveyRecordDao.getLteRecordCountForUpload()
                + surveyRecordDao.getUmtsRecordCountForUpload()
                + surveyRecordDao.getGsmRecordCountForUpload()
                + surveyRecordDao.getCdmaRecordCountForUpload();
    }
}
