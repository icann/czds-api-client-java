package example;

import org.apache.commons.cli.*;
import org.icann.czds.sdk.client.CzdsZoneDownloadClient;
import org.icann.czds.sdk.model.AuthenticationException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static example.CommandlineParser.mergeCommandOptions;
import static example.CommandlineParser.parseCommandLineArguments;

/**
 * This application downloads zone files via CZDS REST endpoint
 */
public class ZoneFileDownloader {

    private CzdsZoneDownloadClient client;

    public static void main(String[] args) {
        new ZoneFileDownloader().run(args);
    }

    public void run(String[] args) {

        // Parse command line arguments.
        CommandLine commandLine = parseCommandLineArguments(args);
        if(commandLine == null) {
            System.exit(1);
        }

        // Build the REST API wrapper - CzdsZoneDownloadClient
        try {
            client = new CzdsZoneDownloadClient(mergeCommandOptions(commandLine));
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
