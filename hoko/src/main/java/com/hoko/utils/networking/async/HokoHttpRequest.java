package com.hoko.utils.networking.async;

import android.content.ContentResolver;
import android.net.http.AndroidHttpClient;

import com.hoko.Hoko;
import com.hoko.model.exceptions.HokoException;
import com.hoko.utils.log.HokoLog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

/**
 * HokoHttpRequest is a savable model around HttpRequests.
 * It contains the path, an operation type, parameters in the form of json and the number of retries.
 */
public class HokoHttpRequest implements Serializable {

    // Constants
    private static final int HokoNetworkingTaskTimeout = 15000; // millis
    //private static final String HokoNetworkingTaskEndpoint = "http://192.168.1.10:3000";
    private static final String HokoNetworkingTaskEndpoint = "https://api.hokolinks.com";
    private static final String HokoNetworkingTaskVersion = "v1";
    private static final String HokoNetworkingTaskFormat = "json";

    // Properties
    private HokoNetworkOperationType mOperationType;
    private String mUrl;
    private String mToken;
    private String mParameters;
    private int mNumberOfRetries;

    // Constructors

    /**
     * Creates a request with a type, path, token and parameters.
     *
     * @param operationType The operation type (e.g. GET/PUT/POST).
     * @param url           The url (e.g. "https://api.hokolinks.com/v1/routes.json").
     * @param token         The application token.
     * @param parameters    The parameters in json string form.
     */
    public HokoHttpRequest(HokoNetworkOperationType operationType, String url, String token, String parameters) {
        mOperationType = operationType;
        mUrl = url.contains("http") ? url : HokoHttpRequest.getURLFromPath(url);
        mToken = token;
        mParameters = parameters;
        mNumberOfRetries = 0;
    }

    private static AbstractHttpEntity getCompressedEntity(byte data[], ContentResolver resolver)
            throws IOException {
        AbstractHttpEntity entity;

        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        OutputStream zipper = new GZIPOutputStream(arr);
        zipper.write(data);
        zipper.close();
        entity = new ByteArrayEntity(arr.toByteArray());
        entity.setContentEncoding("gzip");

        return entity;
    }

    /**
     * Generates the full URL, merging the endpoint, version, path and format.
     *
     * @return The full URL.
     */
    public static String getURLFromPath(String path) {
        return HokoNetworkingTaskEndpoint + "/" + HokoNetworkingTaskVersion + "/" + path + "." + HokoNetworkingTaskFormat;
    }

    // Property Gets
    public HokoNetworkOperationType getOperationType() {
        return mOperationType;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getToken() {
        return mToken;
    }

    public String getParameters() {
        return mParameters;
    }

    // Runnable

    public int getNumberOfRetries() {
        return mNumberOfRetries;
    }

    public void incrementNumberOfRetries() {
        mNumberOfRetries++;
    }

    // Networking

    /**
     * Transforms the HokoHttpRequest to a Runnable object so it can execute the request
     * on a background thread, usually inside a HokoAsyncTask object.
     *
     * @return The runnable wrapper for the request.
     */
    public Runnable toRunnable() {
        return toRunnable(null);
    }

    /**
     * Transforms the HokoHttpRequest to a Runnable object with a callback so it can execute the
     * request on a background thread, usually inside a HokoAsyncTask object. It will then call
     * the callback functions accordingly.
     *
     * @param httpCallback  The HokoHttpRequestCallback object.e
     * @return The runnable wrapper for the request.
     */
    public Runnable toRunnable(final HokoHttpRequestCallback httpCallback) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    switch (mOperationType) {
                        case GET:
                            performGET(httpCallback);
                            break;
                        case POST:
                            performPOST(httpCallback);
                            break;
                        case PUT:
                            performPUT(httpCallback);
                            break;
                    }
                } catch (IOException e) {
                    HokoLog.e(e);
                    if (httpCallback != null)
                        httpCallback.onFailure(e);
                }
            }
        };
    }

    /**
     * Creates an HttpParams object with the proper timeouts on the connection and socket.
     *
     * @return HttpParams with timeouts.
     */
    private HttpParams getHttpParams() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, HokoNetworkingTaskTimeout);
        HttpConnectionParams.setSoTimeout(httpParameters, HokoNetworkingTaskTimeout);

        return httpParameters;
    }

    /**
     * HttpsClient constructor to allow HTTPS through CloudFlare.
     *
     * @param httpParams The params for the HttpsClient
     * @return The HttpClient.
     */
    private HttpClient getHttpsClient(HttpParams httpParams) {
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        DefaultHttpClient client = httpParams != null ? new DefaultHttpClient(httpParams) : new DefaultHttpClient();

        if (HokoNetworkingTaskEndpoint.startsWith("https")) {

            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", socketFactory, 443));
            SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);

            DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            return httpClient;
        } else {
            return client;
        }
    }

    /**
     * Performs an HttpGet to the specified url, will handle the response with the callback.
     *
     * @param httpCallback  The HokoHttpRequestCallback object.
     * @throws IOException  Throws an IOException in case of a network problem.
     */
    private void performGET(HokoHttpRequestCallback httpCallback) throws IOException {
        HttpClient httpClient = getHttpsClient(null);
        HttpGet get = new HttpGet(getUrl());
        get.setHeader("Accept", "application/json");
        get.setHeader("Accept-Encoding", "gzip");
        if (getToken() != null) {
            get.setHeader("Authorization", "Token " + getToken());
            get.setHeader("Hoko-SDK-Version", Hoko.HokoVersion);
        }
        HttpResponse httpResponse = httpClient.execute(get);
        handleHttpResponse(httpResponse, httpCallback);

    }

    /**
     * Performs an HttpPut to the specified url, will handle the response with the callback.
     *
     * @param httpCallback  The HokoHttpRequestCallback object.
     * @throws IOException  Throws an IOException in case of a network problem.
     */
    private void performPUT(HokoHttpRequestCallback httpCallback) throws IOException {
        HttpClient httpClient = getHttpsClient(getHttpParams());
        HttpPut put = new HttpPut(getUrl());

        put.setHeader("Accept-Encoding", "gzip");
        put.setHeader("Accept", "application/json");
        put.setHeader("Content-Type", "gzip/json");
        if (getToken() != null) {
            put.setHeader("Authorization", "Token " + getToken());
            put.setHeader("Hoko-SDK-Version", Hoko.HokoVersion);
        }

        put.setEntity(getCompressedEntity(getParameters().getBytes(), null));

        // HokoLog.d("PUTing " + getParameters());
        HttpResponse httpResponse = httpClient.execute(put);
        handleHttpResponse(httpResponse, httpCallback);

    }

    /**
     * Performs an HttpPost to the specified url, will handle the response with the callback.
     *
     * @param httpCallback  The HokoHttpRequestCallback object.
     * @throws IOException  Throws an IOException in case of a network problem.
     */
    private void performPOST(HokoHttpRequestCallback httpCallback) throws IOException {
        HttpClient httpClient = getHttpsClient(getHttpParams());
        HttpPost post = new HttpPost(getUrl());

        post.setHeader("Accept-Encoding", "gzip");
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "gzip/json");
        if (getToken() != null) {
            post.setHeader("Authorization", "Token " + getToken());
            post.setHeader("Hoko-SDK-Version", Hoko.HokoVersion);
        }

        post.setEntity(getCompressedEntity(getParameters().getBytes(), null));

        //HokoLog.d("POSTing " + getParameters());
        HttpResponse httpResponse = httpClient.execute(post);
        handleHttpResponse(httpResponse, httpCallback);

    }

    /**
     * The HttpResponse handler, tries to parse the response into json, checks the status code and
     * throws exceptions accordingly. Will also use the callback to notify of the response given.
     *
     * @param httpResponse The HttpResponse object coming from a GET/POST/PUT.
     * @param httpCallback The HokoHttpRequestCallback object.
     * @throws IOException Throws an IOException in case of a network problem.
     */
    private void handleHttpResponse(HttpResponse httpResponse, HokoHttpRequestCallback httpCallback) throws IOException {
        InputStream gzipedInputStream = AndroidHttpClient.getUngzippedContent(httpResponse.getEntity());
        String response = convertStreamToString(gzipedInputStream);
        JSONObject jsonResponse;
        try {
            jsonResponse = new JSONObject(response);
        } catch (JSONException e) {
            try {
                jsonResponse = new JSONArray(response).getJSONObject(0);
            } catch (JSONException e2) {
                jsonResponse = new JSONObject();
            }
        }
        if (httpResponse.getStatusLine().getStatusCode() >= 300) {
            HokoException exception = HokoException.serverException(jsonResponse);
            HokoLog.e(exception);
            if (httpCallback != null) {
                httpCallback.onFailure(exception);
            }
        } else {
            if (httpCallback != null)
                httpCallback.onSuccess(jsonResponse);
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    // Type Enum

    /**
     * The possible network operation types: GET, POST and PUT.
     * Serializable for saving HokoHttpRequests to file.
     */
    public enum HokoNetworkOperationType implements Serializable {
        GET, POST, PUT
    }

}
