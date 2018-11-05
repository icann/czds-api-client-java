package org.icann.czds.sdk.model;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfiguration {

    private static ClientConfiguration configuration = null;

    private static String username;
    private static String password;
    private static String authenticationUrl;
    private static String czdsDownloadUrl;
    private static String zonefileOutputDirectory;


    public static ClientConfiguration getInstance() throws IOException{
        if(configuration == null) {
            loadDefaultClientConfiguration();
        }

        return configuration;
    }

    /**
     * Loads the default configuration from application.properties file.
     */
    private static ClientConfiguration loadDefaultClientConfiguration() throws IOException {
        InputStream inputStream = ClientConfiguration.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        properties.load(inputStream);

        String userName = properties.getProperty("icann.account.username");
        String password = properties.getProperty("icann.account.password");

        // Must specify authentication.base.url and czds.base.url
        String authenBaseUrl = properties.getProperty("authentication.base.url");
        String czdsBaseUrl = properties.getProperty("czds.base.url");
        authenBaseUrl = checkNullValue("authentication.base.url", authenBaseUrl);
        czdsBaseUrl = checkNullValue("czds.base.url", czdsBaseUrl);

        // Default to current dir if zonefile.output.directory is not specified.
        String outputDir = properties.getProperty("zonefile.output.directory");
        if(StringUtils.isBlank(outputDir)) {
            outputDir = System.getProperty("user.dir");
        }

        configuration =  new ClientConfiguration(userName, password, authenBaseUrl, czdsBaseUrl, outputDir);

        return configuration;
    }

    /**
     * If initiated using this constructor, you can specify location of file download
     * */
    private ClientConfiguration(String userName, String password, String authenBaseUrl, String czdsBaseUrl, String outputBaseDir) {
        ClientConfiguration.username = userName;
        ClientConfiguration.password = password;
        ClientConfiguration.authenticationUrl = normalizeWithBackSlash(authenBaseUrl.trim()) + "api/authenticate/";
        ClientConfiguration.czdsDownloadUrl = normalizeWithBackSlash(czdsBaseUrl.trim()) + "czds/downloads/";
        ClientConfiguration.zonefileOutputDirectory = normalizeWithBackSlash(outputBaseDir) + "zonefiles/";
    }

    private static String normalizeWithBackSlash(String value) {
        value = StringUtils.trim(value);

        if(!StringUtils.endsWith(value, "/")) {
            value = value + "/";
        }

        return value;
    }

    private static String checkNullValue(String name, String value) throws IOException {
        if(StringUtils.isBlank(value)) {
            throw new IOException(name + " value cannot be null");
        } else {
            return value.trim();
        }
    }

    public static String getUserName() {
        return ClientConfiguration.username;
    }

    public static void setUserName(String userName) {
        ClientConfiguration.username = userName;
    }

    public static String getPassword() {
        return ClientConfiguration.password;
    }

    public static void setPassword(String password) {
        ClientConfiguration.password = password;
    }

    public static String getAuthenticationUrl() {
        return ClientConfiguration.authenticationUrl;
    }

    public static String getCzdsDownloadUrl() {
        return czdsDownloadUrl;
    }

    public static String getZonefileOutputDirectory() {
        return ClientConfiguration.zonefileOutputDirectory;
    }

    public static void setZonefileOutputDirectory(String zonefileOutputDirectory) {
        ClientConfiguration.zonefileOutputDirectory = normalizeWithBackSlash(zonefileOutputDirectory) + "zonefiles";
    }
}
