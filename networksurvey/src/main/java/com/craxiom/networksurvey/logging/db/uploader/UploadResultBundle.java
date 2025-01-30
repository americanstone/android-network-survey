package com.craxiom.networksurvey.logging.db.uploader;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UploadResultBundle
{
    private final Map<UploadTarget, UploadResult> results = new EnumMap<>(UploadTarget.class);

    public UploadResultBundle()
    {
        for (UploadTarget target : UploadTarget.values())
        {
            results.put(target, UploadResult.NotStarted);
        }
    }

    public UploadResult getResult(UploadTarget target)
    {
        return results.getOrDefault(target, UploadResult.NotStarted);
    }

    public void markSuccessful(UploadTarget target)
    {
        results.put(target, UploadResult.Success);
    }

    public void setResult(UploadTarget target, UploadResult result)
    {
        results.put(target, result);
    }

    public Map<UploadTarget, UploadResult> getResults()
    {
        return Collections.unmodifiableMap(results);
    }

    public void markAllFailure()
    {
        for (UploadTarget target : UploadTarget.values())
        {
            results.put(target, UploadResult.Failure);
        }
    }
}
