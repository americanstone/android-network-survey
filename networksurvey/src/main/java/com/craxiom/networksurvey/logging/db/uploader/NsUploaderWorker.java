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

import com.craxiom.networksurvey.constants.NetworkSurveyConstants;
import com.craxiom.networksurvey.logging.db.SurveyDatabase;
import com.craxiom.networksurvey.logging.db.dao.SurveyRecordDao;
import com.craxiom.networksurvey.logging.db.model.CdmaRecordEntity;
import com.craxiom.networksurvey.logging.db.model.CellularRecordsWrapper;
import com.craxiom.networksurvey.logging.db.model.GsmRecordEntity;
import com.craxiom.networksurvey.logging.db.model.LteRecordEntity;
import com.craxiom.networksurvey.logging.db.model.NrRecordEntity;
import com.craxiom.networksurvey.logging.db.model.UmtsRecordEntity;

import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
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

    public static final int NOTIFICATION_ID = 102;
    private static final int LOCATIONS_PER_PART = 100; // Batch size for uploads

    private final NotificationManager notificationManager;
    private final UploaderNotificationHelper notificationHelper;
    private final SurveyDatabase database;

    private boolean isOpenCellIdUploadEnabled;
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
            ForegroundInfo foregroundInfo = createForegroundInfo(notification);
            setForegroundAsync(foregroundInfo);

            Timber.d("UploaderWorker: Starting upload process...");
            // TODO Prevent a second trigger of this doWork somehow (Tower Collector uses an application static variable)

            // Read work input parameters
            isOpenCellIdUploadEnabled = getInputData().getBoolean(NetworkSurveyConstants.PROPERTY_UPLOAD_TO_OPENCELLID, false);
            isBeaconDBUploadEnabled = getInputData().getBoolean(NetworkSurveyConstants.PROPERTY_UPLOAD_TO_BEACONDB, false);
            isRetryEnabled = getInputData().getBoolean(NetworkSurveyConstants.PROPERTY_UPLOAD_RETRY_ENABLED, false);
            // TODO Get the OpenCelliD API key from the input data, or a shared key from the app

            int totalRecords = getTotalRecordsForUpload(database.surveyRecordDao());
            if (totalRecords == 0)
            {
                Timber.d("UploaderWorker: No records to upload.");
                return Result.success();
            }

            int partsCount = (int) Math.ceil((double) totalRecords / LOCATIONS_PER_PART);
            for (int i = 0; i < partsCount; i++)
            {
                boolean success = processUploadBatch(LOCATIONS_PER_PART);
                if (!success)
                {
                    if (isRetryEnabled)
                    {
                        Timber.d("UploaderWorker: Upload failed, retry enabled.");
                        return Result.retry();
                    } else
                    {
                        Timber.e("UploaderWorker: Upload failed with no retry.");
                        return Result.failure();
                    }
                }

                // Progress update
                setProgressAsync(new Data.Builder()
                        .putInt("PROGRESS", (i + 1) * 100 / partsCount)
                        .build());
            }

            Timber.d("UploaderWorker: Upload process completed.");
            return Result.success();
        } catch (Exception e)
        {
            Timber.e(e, "UploaderWorker: Upload process failed.");
            return Result.failure();
        }
    }

    private boolean processUploadBatch(int batchSize)
    {
        List<NrRecordEntity> nrRecords = database.surveyRecordDao().getNrRecordsForUpload(batchSize);
        List<LteRecordEntity> lteRecords = database.surveyRecordDao().getLteRecordsForUpload(batchSize);
        List<UmtsRecordEntity> umtsRecords = database.surveyRecordDao().getUmtsRecordsForUpload(batchSize);
        List<GsmRecordEntity> gsmRecords = database.surveyRecordDao().getGsmRecordsForUpload(batchSize);
        List<CdmaRecordEntity> cdmaRecords = database.surveyRecordDao().getCdmaRecordsForUpload(batchSize);

        boolean success = true;

        if (!nrRecords.isEmpty())
        {
            success &= processUpload(nrRecords);
        }
        if (!lteRecords.isEmpty())
        {
            success &= processUpload(lteRecords);
        }
        if (!umtsRecords.isEmpty())
        {
            success &= processUpload(umtsRecords);
        }
        if (!gsmRecords.isEmpty())
        {
            success &= processUpload(gsmRecords);
        }
        if (!cdmaRecords.isEmpty())
        {
            success &= processUpload(cdmaRecords);
        }

        return success;
    }

    private <T> boolean processUpload(List<T> records)
    {
        UploadResult result = uploadRecords(records);
        if (result == UploadResult.Success)
        {
            markRecordsAsUploaded(records);
            return true;
        }
        return false;
    }

    private <T> UploadResult uploadRecords(List<T> records)
    {
        try
        {
            UploadService uploadService = UploadService.getInstance();

            final CellularRecordsWrapper recordsWrapper = CellularRecordsWrapper.createCellularRecordsWrapper(records);

            if (isOpenCellIdUploadEnabled)
            {
                final UploadResult[] result = {UploadResult.NotStarted};
                uploadService.uploadToOpenCellID(UploadConstants.OPENCELLID_URL, recordsWrapper)
                        .enqueue(new UploadRecordsCallback(result));
                // FIXME Return the result
            }

            if (isBeaconDBUploadEnabled)
            {
                // TODO Update this call to be like the other one
                Response<ResponseBody> response = uploadService.uploadToBeaconDB(UploadConstants.BEACONDB_URL, recordsWrapper).execute();
                if (!response.isSuccessful())
                {
                    Timber.e("UploaderWorker: BeaconDB upload failed with response: %s", response.message());
                    return UploadResult.Failure;
                }
            }

            uploadService.uploadToOpenCellID("http://172.22.51.71:8080/v2/geosubmit", recordsWrapper)
                    .enqueue(new retrofit2.Callback<>()
                    {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response)
                        {
                            if (response.isSuccessful())
                            {
                                Timber.i("Upload successful!");
                            } else
                            {
                                Timber.w("Upload failed: %s", response.errorBody());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t)
                        {
                            Timber.e(t, "UploaderWorker: OpenCellID upload failed due to exception.");
                        }
                    });

            return UploadResult.Success;
        } catch (Exception e)
        {
            Timber.e(e, "UploaderWorker: Upload failed due to exception.");
            return UploadResult.Failure;
        }
    }

    private <T> void markRecordsAsUploaded(List<T> records)
    {
        if (records == null || records.isEmpty()) return;

        database.runInTransaction(() -> {
            if (records.get(0) instanceof NrRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((NrRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markNrRecordsAsUploaded(recordIds);
            } else if (records.get(0) instanceof LteRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((LteRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markLteRecordsAsUploaded(recordIds);
            } else if (records.get(0) instanceof UmtsRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((UmtsRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markUmtsRecordsAsUploaded(recordIds);
            } else if (records.get(0) instanceof GsmRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((GsmRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markGsmRecordsAsUploaded(recordIds);
            } else if (records.get(0) instanceof CdmaRecordEntity)
            {
                List<Long> recordIds = records.stream().map(record -> ((CdmaRecordEntity) record).id).collect(Collectors.toList());
                database.surveyRecordDao().markCdmaRecordsAsUploaded(recordIds);
            }

            Timber.d("UploaderWorker: %d records marked as uploaded.", records.size());
        });
    }

    @Override
    public void onStopped()
    {
        Timber.d("UploaderWorker: Upload cancelled.");
        notificationManager.cancel(NOTIFICATION_ID);
        super.onStopped();
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
    public int getTotalRecordsForUpload(SurveyRecordDao surveyRecordDao)
    {
        return surveyRecordDao.getNrRecordCountForUpload()
                + surveyRecordDao.getLteRecordCountForUpload()
                + surveyRecordDao.getUmtsRecordCountForUpload()
                + surveyRecordDao.getGsmRecordCountForUpload()
                + surveyRecordDao.getCdmaRecordCountForUpload();
    }

    private static class UploadRecordsCallback implements retrofit2.Callback<ResponseBody>
    {
        private final UploadResult[] result;

        public UploadRecordsCallback(UploadResult[] result)
        {
            this.result = result;
        }

        @Override
        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response)
        {
            if (response.isSuccessful())
            {
                Timber.i("Upload successful!");
                result[0] = UploadResult.Success;
            } else
            {
                Timber.w("Upload failed: %s", response.errorBody());
                result[0] = UploadResult.Failure;
            }
        }

        @Override
        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t)
        {
            Timber.e(t, "UploaderWorker: OpenCellID upload failed due to exception.");
            result[0] = UploadResult.ServerError;
        }
    }
}

