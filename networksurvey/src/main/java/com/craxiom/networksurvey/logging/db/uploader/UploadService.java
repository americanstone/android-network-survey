package com.craxiom.networksurvey.logging.db.uploader;

import com.craxiom.networksurvey.logging.db.model.CellularRecordsWrapper;
import com.craxiom.networksurvey.logging.db.model.GeosubmitJsonConverterFactory;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/** @noinspection NewClassNamingConvention*/
public interface UploadService
{
    @POST
    Call<ResponseBody> uploadToOpenCellID(@Url String url, @Body CellularRecordsWrapper records);

    @POST
    Call<ResponseBody> uploadToBeaconDB(@Url String url, @Body CellularRecordsWrapper records);

    static UploadService getInstance()
    {
        return new Retrofit.Builder()
                .baseUrl("https://example.com/")  // Placeholder, replaced dynamically with @Url
                .addConverterFactory(GeosubmitJsonConverterFactory.create())  // Add custom converter
                .addConverterFactory(GsonConverterFactory.create())  // Use Gson for other JSON handling
                .build()
                .create(UploadService.class);
    }
}
