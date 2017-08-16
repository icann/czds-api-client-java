package org.icann.czds.sdk.model;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfiguration {

    private static String userName;
    private static String password;
    private static String globalAccountURL;
    private static String czdsDownloadURL;
    private static String fileStoreageLocation;

    public static ClientConfiguration createClientConfiguration(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        userName = properties.getProperty("global.account.username");
        password = properties.getProperty("global.account.password");
        globalAccountURL = properties.getProperty("global.account.url");
        czdsDownloadURL = properties.getProperty("czds.download.url");
        fileStoreageLocation = properties.getProperty("file.storage.location");
        if (fileStoreageLocation == null || fileStoreageLocation.trim().length() == 0) {
            fileStoreageLocation = "temp";
        }

        return new ClientConfiguration(userName, password, globalAccountURL, czdsDownloadURL, fileStoreageLocation);
    }


    /*If initiated using this constructor, location of file download is automatically set to temp*/
    public ClientConfiguration(String userName, String password, String globalAccountURL, String czdsDownloadURL) throws IOException {
        checkNullValue(userName);
        checkNullValue(password);
        checkNullValue(globalAccountURL);
        checkNullValue(czdsDownloadURL);
        this.userName = userName.trim();
        this.password = password.trim();
        this.globalAccountURL = globalAccountURL.trim();
        this.czdsDownloadURL = formatURL(czdsDownloadURL.trim());
        this.fileStoreageLocation = "temp";
    }

    /*If initiated using this constructor, you can specify location of file download*/
    public ClientConfiguration(String userName, String password, String globalAccountURL, String czdsDownloadURL, String fileStoreageLocation) throws IOException {
        this(userName, password, globalAccountURL, czdsDownloadURL);
        checkNullValue(fileStoreageLocation);
        this.fileStoreageLocation = fileStoreageLocation;

    }


    private String formatURL(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        return url;
    }

    private static void checkNullValue(String value) throws IOException {
        if (value == null || value.trim().length() == 0) {
            throw new IOException("Value Cannot be null");
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getGlobalAccountURL() {
        return globalAccountURL;
    }

    public String getCzdsDownloadURL() {
        return czdsDownloadURL;
    }

    public String getFileStoreageLocation() {
        return fileStoreageLocation;
    }
}
