package org.icann.czds.sdk.client;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
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

    protected CloseableHttpResponse makeHeadRequest(String url) throws IOException, AuthenticationException {

        HttpHead httpHead = new HttpHead(url);
        httpHead.addHeader("Authorization", "Bearer " + this.token);
        httpHead.addHeader("Accept-Encoding", "gzip");

        try(CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = httpclient.execute(httpHead)) {

            if (response.getCode() == 404) {
                System.out.println(String.format("ERROR: Please check url %s", url));
            }

            if (response.getCode() == 403) {
                System.out.println(String.format("ERROR: %s is not authorized to download  %s", clientConfiguration.getUserName(), url));
            }

            if (response.getCode() == 401) {
                this.token = null;
                authenticateIfRequired();
                makeHeadRequest(url);
            }

            if (response.getCode() == 428) {

                String reason = response.getReasonPhrase();

                if (reason.isEmpty()) {
                    reason = "ERROR: You need to first login to CZDS web interface and accept new Terms & Conditions";
                }

                throw new AuthenticationException(reason);
            }

            if (response.getCode() == 503) {
                System.out.println("response = " + response + " ERROR: Service Unavailable");
            }

            return response;
        }
    }

    protected CloseableHttpResponse makeGetRequest(String url) throws IOException, AuthenticationException {

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", "Bearer " + this.token);
        httpGet.addHeader("Accept-Encoding", "gzip");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httpGet);

        if (response.getCode() == 404) {
            throw new IOException(String.format("ERROR: Please check url %s", url));
        }

        if(response.getCode() == 403){
            throw new AuthenticationException(String.format("ERROR: %s is not authorized to download  %s", clientConfiguration.getUserName(), url));
        }

        if (response.getCode() == 401) {
            this.token = null;
            authenticateIfRequired();
            response = makeGetRequest(url);
        }

        if(response.getCode() == 428){

            String reason = response.getReasonPhrase();

            if(reason.isEmpty()){
                reason = "ERROR: You need to first login to CZDS web interface and accept new Terms & Conditions";
            }

            throw new AuthenticationException(reason);
        }

        if (response.getCode() == 503) {
            throw new AuthenticationException("ERROR: Service Unavailable");
        }

        return response;
    }

    protected void authenticateIfRequired() throws AuthenticationException, IOException {

        if (token != null) {
            return;
        }

        HttpPost httpPost = new HttpPost(getAuthenticationUrl());
        Map<String, String> params = new HashMap<>();
        params.put("username", clientConfiguration.getUserName());
        params.put("password", clientConfiguration.getPassword());
        httpPost.setEntity(buildRequestEntity(params));

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = (CloseableHttpResponse)httpclient.execute(httpPost)) {

            if (response.getCode() == 404) {
                throw new IOException(String.format("ERROR: Please check url %s", getAuthenticationUrl()));
            }

            if (response.getCode() == 401) {
                throw new AuthenticationException(
                        String.format("ERROR: Invalid username or password for user %s. Please reset your password via Web",
                                clientConfiguration.getUserName()));
            }
            if (response.getCode() == 500) {
                throw new AuthenticationException("ERROR: Internal Server Exception. Please try again later");
            }

            this.token = getAuthToken(response.getEntity().getContent());
            System.out.println("Got access_token: " + this.token);
        }
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
        StringEntity stringEntity = new StringEntity(string, ContentType.APPLICATION_JSON, false);
        return stringEntity;
    }
}
