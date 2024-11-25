package edu.stevens.cs522.chat.web.client;

import java.io.IOException;
import java.util.UUID;

import edu.stevens.cs522.chat.web.request.ChatServiceRequest;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/*
 * This interceptor adds app-specific headers to every message sent to HTTP server.
 */
public class HeaderInterceptor implements Interceptor {

    public static final String APPLICATION_ID = "X-App-Id";

    public static final String LATITUDE = "X-Latitude";

    public static final String LONGITUDE = "X-Longitude";

    public final static String USER_AGENT = "USER-AGENT";

    protected UUID appId;

    protected Double latitude;

    protected Double longitude;

    protected String userAgent;

    public HeaderInterceptor(ChatServiceRequest request, String userAgent) {
        this.appId = request.appId;
        this.latitude = request.latitude;
        this.longitude = request.longitude;
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder httpRequestBuilder = chain.request().newBuilder();
        httpRequestBuilder.addHeader(APPLICATION_ID, appId.toString());
        httpRequestBuilder.addHeader(LATITUDE, Double.toString(latitude));
        httpRequestBuilder.addHeader(LONGITUDE, Double.toString(longitude));
        httpRequestBuilder.addHeader(USER_AGENT, userAgent);
        return chain.proceed(httpRequestBuilder.build());
    }
}
