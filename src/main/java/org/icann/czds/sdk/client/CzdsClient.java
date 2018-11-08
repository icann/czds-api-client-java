package org.icann.czds.sdk.client;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.icann.czds.sdk.model.AuthResult;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

public class CzdsClient {

    protected ObjectMapper objectMapper;

    protected ClientConfiguration clientConfiguration;

    protected String token;

    /*
     * Instantiate the client by providing ClientConfiguration
     */
    public CzdsClient(ClientConfiguration clientConfiguration) {
        this.objectMapper = new ObjectMapper();
        this.clientConfiguration = clientConfiguration;

    }

    protected String getAuthenticationUrl() {
        return StringUtils.appendIfMissing(clientConfiguration.getAuthenticationBaseUrl(), "/") + "api/authenticate/";
    }

    protected HttpResponse makeGetRequest(String url) throws IOException, AuthenticationException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", "Bearer " + this.token);
        httpGet.addHeader("Accept-Encoding", "gzip");
        HttpResponse response = httpclient.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 404) {
            throw new IOException(String.format("ERROR: Please check url %s", url));
        }

        if(response.getStatusLine().getStatusCode() == 403){
            throw new AuthenticationException(String.format("ERROR: %s is not authorized to download  %s", clientConfiguration.getUserName(), url));
        }

        if (response.getStatusLine().getStatusCode() == 401) {
            this.token = null;
            authenticateIfRequired();
            response = makeGetRequest(url);
        }

        if(response.getStatusLine().getStatusCode() == 428){

            String reason = response.getStatusLine().getReasonPhrase();

            if(reason.isEmpty()){
                reason = "ERROR: You need to first login to CZDS web interface and accept new Terms & Conditions";
            }

            throw new AuthenticationException(reason);
        }

        if (response.getStatusLine().getStatusCode() == 503) {
            throw new AuthenticationException("ERROR: Service Unavailable");
        }

        return response;
    }

    protected void authenticateIfRequired() throws AuthenticationException, IOException {

        if (token != null) {
            return;
        }

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(getAuthenticationUrl());

        Map<String, String> params = new HashMap<>();
        params.put("username", clientConfiguration.getUserName());
        params.put("password", clientConfiguration.getPassword());

        httppost.setEntity(buildRequestEntity(params));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (response.getStatusLine().getStatusCode() == 404) {
            throw new IOException(String.format("ERROR: Please check url %s", getAuthenticationUrl()));
        }

        if (response.getStatusLine().getStatusCode() == 401) {
            throw new AuthenticationException(String.format("ERROR: Invalid username or password for user %s. Please reset your password via Web", clientConfiguration.getUserName()));
        }
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new AuthenticationException("ERROR: Internal Server Exception. Please try again later");
        }

        this.token = getAuthToken(entity.getContent());

    }


    private String getAuthToken(InputStream inputStream) throws IOException, AuthenticationException {
        AuthResult authResult = this.objectMapper.readValue(inputStream, AuthResult.class);
        return authResult.getAccessToken();
    }

    protected HttpEntity buildRequestEntity(Object object) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = this.objectMapper.getFactory().createGenerator(writer);
        this.objectMapper.writeValue(generator, object);
        generator.close();
        writer.close();
        String string = writer.toString();
        StringEntity stringEntity = new StringEntity(string, "UTF-8");
        stringEntity.setContentType("application/json");
        return stringEntity;
    }
}
