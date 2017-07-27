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
UserClient helps you to authenticate with global account, which provides you with a token.
    This token can be used to download zone files of TLD for which you are authorized for.
*/
public class UserClient {

    protected ObjectMapper objectMapper;

    private ClientConfiguration clientConfiguration;

    private String token;

    /*
    Instantiate UserClient by providing ClientConfiguration
    */
    public UserClient(ClientConfiguration clientConfiguration) {
        this.objectMapper = new ObjectMapper();
        this.clientConfiguration = clientConfiguration;
    }

    /*
     This helps you to download All Zone File for which user is approved for.
     accepts authentication token as input.
     Throws AuthenticationException if not authorized.
    */
    public List<File> downloadApprovedZoneFiles() throws IOException, AuthenticationException {

        authenticate();

        String linksURL = clientConfiguration.getCzdsDownloadURL() + ApplicationConstants.CZDS_LINKS;

        HttpResponse response = makeGetRequest(linksURL);

        Set<String> listOfDownloadURLs = getDownloadURLs(response);

        List<File> zoneFiles = new ArrayList<>();

        for (String url : listOfDownloadURLs) {
            zoneFiles.add(getZoneFile(url));
        }

        return zoneFiles;
    }

    /*
     This helps you to download Zone File of particular TLD.
     accepts name of tld and authentication token as input.
     Throws AuthenticationException if not authorized to download that particular tld.
    */
    public File downloadZoneFile(String zone) throws IOException, AuthenticationException {
        authenticate();
        String downloadURL = clientConfiguration.getCzdsDownloadURL() + zone.trim() + ApplicationConstants.CZDS_ZONE;
        return getZoneFile(downloadURL);
    }


    private File getZoneFile(String downloadURL) throws IOException, AuthenticationException {
        HttpResponse response = makeGetRequest(downloadURL);
        return createFileLocally(response.getEntity().getContent(), getFileName(response));
    }

    private HttpResponse makeGetRequest(String url) throws IOException, AuthenticationException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", "Bearer " + this.token);
        httpGet.addHeader("Content-Encoding", "gzip");
        HttpResponse response = httpclient.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 401) {
            throw new AuthenticationException("Either you are not authorized to download zone file of tld or tld does not exist");
        }

        if (response.getStatusLine().getStatusCode() == 503) {
            throw new AuthenticationException("Service Unavailable");
        }

        return response;
    }

    /*
     This helps you to authenticate with global account and provide you a token which can be used to download ZONE file.
     returns a token string if authentication is successful.
     Throws AuthenticationException if authentication failed.
    */
    private void authenticate() throws AuthenticationException, IOException {

        if (token != null) {
            return;
        }

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(clientConfiguration.getGlobalAccountURL());

        Map<String, String> params = new HashMap<>();
        params.put("username", clientConfiguration.getUserName());
        params.put("password", clientConfiguration.getPassword());

        httppost.setEntity(buildRequestEntity(params));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (response.getStatusLine().getStatusCode() == 401) {
            throw new AuthenticationException(String.format("Invalid username or password for user %s", clientConfiguration.getUserName()));
        }
        if (response.getStatusLine().getStatusCode() == 500) {
            throw new AuthenticationException("Internal Server Exception. Please try again later");
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
        File tempDirectory = new File(ApplicationConstants.TEMP_DIR_NAME);
        if (!tempDirectory.exists()) {
            tempDirectory.mkdir();
        }

        File file = new File(ApplicationConstants.TEMP_DIR_NAME + "/" + fileName);

        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        inputStream.close();
        return file;
    }

    private String getFileName(HttpResponse response) throws AuthenticationException {
        Header[] headers = response.getHeaders("Content-disposition");
        String preFileName = "attachment;filename=";
        if (headers.length == 0) {
            throw new AuthenticationException("Either you are not authorized to download zone file of tld or tld does not exist");
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
