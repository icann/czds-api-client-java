package org.icann.czds.sdk.model;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfiguration {

    private static  ClientConfiguration configuration = null;

    private  String username;
    private  String password;
    private  String authenticationBaseUrl;
    private  String czdsBaseUrl;
    private  String czdsDownloadBaseUrl;
    private  String workingDirectory;


    public static ClientConfiguration getInstance() throws IOException{
        if(configuration == null) {
            configuration = loadDefaultClientConfiguration();
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
        String czdsDownloadBaseUrl = properties.getProperty("czds.download.base.url");

        // Default to current dir if zonefile.output.directory is not specified.
        String workingDir = properties.getProperty("working.directory");
        if(StringUtils.isBlank(workingDir)) {
            workingDir = System.getProperty("user.dir");
        }

        if(czdsDownloadBaseUrl == null){
            czdsDownloadBaseUrl = czdsBaseUrl;
        }
        return new ClientConfiguration(userName, password, authenBaseUrl, czdsBaseUrl, czdsDownloadBaseUrl, workingDir);
    }

    /**
     * If initiated using this constructor, you can specify location of file download
     * */
    private ClientConfiguration(String userName, String password, String authenBaseUrl, String czdsBaseUrl, String workingDir) {
        this(userName, password, authenBaseUrl, czdsBaseUrl, czdsBaseUrl, workingDir);
    }

    private ClientConfiguration(String userName, String password, String authenBaseUrl, String czdsBaseUrl, String downloadurl, String workingDir) {
        setUserName(userName);
        setPassword(password);
        setAuthenticationBaseUrl(authenBaseUrl);
        setCzdsBaseUrl(czdsBaseUrl);
        setCzdsDownloadBaseUrl(downloadurl);
        setWorkingDirectory(workingDir);
    }

    private static String normalizeWithBackSlash(String value) {
        value = StringUtils.trim(value);

        if(!StringUtils.endsWith(value, "/")) {
            value = value + "/";
        }

        return value;
    }

    public static String checkNullValue(String name, String value) throws IOException {
        if(StringUtils.isBlank(value)) {
            throw new IOException(String.format("ERROR: missging %. Please configure it in application.properties file or pass in via command line.", name));
        } else {
            return value.trim();
        }
    }

    public String validate() {
        StringBuilder sb = new StringBuilder();

        // Check Authentication url
        if(StringUtils.isBlank(getAuthenticationBaseUrl())) {
            sb.append("Missing authentication base URL.\n");
        }

        // Check CZDS url
        if(StringUtils.isBlank(getCzdsBaseUrl())) {
            sb.append("Missing CZDS base URL.\n");
        }

        // check username
        if(StringUtils.isBlank(getUserName())) {
            sb.append("Missing username.\n");
        }

        // check password
        if(StringUtils.isBlank(getPassword())) {
            sb.append("Missing password.\n");
        }

        return sb.toString();
    }

    public  String getUserName() {
        return username;
    }

    public  void setUserName(String userName) {
        if(!StringUtils.isBlank(userName)) {
            this.username = userName;
        }
    }

    public String getPassword() {
        return password;
    }

    public  void setPassword(String password) {
        if(!StringUtils.isBlank(password)) {
           this.password = password;
        }
    }

    public String getAuthenticationBaseUrl() {
        return authenticationBaseUrl;
    }



    public void setAuthenticationBaseUrl(String authenBaseUrl) {
        this.authenticationBaseUrl = authenBaseUrl;
    }


    public String getCzdsBaseUrl() {
        return czdsBaseUrl;
    }

    public void setCzdsBaseUrl(String czdsBaseUrl) {
        this.czdsBaseUrl = czdsBaseUrl;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String directory) {
        this.workingDirectory = directory;
    }

    public String getCzdsDownloadBaseUrl() {
        return czdsDownloadBaseUrl;
    }

    public void setCzdsDownloadBaseUrl(String czdsDownloadBaseUrl) {
        this.czdsDownloadBaseUrl = czdsDownloadBaseUrl;
    }
}
