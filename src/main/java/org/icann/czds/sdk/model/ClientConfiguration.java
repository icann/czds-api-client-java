package org.icann.czds.sdk.model;


import java.io.IOException;

public class ClientConfiguration {


    private String userName;
    private String password;
    private String globalAccountURL;
    private String czdsDownloadURL;

    public ClientConfiguration(String userName, String password, String globalAccountURL, String czdsDownloadURL) throws IOException {
        checkNullValue(userName);
        checkNullValue(password);
        checkNullValue(globalAccountURL);
        checkNullValue(czdsDownloadURL);
        this.userName = userName.trim();
        this.password = password.trim();
        this.globalAccountURL = globalAccountURL.trim();
        this.czdsDownloadURL = czdsDownloadURL.trim();
    }

    private void checkNullValue(String value) throws IOException {
        if(value==null || value.trim().length()==0){
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
}
