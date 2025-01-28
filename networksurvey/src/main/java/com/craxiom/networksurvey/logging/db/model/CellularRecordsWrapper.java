package com.craxiom.networksurvey.logging.db.model;

import java.util.List;

public record CellularRecordsWrapper(List<GsmRecordEntity> gsmRecords,
                                     List<UmtsRecordEntity> umtsRecords,
                                     List<LteRecordEntity> lteRecords,
                                     List<NrRecordEntity> nrRecords)
{
}
