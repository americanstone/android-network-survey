package com.craxiom.networksurvey.logging.db.model;

import java.util.Collections;
import java.util.List;

public record CellularRecordsWrapper(List<GsmRecordEntity> gsmRecords,
                                     List<UmtsRecordEntity> umtsRecords,
                                     List<LteRecordEntity> lteRecords,
                                     List<NrRecordEntity> nrRecords)
{
    /**
     * @noinspection unchecked
     */
    public static CellularRecordsWrapper createCellularRecordsWrapper(List<?> records)
    {
        if (records == null || records.isEmpty())
        {
            return new CellularRecordsWrapper(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        if (records.get(0) instanceof GsmRecordEntity)
        {
            return new CellularRecordsWrapper(
                    (List<GsmRecordEntity>) records,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        } else if (records.get(0) instanceof CdmaRecordEntity)
        {
            return new CellularRecordsWrapper(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        } else if (records.get(0) instanceof UmtsRecordEntity)
        {
            return new CellularRecordsWrapper(
                    Collections.emptyList(),
                    (List<UmtsRecordEntity>) records,
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        } else if (records.get(0) instanceof LteRecordEntity)
        {
            return new CellularRecordsWrapper(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    (List<LteRecordEntity>) records,
                    Collections.emptyList()
            );
        } else if (records.get(0) instanceof NrRecordEntity)
        {
            return new CellularRecordsWrapper(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    (List<NrRecordEntity>) records
            );
        }

        throw new IllegalArgumentException("Unsupported record type: " + records.get(0).getClass().getName());
    }
}
