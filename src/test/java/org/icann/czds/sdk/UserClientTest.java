package org.icann.czds.sdk;


import org.icann.czds.sdk.client.UserClient;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientAuthentication;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;


public class UserClientTest {

    private UserClient userClient;

    @BeforeClass
    public void setup() {
        String authURL = "http://localhost:8080/api/authenticate";
        String downloadURL = "https://czds2-api-qa.icann.org/czds/downloads";
        userClient = new UserClient(authURL, downloadURL);
    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testAuthenticate() throws IOException, AuthenticationException {
        ClientAuthentication clientAuthentication = new ClientAuthentication("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        String token = userClient.authenticate(clientAuthentication);

        Assert.assertTrue(token.length() > 0);

        clientAuthentication = new ClientAuthentication("jane.johnson18@example1.com", "Jo6YHwxWCeZL!");
        userClient.authenticate(clientAuthentication);
    }

    @Test
    public void testDownLoadZoneFile() throws IOException, AuthenticationException {

        ClientAuthentication clientAuthentication = new ClientAuthentication("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        String token = userClient.authenticate(clientAuthentication);


        File file = userClient.downloadZoneFile("aaa", token);

        Assert.assertTrue(file.getName().equals("aaa.zone"));


    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testDownLoadZoneFileWrongToken() throws IOException, AuthenticationException {
        ClientAuthentication clientAuthentication = new ClientAuthentication("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        String token = userClient.authenticate(clientAuthentication);

        File file = userClient.downloadZoneFile("aaa", token + "test");

    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testDownLoadZoneFileWrongZone() throws IOException, AuthenticationException {
        ClientAuthentication clientAuthentication = new ClientAuthentication("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        String token = userClient.authenticate(clientAuthentication);
        userClient.downloadZoneFile("aaaa", token + "test");
    }

    @Test
    public void testDownLoadAllZoneFile() throws IOException, AuthenticationException {

        ClientAuthentication clientAuthentication = new ClientAuthentication("jane.johnson18@example.com", "Jo6YHwxWCeZL!");
        String token = userClient.authenticate(clientAuthentication);
        List<File> files = userClient.downloadApprovedZoneFiles(token);

        Assert.assertEquals(files.size(), 1);


    }
}
