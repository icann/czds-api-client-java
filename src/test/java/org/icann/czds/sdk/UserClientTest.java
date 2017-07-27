package org.icann.czds.sdk;


import org.icann.czds.sdk.client.UserClient;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;



public class UserClientTest {

    private UserClient userClient;

    @Test
    public void testDownLoadZoneFile() throws IOException, AuthenticationException {
        ClientConfiguration clientConfiguration = buildClientConfiguration("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        userClient = new UserClient(clientConfiguration);

        File file = userClient.downloadZoneFile("aaa");

        Assert.assertTrue(file.getName().equals("aaa.zone"));


    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testDownLoadZoneFileWrongCredentials() throws IOException, AuthenticationException {
        ClientConfiguration clientConfiguration = buildClientConfiguration("wrong@example.com", "Jo6YHwxWCeZL!");
        userClient = new UserClient(clientConfiguration);

        File file = userClient.downloadZoneFile("aaa");



    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testDownLoadZoneFileWrongZone() throws IOException, AuthenticationException {
        ClientConfiguration clientConfiguration = buildClientConfiguration("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        userClient = new UserClient(clientConfiguration);
        userClient.downloadZoneFile("aaaa");
    }

    @Test
    public void testDownLoadAllZoneFile() throws IOException, AuthenticationException {

        ClientConfiguration clientConfiguration = buildClientConfiguration("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        userClient = new UserClient(clientConfiguration);
        List<File> files = userClient.downloadApprovedZoneFiles();

        Assert.assertEquals(files.size(), 1);


    }

    private ClientConfiguration buildClientConfiguration(String userName, String password) throws IOException {
        String authURL = "http://localhost:8080/api/authenticate";
        String downloadURL = "https://czds2-api-qa.icann.org/czds/downloads";
        return new ClientConfiguration(userName, password, authURL, downloadURL);
    }
}
