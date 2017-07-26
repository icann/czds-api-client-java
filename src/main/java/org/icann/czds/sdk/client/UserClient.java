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
import org.icann.czds.sdk.model.AuthResult;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/*
UserClient helps you to authenticate with global account, which provides you with a token.
    This token can be used to download zone files of TLD for which you are authorized for.
*/
public class UserClient {

    protected ObjectMapper objectMapper;

    private static final String TEMP_DIRECTORY_NAME = "temp";

    private ClientConfiguration clientConfiguration;

    /*
    Instantiate UserClient by providing Global Account URL and CZDS download URL
    */
    public UserClient(ClientConfiguration clientConfiguration) {
        this.objectMapper = new ObjectMapper();
        this.clientConfiguration = clientConfiguration;
    }

    /*
     This helps you to authenticate with global account and provide you a token which can be used to download ZONE file.
     returns a token string if authentication is successful.
     Throws AuthenticationException if authentication failed.
    */
    public String authenticate() throws AuthenticationException, IOException {
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

        return getAuthToken(entity.getContent());

    }

    /*
     This helps you to download All Zone File for which user is approved for.
     accepts authentication token as input.
     Throws AuthenticationException if not authorized.
    */
    public List<File> downloadApprovedZoneFiles(String token) throws IOException, AuthenticationException {

        String czdsDownloadURL = clientConfiguration.getCzdsDownloadURL();
        if (!czdsDownloadURL.endsWith("/")) {
            czdsDownloadURL = czdsDownloadURL + "/";
        }

        String linksURL = czdsDownloadURL + "links";
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(linksURL);
        httpGet.addHeader("Authorization", "Bearer " + token);
        HttpResponse response = httpclient.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 401) {
            throw new AuthenticationException("Either you are not authorized to download zone file of tld or tld does not exist");
        }

        if (response.getStatusLine().getStatusCode() == 503) {
            throw new AuthenticationException("Service Unavailable");
        }

        Set<String> listOfDownloadURLs = getDownloadURLs(response.getEntity().getContent());

        List<File> zoneFiles = new ArrayList<>();

        for (String url : listOfDownloadURLs) {
            zoneFiles.add(getZoneFile(url, token));
        }

        return zoneFiles;
    }


    /*
     This helps you to download Zone File of particular TLD.
     accepts name of tld and authentication token as input.
     Throws AuthenticationException if not authorized to download that particular tld.
    */
    public File downloadZoneFile(String zone, String token) throws IOException, AuthenticationException {
        String czdsDownloadURL = clientConfiguration.getCzdsDownloadURL();
        if (!czdsDownloadURL.endsWith("/")) {
            czdsDownloadURL = czdsDownloadURL + "/";
        }
        String downloadURL = czdsDownloadURL + zone.trim() + ".zone";
        return getZoneFile(downloadURL, token);
    }


    private File getZoneFile(String downloadURL, String token) throws IOException, AuthenticationException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(downloadURL);
        httpGet.addHeader("Authorization", "Bearer " + token);
        HttpResponse response = httpclient.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 401) {
            throw new AuthenticationException("Either you are not authorized to download zone file of tld or tld does not exist");
        }

        if (response.getStatusLine().getStatusCode() == 503) {
            throw new AuthenticationException("Service Unavailable");
        }
        return createFileLocally(response.getEntity().getContent(), getFileName(response));
    }


    private String getAuthToken(InputStream inputStream) throws IOException, AuthenticationException {

        if (inputStream != null) {
            String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            AuthResult authResult = getAuthResult(result);
            inputStream.close();
            return authResult.getAccessToken();
        }

        throw new IOException("Internal Server Error. Please try after some time");
    }

    private Set<String> getDownloadURLs(InputStream inputStream) throws IOException, AuthenticationException {

        if (inputStream != null) {
            String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            inputStream.close();
            return getListOfURLs(result);
        }

        throw new IOException("Internal Server Error. Please try after some time");
    }

    private File createFileLocally(InputStream inputStream, String fileName) throws IOException {
        File tempDirectory = new File(TEMP_DIRECTORY_NAME);
        if (!tempDirectory.exists()) {
            tempDirectory.mkdir();
        }

        File file = new File(TEMP_DIRECTORY_NAME + "/" + fileName);

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

    private AuthResult getAuthResult(String result) throws IOException {
        if (result == null || result.length() == 0) {
            throw new IOException("Internal Server Error. Please try after some time");
        }
        return objectMapper.readValue(result, AuthResult.class);
    }

    private Set<String> getListOfURLs(String result) throws IOException {
        if (result == null || result.length() == 0) {
            return new HashSet<>();
        }
        return objectMapper.readValue(result, Set.class);
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
