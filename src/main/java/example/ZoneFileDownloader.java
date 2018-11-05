package example;


import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.icann.czds.sdk.client.CZDSClient;
import org.icann.czds.sdk.model.AuthenticationException;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This application downloads zone files via CZDS REST endpoint
 */
public class ZoneFileDownloader {

    private CZDSClient client;

    public static void main(String[] args) {
        new ZoneFileDownloader().run(args);
    }

    public void run(String[] args) {

        // Parse command line arguments.
        CommandLine commandLine = parseCommandLineArguments(args);
        if(commandLine == null) {
            System.exit(1);
        }

        // Build the REST API wrapper - CZDSClient
        try {
            client = buildCzdsClient(commandLine);
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        // New ready to download zone files
        try {
            String[] tlds = null;
            if(commandLine.hasOption("tld")) {
                tlds = commandLine.getOptionValues("tld");
            }
            if(tlds == null || tlds.length == 0) {
                // Download all the APPROVED zone files
                downloadAllApprovedZoneFiles();
            } else {
                // Download the given zone files
                downloadZoneFile(tlds);
            }

        } catch (IOException | AuthenticationException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Parse command line options
     */
    private CommandLine parseCommandLineArguments(String[] args) {

        Options options = new Options();

        // tld option can take multiple arguments, comma separated
        Option tldOption = new Option("t", "tld", true,
                "Specify the TLD(s) you want to download zone file(s) for. " +
                        "Comma separated multiple TLDs. By default, all APPROVED zone files will be downloaded.");
        tldOption.setArgs(Option.UNLIMITED_VALUES);
        tldOption.setValueSeparator(',');

        options.addOption("u", "username", true, "Specify your username.")
                .addOption("p", "password", true, "Specify your password")
                .addOption("o", "output", true, "Specify the output directory where the file(s) will be saved.")
                .addOption("h", "help", false, "Print usage.")
                .addOption("a", "authen-url", true, "Specify the authentication REST endpoint base URL.")
                .addOption("c", "czds-url", true, "Specify the CZDS REST endpoint base URL.")
                .addOption(tldOption);

        // Create a parser
        CommandLineParser parser = new DefaultParser();

        // Parse the options passed as command line arguments
        try {
            CommandLine commandLine = parser.parse( options, args);

            if(commandLine.hasOption("help")) {
                printUsage(options);
                System.exit(1);
            }
            return commandLine;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsage(options);
            return null;
        }
    }

    /**
     * Build client
     */
    private CZDSClient buildCzdsClient(CommandLine commandLine) throws IOException {
        // Lets get an instance of ClientConfiguration
        ClientConfiguration configuration = null;
        try {
            configuration = ClientConfiguration.getInstance();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        }

        // Authetication base URL
        if(commandLine.hasOption("authen-url")) {
            configuration.setAuthenticationUrl(commandLine.getOptionValue("authen-url"));
        }

        // CZDS base URL
        if(commandLine.hasOption("czds-url")) {
            configuration.setCzdsDownloadUrl(commandLine.getOptionValue("czds-url"));
        }

        // Username
        if(commandLine.hasOption("username")) {
            configuration.setUserName(commandLine.getOptionValue("username"));
        }

        // Password
        if(commandLine.hasOption("password")) {
            configuration.setPassword(commandLine.getOptionValue("password"));
        }

        // Directory
        String directory = null;
        if(commandLine.hasOption("output")) {
            configuration.setZonefileOutputDirectory(commandLine.getOptionValue("output"));
        }

        // Make sure all configurations are provided
        String errorMsg = ClientConfiguration.validate();
        if(!StringUtils.isBlank(errorMsg)) {
            System.out.println("ERROR: missing the required configurations. " +
                    "Please provide them in either application.properties file or pass in via command line options");
            System.out.println(errorMsg);
            System.exit(1);
        }

        return new CZDSClient(configuration);
    }

    /**
     * Example of downloading all your APPROVED zone files at once
     */
    private void downloadAllApprovedZoneFiles() throws IOException, AuthenticationException {
        System.out.println("Start downloading all APPROVED zone files. This may take a few minutes.");
        List<File> fileList = client.downloadApprovedZoneFiles();
        printResultFiles(fileList);
    }

    /**
     * Example of downloading the zone file for the given TLD
     */
    private void downloadZoneFile(String[] tlds) throws IOException, AuthenticationException {
        System.out.println("Start download zone file(s). This may take a few minutes.");

        List<File> fileList = new ArrayList<>();
        for(int i = 0; i < tlds.length; i ++) {
            fileList.add(client.downloadZoneFile(tlds[i]));
        }

        printResultFiles(fileList);
    }

    private void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ZoneFileDownloader", options, true);
    }

    private void printResultFiles(List<File> fileList) {
        System.out.println("Downloading completed. Here are the file(s):");
        if (!fileList.isEmpty()) {
            fileList.forEach(file -> System.out.println(file.getAbsolutePath()));
        }
    }
}
