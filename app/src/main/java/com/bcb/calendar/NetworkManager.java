package com.bcb.calendar;

import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 3/24/17.
 */

public class NetworkManager {
    public enum Endpoint {
        USER_INFO("https://prd-mobile.temple.edu/banner-mobileserver/api/2.0/security/getUserInfo"),
        GRADES("https://prd-mobile.temple.edu/banner-mobileserver/api/2.0/grades"),
        COURSES("https://prd-mobile.temple.edu/banner-mobileserver/api/2.0/courses/overview"),
        COURSE_ROSTER("https://prd-mobile.temple.edu/banner-mobileserver/api/2.0/courses/roster"),
        NEWS("https://prd-mobile.temple.edu/banner-mobileserver/rest/1.2/feed"),
        COURSE_SEARCH("https://prd-mobile.temple.edu/CourseSearch/searchCatalog.jsp");

        private final String url;

        Endpoint(final String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return url;
        }
    }

    public static NetworkManager SHARED = new NetworkManager();

    public void requestFromEndpoint(Endpoint endpoint,
                                    @Nullable String tuID,
                                    @Nullable Map<String, String> parameters,
                                    @Nullable Credential credential,
                                    JSONObjectRequestListener jsonObjectRequestListener) {

        String url = endpoint.toString();

        // Add TU ID to path if present
        if (tuID != null) {
            url += ("/" + tuID);
        }

        Log.d("URL: ", url);

        // Base request
        ANRequest.GetRequestBuilder requestBuilder = AndroidNetworking.get(url);

        // Add basic auth header if credentials are present
        if (credential != null) {
            requestBuilder.addHeaders(NetworkManager.generateBasicAuthHeader(credential));
        }

        // Add parameters if present
        if (parameters != null) {
            requestBuilder.addPathParameter(parameters);
        }

        // Make the request
        requestBuilder.build().getAsJSONObject(jsonObjectRequestListener);
    }

    public void requestFromEndpoint(Endpoint endpoint,
                                @Nullable String tuID,
                                @Nullable Map<String, String> parameters,
                                @Nullable Credential credential,
                                JSONArrayRequestListener jsonArrayRequestListener) {

        String url = endpoint.toString();

        // Add TU ID to path if present
        if (tuID != null) {
            url += ("/" + tuID);
        }

        Log.d("URL: ", url);

        // Base request
        ANRequest.GetRequestBuilder requestBuilder = AndroidNetworking.get(url);

        // Add basic auth header if credentials are present
        if (credential != null) {
            requestBuilder.addHeaders(NetworkManager.generateBasicAuthHeader(credential));
        }

        // Add parameters if present
        if (parameters != null) {
            requestBuilder.addPathParameter(parameters);
        }

        // Make the request
        requestBuilder.build().getAsJSONArray(jsonArrayRequestListener);
    }

    public void requestFromEndpointWithAuthenticator(Endpoint endpoint,
                                    @Nullable String tuID,
                                    @Nullable Map<String, String> parameters,
                                    @Nullable Credential credential,
                                    JSONObjectRequestListener jsonObjectRequestListener) {

        String url = endpoint.toString();

        // Add TU ID to path if present
        if (tuID != null) {
            url += ("/" + tuID);
        }

        Log.d("URL: ", url);

        // Base request
        ANRequest.GetRequestBuilder requestBuilder = AndroidNetworking.get(url);

        // Add basic auth header if credentials are present
        if (credential != null) {
           // UserAuthenticator authenticator = new UserAuthenticator(credential);
           // Authenticator.setDefault(authenticator);

           requestBuilder.addHeaders(NetworkManager.generateBasicAuthHeader(credential));
        }

        // Add parameters if present
        if (parameters != null) {
            requestBuilder.addPathParameter(parameters);
        }

        // Make the request
        requestBuilder.build().getAsJSONObject(jsonObjectRequestListener);
    }

    static Map<String, String> generateBasicAuthHeader(Credential credential) {
        Map<String, String> map = new HashMap<>(1);
        map.put("Authorization", "Basic "
                + Base64.encodeToString((credential.username + ":" + credential.password)
                .getBytes(), Base64.NO_WRAP));

        Log.d("Authorization: ", map.get("Authorization").toString());
        return map;
    }

    private class UserAuthenticator extends Authenticator{
        private Credential credential;

        public UserAuthenticator(Credential credential){
            super();
            this.credential = credential;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            Log.d("Authenticating: ", credential.username + " " + credential.password);

            // Get information about the request

            String prompt = getRequestingPrompt();

            String hostname = getRequestingHost();

            InetAddress ipaddr = getRequestingSite();

            int port = getRequestingPort();

            return new PasswordAuthentication(credential.username, credential.password.toCharArray());
        }
    }

}
