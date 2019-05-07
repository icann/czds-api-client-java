package org.icann.czds.sdk.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.icann.czds.sdk.model.ApplicationConstants;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * ZoneDownloadClient helps you to download all zone file for which a user is approved for or a particular zone file.
 */
public class ZoneDownloadClient extends CzdsClient{

    /*
     * Instantiate ZoneDownloadClient by providing ClientConfiguration
     */
    public ZoneDownloadClient(ClientConfiguration clientConfiguration) {
        super(clientConfiguration);
    }

    /**
     *  REST endpoint to download zone files
     *
     * @return The REST endpoint URL
     */
    public String getCzdsDownloadUrl() {
        return StringUtils.appendIfMissing(clientConfiguration.getCzdsBaseUrl(), "/") + "czds/downloads/";
    }

    /**
     * Directory where zone files will be saved
     *
     * @return The directory where the output files will be saved
     */
    public String getZonefileOutputDirectory() {
        return StringUtils.appendIfMissing(clientConfiguration.getWorkingDirectory(), "/") + "zonefiles";
    }

    /*
     * This helps you to download All Zone File for which user is approved for.
     * Throws AuthenticationException if not authorized.
     */
    public List<File> downloadApprovedZoneFiles() throws AuthenticationException, IOException{
        List<File> zoneFiles = new ArrayList<>();
        try {
            authenticateIfRequired();


            String linksURL = getCzdsDownloadUrl() + ApplicationConstants.CZDS_LINKS;

            HttpResponse response = makeGetRequest(linksURL);

            Set<String> listOfDownloadURLs = getDownloadURLs(response);

            for (String url : listOfDownloadURLs) {
                try {
                    File savedZoneFile = getZoneFile(url);
                    zoneFiles.add(savedZoneFile);
                } catch (Exception e) {
                    System.out.println(String.format("ERROR: failed to download zone file for zone - %s - with error %s", url, e.getMessage()));
                    continue;
                }
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
            String downloadURL = getCzdsDownloadUrl() + zone.trim() + ApplicationConstants.CZDS_ZONE;
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

    private Set<String> getDownloadURLs(HttpResponse response) throws IOException, AuthenticationException {
        if (response.getEntity().getContentLength() == 0) {
            return new HashSet<>();
        }
        return this.objectMapper.readValue(response.getEntity().getContent(), Set.class);
    }

    private File createFileLocally(InputStream inputStream, String fileName) throws IOException {
        System.out.println("Saving zone file to " + fileName);
        File tempDirectory = new File(getZonefileOutputDirectory());
        if (!tempDirectory.exists()) {
            tempDirectory.mkdir();
        }

        File file = new File(getZonefileOutputDirectory(), fileName);
        try {
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            inputStream.close();
            return file;
        } catch (IOException e) {
            throw new IOException("ERROR: Failed to save file to " + file.getAbsolutePath(), e);
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
}
