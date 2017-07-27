package example;


import org.icann.czds.sdk.client.UserClient;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.*;
import java.util.List;
import java.util.Properties;

/*
This application will try to load resources/application.properties(make sure all values are provided)
Once properties are loaded, client will authenticate with global account and download all zone files for which the user is approved for.

If using this client as part of your code, you can ignore this class. Please Refer UserClient.java for detailed usage.)
*/
public class ZoneDownloadExample {

    public static void main(String[] args) {
        new ZoneDownloadExample().run(args);

    }

    public void run(String[] args) {

        InputStream inputStream = ZoneDownloadExample.class.getClassLoader().getResourceAsStream("application.properties");

        ClientConfiguration clientConfiguration = null;
        try {
            clientConfiguration = loadApplicationProperties(inputStream);
        } catch (IOException e) {
            System.out.println("Either one or more properties missing. Required properties:\n" +
                    "global.account.url\n" +
                    "czds.download.url\n" +
                    "global.account.username\n" +
                    "global.account.password");
            System.exit(1);
        }


        UserClient userClient = new UserClient(clientConfiguration);
        try {
            System.out.println("starting download");
            List<File> fileList = userClient.downloadApprovedZoneFiles(); //to download all approved zone files
            fileList.forEach(file -> System.out.println(file.getAbsolutePath()));

            File file = userClient.downloadZoneFile("aaa"); //to download particular zone file
            System.out.println(file.getAbsolutePath());
        } catch (AuthenticationException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
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
