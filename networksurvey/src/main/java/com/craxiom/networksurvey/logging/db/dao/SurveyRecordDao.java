package com.craxiom.networksurvey.logging.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.craxiom.networksurvey.logging.db.model.CdmaRecordEntity;
import com.craxiom.networksurvey.logging.db.model.GsmRecordEntity;
import com.craxiom.networksurvey.logging.db.model.LteRecordEntity;
import com.craxiom.networksurvey.logging.db.model.NrRecordEntity;
import com.craxiom.networksurvey.logging.db.model.UmtsRecordEntity;

import java.util.List;

@Dao
public interface SurveyRecordDao
{
    @Query("SELECT COUNT(*) FROM gsm_survey_records WHERE uploaded = 0")
    int getGsmRecordCountForUpload();

    @Query("SELECT * FROM gsm_survey_records WHERE uploaded = 0 LIMIT :limit")
    List<GsmRecordEntity> getGsmRecordsForUpload(int limit);

    @Query("UPDATE gsm_survey_records SET uploaded = 1 WHERE id IN (:recordIds)")
    void markGsmRecordsAsUploaded(List<Long> recordIds);

    @Query("SELECT COUNT(*) FROM cdma_survey_records WHERE uploaded = 0")
    int getCdmaRecordCountForUpload();

    @Query("SELECT * FROM cdma_survey_records WHERE uploaded = 0 LIMIT :limit")
    List<CdmaRecordEntity> getCdmaRecordsForUpload(int limit);

    @Query("UPDATE cdma_survey_records SET uploaded = 1 WHERE id IN (:recordIds)")
    void markCdmaRecordsAsUploaded(List<Long> recordIds);

    @Query("SELECT COUNT(*) FROM umts_survey_records WHERE uploaded = 0")
    int getUmtsRecordCountForUpload();

    @Query("SELECT * FROM umts_survey_records WHERE uploaded = 0 LIMIT :limit")
    List<UmtsRecordEntity> getUmtsRecordsForUpload(int limit);

    @Query("UPDATE umts_survey_records SET uploaded = 1 WHERE id IN (:recordIds)")
    void markUmtsRecordsAsUploaded(List<Long> recordIds);

    @Query("SELECT COUNT(*) FROM lte_survey_records WHERE uploaded = 0")
    int getLteRecordCountForUpload();

    @Query("SELECT * FROM lte_survey_records WHERE uploaded = 0 LIMIT :limit")
    List<LteRecordEntity> getLteRecordsForUpload(int limit);

    @Query("UPDATE lte_survey_records SET uploaded = 1 WHERE id IN (:recordIds)")
    void markLteRecordsAsUploaded(List<Long> recordIds);

    @Query("SELECT COUNT(*) FROM nr_survey_records WHERE uploaded = 0")
    int getNrRecordCountForUpload();

    @Query("SELECT * FROM nr_survey_records WHERE uploaded = 0 LIMIT :limit")
    List<NrRecordEntity> getNrRecordsForUpload(int limit);

    @Query("UPDATE nr_survey_records SET uploaded = 1 WHERE id IN (:recordIds)")
    void markNrRecordsAsUploaded(List<Long> recordIds);
}
