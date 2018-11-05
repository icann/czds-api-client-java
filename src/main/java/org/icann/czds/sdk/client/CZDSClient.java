package org.icann.czds.sdk.client;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.icann.czds.sdk.model.ApplicationConstants;
import org.icann.czds.sdk.model.AuthResult;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/*
CZDSClient helps you to download all zone file for which a user is approved for or a particular zone file.
*/
public class CZDSClient {

    protected ObjectMapper objectMapper;

    private ClientConfiguration clientConfiguration;

    private String token;

    /*
    Instantiate CZDSClient by providing ClientConfiguration
    */
    public CZDSClient(ClientConfiguration clientConfiguration) {
        this.objectMapper = new ObjectMapper();
        this.clientConfiguration = clientConfiguration;

    }

    /*
     This helps you to download All Zone File for which user is approved for.
     Throws AuthenticationException if not authorized.
    */
    public List<File> downloadApprovedZoneFiles() throws AuthenticationException, IOException{
        List<File> zoneFiles = new ArrayList<>();
        try {
            authenticateIfRequired();


            String linksURL = clientConfiguration.getCzdsDownloadUrl() + ApplicationConstants.CZDS_LINKS;

            HttpResponse response = makeGetRequest(linksURL);

            Set<String> listOfDownloadURLs = getDownloadURLs(response);

            for (String url : listOfDownloadURLs) {
                zoneFiles.add(getZoneFile(url));
            }

            return zoneFiles;
        } catch (AuthenticationException | IOException e) {
            throw e;
        }
    }

    /*
     This helps you to download Zone File of particular TLD.
     accepts name of tld as input.
     Throws AuthenticationException if not authorized to download that particular tld.
    */
    public File downloadZoneFile(String zone) throws  AuthenticationException, IOException{
        try {
            authenticateIfRequired();
            String downloadURL = clientConfiguration.getCzdsDownloadUrl() + zone.trim() + ApplicationConstants.CZDS_ZONE;
            return getZoneFile(downloadURL);
        } catch (AuthenticationException | IOException e) {
            throw e;
        }
    }


    private File getZoneFile(String downloadURL) throws IOException, AuthenticationException {
        System.out.println("Downloading zone file from " + downloadURL);
        HttpResponse response = makeGetRequest(downloadURL);
        return createFileLocally(response.getEntity().getContent(), getFileName(response));
    }

    private HttpResponse makeGetRequest(String url) throws IOException, AuthenticationException {
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

    private void authenticateIfRequired() throws AuthenticationException, IOException {

        if (token != null) {
            return;
        }

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(clientConfiguration.getAuthenticationUrl());

        Map<String, String> params = new HashMap<>();
        params.put("username", clientConfiguration.getUserName());
        params.put("password", clientConfiguration.getPassword());

        httppost.setEntity(buildRequestEntity(params));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (response.getStatusLine().getStatusCode() == 404) {
            throw new IOException(String.format("ERROR: Please check url %s", clientConfiguration.getAuthenticationUrl()));
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

    private Set<String> getDownloadURLs(HttpResponse response) throws IOException, AuthenticationException {
        if (response.getEntity().getContentLength() == 0) {
            return new HashSet<>();
        }
        return this.objectMapper.readValue(response.getEntity().getContent(), Set.class);
    }

    private File createFileLocally(InputStream inputStream, String fileName) throws IOException {
        System.out.println("Saving zone file " + fileName);
        File tempDirectory = new File(clientConfiguration.getZonefileOutputDirectory());
        if (!tempDirectory.exists()) {
            tempDirectory.mkdir();
        }

        File file = new File(clientConfiguration.getZonefileOutputDirectory(), fileName);
        try {
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            inputStream.close();
            return file;
        } catch (IOException e) {
            throw new IOException("ERROR: Failed to save file " + file.getAbsolutePath(), e);
        }
    }

    private String getFileName(HttpResponse response) throws AuthenticationException {
        Header[] headers = response.getHeaders("Content-disposition");
        String preFileName = "attachment;filename=";
        if (headers.length == 0) {
            throw new AuthenticationException("ERROR: Either you are not authorized to download zone file of tld or tld does not exist");
        }
        String fileName = headers[0].getValue().substring(headers[0].getValue().indexOf(preFileName) + preFileName.length());
        return fileName;
    }

    private HttpEntity buildRequestEntity(Object object) throws IOException {
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
