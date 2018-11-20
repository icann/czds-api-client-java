# CZDS API client

This repository provides a Java example of how to download zone files via CZDS (Centralized Zone Data Service) 
REST API. A detail [API Specs](https://github.com/icann/czds-api-client/blob/master/docs/ICANN_CZDS_api.pdf) is
included in this reposity in the [docs directory](https://github.com/icann/czds-api-client/tree/master/docs).

# Environment
Language: Java

Build Tool: Maven

# Configurations

You can provide default configurations in `application.properties` file.

```
# REST endpoint for authentication
# Can be overwritten via command line option -a
authentication.base.url=https://czds-api-test.icann.org
   
# REST endpoint for downloading zone files
# Can be overwritten via command line option -c
czds.base.url=https://account-api-test.icann.org
  
# Account credential.
# Optional. Can be overwritten via commandline option -u and -p
icann.account.username=username@example.com
icann.account.password=12345Abcd#
   
# The directory where zone files will be saved
# Optional. Default to current directory.
# Can be overwritten via commandline option -d
working.directory=/where/zonefiles/will/be/saved
```

# Command line Options

Use command line `-t` to specify the TLDs you want to download zone files for. By default, all APPROVED zone files
will be downloaded. You can also use command line options to pass in your account credential and output directory. 
The command line options have higher precedence than the `application.properties` file.

```
usage: ZoneFileDownloader [-a <arg>] [-c <arg>] [-h] [-o <arg>] [-p <arg>] [-t <arg>] [-u <arg>]
 -a,--authen-url <arg>   Specify the authentication REST endpoint base URL.
 -c,--czds-url <arg>     Specify the CZDS REST endpoint base URL.
 -h,--help               Print usage.
 -d,--directory <arg>    Specify the directory where the file(s) will be saved.
 -p,--password <arg>     Specify your password
 -t,--tld <arg>          Specify the TLD(s) you want to download zone file(s) for. Comma separated multiple TLDs. 
                         By default, all APPROVED zone files will be downloaded.
 -u,--username <arg>     Specify your username.
```

# Build
```
mvn clean install
```

It produces an executable jar `./target/zonefile-downloader.jar`

# Run

* To download all of your APPROVED zone files assume that you have set all the required configurations in application.properties
    - run 
    ```
    java -jar ./target/zonefile-downloader.jar
    ```

* To download zone files for one or more TLDs (for example, abb, booking), assume that you have set all the required configurations in application.properties
    - run 
    ```
    java -jar ./target/zonefile-downloader.jar -t abb,booking
    ```

* To pass in all commandline options, 
    - run
```
       java -jar ./target/zonefile-downloader.jar \
       -a https://account-api-test.icann.org \
       -c https://czds-api-test.icann.org \
       -t booking \
       -d /where/you/want/to/save/zonefiles \
       -u username@example.com \
       -p 1234567#Abcdefg
 ``` 
 
 # Documentation
 
 * CZDS REST API Specs - https://github.com/icann/czds-api-client/blob/master/docs/ICANN_CZDS_api.pdf