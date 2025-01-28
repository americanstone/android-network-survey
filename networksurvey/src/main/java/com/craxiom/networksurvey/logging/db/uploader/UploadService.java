package com.craxiom.networksurvey.logging.db.uploader;

import static com.google.common.net.HttpHeaders.USER_AGENT;

import androidx.annotation.NonNull;

import com.craxiom.networksurvey.logging.db.model.CellularRecordsWrapper;
import com.craxiom.networksurvey.logging.db.model.GeosubmitJsonConverterFactory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * @noinspection NewClassNamingConvention
 */
public interface UploadService
{
    @POST
    Call<ResponseBody> uploadToOpenCellID(@Url String url, @Body CellularRecordsWrapper records);

    @POST
    Call<ResponseBody> uploadToBeaconDB(@Url String url, @Body CellularRecordsWrapper records);

    static UploadService getInstance()
    {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor())
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://example.com/")  // Placeholder, replaced dynamically with @Url
                .client(client)
                .addConverterFactory(GeosubmitJsonConverterFactory.create())  // Add custom converter
                .addConverterFactory(GsonConverterFactory.create())  // Use Gson for other JSON handling
                .build()
                .create(UploadService.class);
    }

    class UserAgentInterceptor implements Interceptor
    {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException
        {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header(USER_AGENT, "NetworkSurvey")
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }
}
