# CZDS API client

This repository provides an example of how to download zone files via CZDS (Centralized Zone Data Service) 
REST endpoint.

# Environment
Language: Java

Build Tool: Maven

# Configuration

You can provide default configuration in `application.properties` file.

```
# REST endpoint for authentication
# Required
authentication.base.url=https://accounts-api-qa.icann.org
   
# REST endpoint for downloading zone files
# Required
czds.base.url=https://czds2-api-qa.icann.org
  
# Account credential.
# Optional. Can be overwritten via commandline option -u and -p
icann.account.username=username@example.com
icann.account.password=12345Abcd#
   
# The directory where zone files will be saved
# Optional. Default to current directory.
# Can be overwritten via commandline option -o
zonefile.output.directory=/where/zonefiles/will/be/saved
```

# Command line Options

Use command line `-t` to specify the TLDs you want to download zone files for. By default, all APPROVED zone files
will be downloaded. You can also use command line options to pass in your account credential and output directory. 
The command line options have higher precedence than the `application.properties` file.

```
usage: ZoneFileDownloader [-h] [-o <arg>] [-p <arg>] [-t <arg>] [-u <arg>]
 -h,--help             Print usage.
 -o,--output <arg>     Specify the output directory where the file(s) will be saved.
 -p,--password <arg>   Specify your password
 -t,--tld <arg>        Specify the TLD(s) you want to download zone file(s) for. Comma separated multiple TLDs. 
                       By default, all APPROVED zone files will be downloaded.
 -u,--username <arg>   Specify your username.
```

# Build

Run **`mvn clean install`**

# Run

* To download all of your APPROVED zone files, 
    - run **`java -jar ./target/zonefile-downloader.jar`**

* To download zone files for one or more TLDs (for example, abb, booking), 
    - run **`java -jar ./target/zonefile-downloader.jar -t abb,booking`**

* To pass other commandline options, 
    - run **`java -jar ./target/zonefile-downloader.jar -t booking -o /tmp/myzonefiles -u account@example.com -p mypassword`**
