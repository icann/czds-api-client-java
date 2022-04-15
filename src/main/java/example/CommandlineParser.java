package example;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.icann.czds.sdk.model.ClientConfiguration;

import java.io.IOException;

public class CommandlineParser {

    /**
     * Build client
     *
     * @param commandLine The command line options
     *
     * @return {@link ClientConfiguration}
     *
     * @throws IOException Failed to get the command line options together
     */
    public static ClientConfiguration mergeCommandOptions(CommandLine commandLine) throws IOException {
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
            configuration.setAuthenticationBaseUrl(commandLine.getOptionValue("authen-url"));
        }

        // CZDS base URL
        if(commandLine.hasOption("czds-url")) {
            configuration.setCzdsBaseUrl(commandLine.getOptionValue("czds-url"));
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
        if(commandLine.hasOption("directory")) {
            configuration.setWorkingDirectory(commandLine.getOptionValue("directory"));
        }

        // Make sure all configurations are provided
        String errorMsg = configuration.validate();
        if(!StringUtils.isBlank(errorMsg)) {
            System.out.println("ERROR: missing the required configurations. " +
                    "Please provide them in either application.properties file or pass in via command line options");
            System.out.println(errorMsg);
            System.exit(1);
        }

        return configuration;
    }

    /**
     * Parse command line options
     *
     * @param args The command line arguments
     *
     * @return {@link CommandLine}
     */
    public static CommandLine parseCommandLineArguments(String[] args) {

        Options options = new Options();

        // tld option can take multiple arguments, comma separated
        Option tldOption = new Option("t", "tld", true,
                "Specify the TLD(s) you want to download zone file(s) for. " +
                        "Comma separated multiple TLDs. By default, all APPROVED zone files will be downloaded.");
        tldOption.setArgs(Option.UNLIMITED_VALUES);
        tldOption.setValueSeparator(',');

        options.addOption("u", "username", true, "Specify your username.")
                .addOption("p", "password", true, "Specify your password")
                .addOption("d", "directory", true, "Specify the working directory.")
                .addOption("h", "help", false, "Print usage.")
                .addOption("a", "authen-url", true, "Specify the authentication REST endpoint base URL.")
                .addOption("c", "czds-url", true, "Specify the CZDS REST endpoint base URL.")
                .addOption(tldOption);

        // Create a parser
        CommandLineParser parser = new DefaultParser();

        // Parse the options passed as command line arguments
        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("help")) {
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

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ZoneFileDownloader", options, true);
    }
}