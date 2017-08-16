package example;


import org.icann.czds.sdk.client.UserClient;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.*;
import java.util.List;
import java.util.Properties;

/*
If using this client as part of your code, you can ignore this class. Please Refer UserClient.java for detailed usage.)
*/
public class ZoneDownloadExample {

    public static void main(String[] args) {
        new ZoneDownloadExample().run(args);

    }

    public void run(String[] args) {
        ClientConfiguration clientConfiguration = null;
        try {
            clientConfiguration = new ClientConfiguration("test5@performance.test",
                    "8D!#MdoX2h-y",
                    "http://localhost:8088/api/authenticate",
                    "http://localhost:8081/czds/downloads",
                    "/Users/krishna.raghavendra/Desktop/Projects/czds-client/temp/temp");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        UserClient userClient = new UserClient(clientConfiguration);

        System.out.println("starting download");
        List<File> fileList = userClient.downloadApprovedZoneFiles(); //example to download all approved zone files
        if (!fileList.isEmpty()) {
            fileList.forEach(file -> System.out.println(file.getAbsolutePath()));
        }

        File file = userClient.downloadZoneFile("juniper.zone"); // example to download particular zone file
        if (file != null) {
            System.out.println(file.getAbsolutePath());
        }
    }


}
