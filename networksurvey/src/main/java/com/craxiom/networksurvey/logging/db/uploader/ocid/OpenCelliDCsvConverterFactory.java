package com.craxiom.networksurvey.logging.db.uploader.ocid;

import androidx.annotation.NonNull;

import com.craxiom.networksurvey.logging.db.model.CellularRecordsWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class OpenCelliDCsvConverterFactory extends Converter.Factory
{
    private static final MediaType MEDIA_TYPE_CSV = MediaType.parse("text/csv; charset=UTF-8");

    public static OpenCelliDCsvConverterFactory create()
    {
        return new OpenCelliDCsvConverterFactory();
    }

    @Override
    public Converter<CellularRecordsWrapper, RequestBody> requestBodyConverter(@NonNull Type type, @NonNull Annotation[] parameterAnnotations,
                                                                               @NonNull Annotation[] methodAnnotations, @NonNull Retrofit retrofit)
    {
        return value -> {
            String csvContent = OpenCelliDCsvFormatter.formatRecords(value);
            return RequestBody.create(csvContent, MEDIA_TYPE_CSV);
        };
    }
}
