package org.icann.czds.sdk.example;


import org.icann.czds.sdk.client.UserClient;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/*
This application can be started by providing properties file as input (Refer resources/application.properties for required fields.
If no property file is provided, this application will try to load resources/application.properties(make sure all values are provided)
Once properties are loaded, client will authenticate with global account and download all zone files for which the user is approved for.

If using this client as part of your code, you can ignore this class. Please Refer UserClient.java for detailed usage.)
*/
public class StartDownload {

    public static void main(String[] args) {
        InputStream inputStream = null;
        if (args.length == 1) {
            try {
                inputStream = new FileInputStream(args[0]);
            } catch (FileNotFoundException e) {
                System.out.println("File Not found at given path.");
            }
        } else if (args.length == 0) {
            inputStream = StartDownload.class.getClassLoader().getResourceAsStream("application.properties");
            System.out.println("loaded file from resources");

        } else {
            System.out.println("Usage: Either 0 or 1 argument is allowed. The argument can be a properties file");
            System.exit(1);
        }
        if (inputStream == null) {
            System.out.println("Please provide either input file or add application.properties in resources");
            System.exit(1);
        }

        StartDownload startDownload = new StartDownload();
        ClientConfiguration clientConfiguration = null;
        try {
            clientConfiguration = startDownload.loadApplicationProperties(inputStream);
        } catch (IOException e) {
            System.out.println("Either one or more properties missing. Required properties:\n" +
                    "global.account.url\n" +
                    "czds.download.url\n" +
                    "global.account.username\n" +
                    "global.account.password");
            System.exit(1);
        }

        System.out.println("starting download");

        UserClient userClient = new UserClient(clientConfiguration);
        try {
            String token = userClient.authenticate();
            userClient.downloadApprovedZoneFiles(token);
        } catch (AuthenticationException | IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Completed download. Please check /temp directory for all zone files");

    }

    private ClientConfiguration loadApplicationProperties(InputStream inputStream) throws IOException {
        Properties prop = new Properties();
        try {
            prop.load(inputStream);
            return new ClientConfiguration(prop.getProperty("global.account.username"),
                    prop.getProperty("global.account.password"),
                    prop.getProperty("global.account.url"),
                    prop.getProperty("czds.download.url"));
        } catch (IOException e) {
            throw new IOException();
        }

    }
}
