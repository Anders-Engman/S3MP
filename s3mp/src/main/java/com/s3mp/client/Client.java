package com.s3mp.client;

// Client.java
// Author: Anders Engman
// Date: 6/3/22
// Description: this file independently covers all the functional requirements of a functioning S3MP client CLI application.
//              It contains a variety of core functions such as download, validateDownload, and versions as well as
//              some Quality of Life functions such as "commands", "ping", and "check" amongst others.
// REQUIREMENTS FULFILLED:
//      STATEFUL: 
//      UI: This file generates the UI with which the user interacts

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {

    private Scanner scanner;
    private String simpleServerAddress;
    private String fullServerAddress;
    private String accessToken = "No Token";
    private String refreshToken = "No Token";
    private String userRole = null;
    private String currentCoreSoftware = "kk3wtkNTct";
    private String currentCoreSoftwareVersion = "1.0";
    private String coreSoftwareUrl;
    private Double coreSoftwareSize;
    private String username;
    private String s3mpVersion;
    private String host;
    private int portNumber = 9100;

    public Client(String s3mpVersion) throws NoSuchAlgorithmException, URISyntaxException {

        scanner = new Scanner(System.in);
        String userInput = " ";
        this.s3mpVersion = s3mpVersion;
        this.host = "localhost";
        this.simpleServerAddress = "https://" + this.host + ":" + this.portNumber;
        this.fullServerAddress = this.simpleServerAddress + "/" + this.s3mpVersion;
        this.coreSoftwareUrl = this.fullServerAddress + "/download/initialize/" + currentCoreSoftwareVersion;
        this.coreSoftwareSize = 325.22;

        // Print the welcome prompt
        System.out.println("\n");
        System.out.println("S3MP " + s3mpVersion + " Client Session Initialized...");
        System.out.println("---------------------------------------------------------");
        System.out.println("Please type 'login' and enter valid credentials to begin.");
        System.out.println("For a list of commands, type 'commands' to view all S3MP v1 commands and their descriptions.");
        System.out.println("---------------------------------------------------------");

        // This while loop handles the different command routing
        // Each command is fairly well described by its "command".equalsIgnoreCase declaration.
        // This specificity is in efforts to maintain strictly typed commands which guide user engagement with the
        // platform
        // Note: EqualsIgnoreCase is utilized to facilitate reasonable operation given the potential for slight user error
        while (!"Quit".equalsIgnoreCase(userInput)) {

            System.out.println("Please input a Command:");
            userInput = scanner.nextLine();

            try {
                if ("versions".equalsIgnoreCase(userInput)) {
                    getVersions();
                } else if ("uptodate".equalsIgnoreCase(userInput)) {
                    checkIfUpToDate();
                } else if ("login".equalsIgnoreCase(userInput)) {
                    try {
                        login();
                    } catch (InterruptedException interruptedException) {
                        System.out.println(interruptedException);
                    }
                } else if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                } else if ("ping".equalsIgnoreCase(userInput)) {
                    Boolean serverAvailable = pingServer(this.host, 9100, 2000);

                    if (serverAvailable) {
                        System.out.println("S3MP Server Available.");
                    } else {
                        System.out.println("S3MP Server Unavailable.");
                    }
                } else if("validate".equalsIgnoreCase(userInput)) {
                    validateDownload();
                } else if ("info".equalsIgnoreCase(userInput)) {
                    getDownloadInfo();
                } else if ("check".equalsIgnoreCase(userInput)) {
                    checkInstalledVersion();
                } else if ("download".equalsIgnoreCase(userInput)) {
                    initiateDownload();
                } else if ("refresh".equalsIgnoreCase(userInput)) {
                    refreshToken();
                } else if ("commands".equalsIgnoreCase(userInput)) {
                    printCommandsAndDescriptions();
                } else if ("address".equalsIgnoreCase(userInput)) {
                    setServerAddress();
                }

            } catch (IOException ioException) {
                System.out.println(ioException);
            }

            System.out.println("");
        }

        System.out.println("S3MP Client Session Terminated.");

    }

    // This command is responsible for displaying available core software versions present on the server.
    public void getVersions() throws IOException {

        // Wrapper which handles token presence detection
        if (checkToken()) {
            try {
                // If the user role is not already established locally, get it from the server
                if (this.userRole == null) {
                    this.userRole = getRole();
                }

                URL url = new URL(this.fullServerAddress + "/versions/" + this.userRole);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                // A valid access token is required for access to this resource
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));

                StringBuilder strBuilder = new StringBuilder();

                // this loop receives the incoming information from the server and encodes it as a string.
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                bufferedReader.close();

                // This primitive string array is comprised of properly parsed JSON KV pairs
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s\\[|\\]]", "").split(",");

                if (http.getResponseCode() == 200) {
                    System.out.println(" ");
                    System.out.println("Versions currently live on the server: ");
        
                    // Print the version info including number and stability
                    // The differentiation between lines enables a concise numbered list of versions
                    for (String str : stringArr) {
                        if (str.contains("id")) {
                            System.out.print(str.substring(str.indexOf(":") + 1) + ". ");
                        } else if (str.contains("versionNumber")) {
                            System.out.print("Version " + str.substring(str.indexOf(":") + 1));
                        } else if (str.contains("stability")) {
                            System.out.println(" - Stable Build: " + str.substring(str.indexOf(":") + 1));
                        }
                    }
                } else {
                    System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                }
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            System.out.println("Invalid Token. Please use 'refresh' or 'login' to get new valid tokens.");
            return;
        }
    }

    // This command indicates to the user if their current core software version is up to date with the most updated
    // core software version on the server
    public void checkIfUpToDate() throws IOException {

        // Wrapper which handles token presence detection
        if (checkToken()) {
            try {
                // If the user role is not already established locally, get it from the server
                if (this.userRole == null) {
                    this.userRole = getRole();
                }

                URL url = new URL(this.fullServerAddress + "/latest/" + this.currentCoreSoftwareVersion + "/" + this.userRole);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                // A valid access token is required for access to this resource
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));

                StringBuilder strBuilder = new StringBuilder();
                String inputLine;

                // this loop receives the incoming information from the server and encodes it as a string.
                while ((inputLine = bufferedReader.readLine()) != null) {
                    strBuilder.append(inputLine);
                }

                HashMap<String, String> updateMap = new HashMap<>();
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                // This enhanced for loop handles the population of the hashmap, which holds the KV pairs coming from
                // the S3MP server
                for (String str : stringArr) {
                    updateMap.put(str.substring(0, str.indexOf(":")).replaceAll("\"", ""), (str.substring(str.indexOf(":") + 3)).replaceAll("\"", ""));
                }

                bufferedReader.close();

                // Depending on the HTTP response, printing of installation/error info should change.
                if (http.getResponseCode() == 200) {
                    if (updateMap.get("IsUpToDate").equals("true")) {
                        System.out.println("You're all set! Version " + this.currentCoreSoftwareVersion + " is installed locally and is the latest version.");
                    } else {
                        System.out.println("Your current installation is Version " + this.currentCoreSoftwareVersion + " and is not the latest version available.");
                        System.out.println("Version " + updateMap.get("LatestVersion") + " is live on the server.");
                    }
                } else {
                    System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                }
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            System.out.println("Invalid Token. Please use 'refresh' or 'login' to get new valid tokens.");
            return;
        }
    }

    // This function enables users to determine if the S3MP server is available. It is considered a utility function
    // and is not believed to provide too much information to unauthenticated/unauthorized users to be made
    // available without proper authentication/authorization
    public static boolean pingServer(String host, int port, int timeout) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {

            return false; 
        }
    }

    // While the function is named "login" for easy comprehension, it's more genuinely the combination of authentication
    // and authorization (via token) functions within the S3MP client. User information must be typed correctly (including casing)
    // so no forgiveness is given here (where casing forgiveness is provided in the main menu).

    // NOTE: Potential users are always prompted through the password screen as to not give any excess information
    // to potential malevolent actors such as "Username not found" or "Password incorrect" prompts.
    public void login() throws IOException, InterruptedException {
        
        try {
            String username = " ";
            String password = " ";

            // Take user input directly from the command line
            System.out.println("");
            System.out.println("Please Input Username: ");
            username = scanner.nextLine();
            System.out.println("Please Input Password: ");
            password = scanner.nextLine();

            // This endpoint handles all things OAuth. (access_token and refresh_token)
            URL url = new URL(this.simpleServerAddress + "/oauth/token");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            // Required auth header for token receipt.
            String authHeader = "Basic " + new String(Base64.getEncoder().encode("s3mp:secret".getBytes()));
            // Setting necessary http message headers and properties
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setRequestProperty("Authorization", authHeader);

            // OAuth, by design, is strict about the format of information hitting its endpoint. This encoding is necessary to
            // communicate with the endpoint
            String grant_type = URLEncoder.encode("password", "UTF-8");
            String client_id = URLEncoder.encode("s3mp", "UTF-8");
            String client_secret = URLEncoder.encode("secret", "UTF-8");

            // The 'body' of the http message
            String postDataParams = "grant_type=" + grant_type + 
                                    "&client_id=" + client_id + 
                                    "&client_secret=" + client_secret +
                                    "&username=" + username +
                                    "&password=" + password;

            // Must be written to a byte array
            byte[] out = postDataParams.getBytes(StandardCharsets.UTF_8);

            // Sending request
            OutputStream stream = http.getOutputStream();
            stream.write(out);

            // Receiving response
            StringBuilder strBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                strBuilder.append(line);
            }

            // Utilizing a helper function to properly translate response to usable token information
            HashMap<String, String> tokenMap = convertTokenResponseToHashMap(strBuilder.toString());

            // STATEFUL: The storage/usage of tokens and user sessions indicates S3MPs statefulness on the client side.
            // Set all relevant locally stored information
            this.refreshToken = tokenMap.get("refresh_token");
            this.accessToken = tokenMap.get("access_token");
            this.username = username;

            // If auth is successful, tell the user
            if (http.getResponseCode() == 200) {
                System.out.println("User Authenticated. Welcome " + this.username);
                this.userRole = getRole();
            // Generic failure message advising contact with admins
            } else {
                System.out.println("Authentication failed. Please contact your administrator.");
            }

            http.disconnect();

        } catch (IOException exception) {
            // More often than not, a failed login attempt will land the user here hence the ambiguous messaging
            // This is also the end result for attempted fuzzing of the login flow
            System.out.println("Login Attempt Failed. Please use the 'ping' command to determine if the S3MP server is available.");
            System.out.println("   Additionally, please try logging in again.");
        }
    }

    // This function handles the refresh token flow. Provided a user is utilizing S3MP beyond the validity of their
    // access token, this endpoint enables users to refresh both their access and refresh tokens using just the
    // 'refresh' command. The protocol handles the rest and shelters users from unnecessary detail
    public void refreshToken() throws IOException {
        try {
            // Endpoint for all OAuth related functions
            URL url = new URL(this.simpleServerAddress + "/oauth/token");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            // Similarly to the Login function, all of the configuration for this http request must be adherent
            // to OAuth standards, hence the encoding and formatting.
            String authHeader = "Basic " + new String(Base64.getEncoder().encode("s3mp:secret".getBytes()));
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setRequestProperty("Authorization", authHeader);

            // Setting the variables which will be included in the request
            String grant_type = URLEncoder.encode("refresh_token", "UTF-8");
            String client_id = URLEncoder.encode("s3mp", "UTF-8");
            String client_secret = URLEncoder.encode("secret", "UTF-8");
            String refresh_token = URLEncoder.encode(this.refreshToken, "UTF-8");

            // Note that this differs fromt the login request as the grant_type is now refresh_token instead of
            // username and password
            String postDataParams = "grant_type=" + grant_type + 
                                    "&client_id=" + client_id + 
                                    "&client_secret=" + client_secret +
                                    "&refresh_token=" + refresh_token;

            // Necessary byte array conversion of http body parameters
            byte[] out = postDataParams.getBytes(StandardCharsets.UTF_8);

            // Sending request
            OutputStream stream = http.getOutputStream();
            stream.write(out);

            // Receiving response
            StringBuilder strBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                strBuilder.append(line);
            }

            http.disconnect();

            // Utilizing same helper function to translate incoming information to usable KV pairs in a hashmap
            HashMap<String, String> tokenMap = convertTokenResponseToHashMap(strBuilder.toString());

            // with a successful refresh request comes new refresh and access tokens, which means storage is necessary
            this.refreshToken = tokenMap.get("refresh_token");
            this.accessToken = tokenMap.get("access_token");

            // Tell the user what they need to know "You're good to go" while not conveying unnecessary technical info
            System.out.println("Successful OAuth Token Refresh.");
        } catch (IOException exception) {
            System.out.println("Token Refresh Attempt Failed. Please login to receive a valid token.");
        }
    }

    // This is a private, non-user accessible helper function in charge of determining user roles via GET request.
    // The intent is to ensure proper application interaction from GENERIC and EXPERIMENTAL users but can be
    // expanded to handle additional future roles such as ADMIN and others.
    private String getRole() throws MalformedURLException, IOException {

        try {
            // Roles Endpoint
            URL url = new URL(this.fullServerAddress + "/roles/" + this.username);

            // Simple HTTP message configuration, though it still takes an access token as the endpoint is protected
            // NOTE: This helper function relies on its position within larger functions for the detection of invalid
            // OAuth tokens, thusly it does not require internal Oauth token checking.
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Accept", "application/json");
            http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

            // Receiving input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(line);
            }

            bufferedReader.close();
            http.disconnect();

            // The output from the server is only the ROLE as a string so the builder can be returned in string form
            return strBuilder.toString();

        } catch (IOException exception) {
            // This is an unusual outcome but the protocol has to handle unexpected behavior from unreliable networks
            System.out.println("Role Request Unsuccessful. Please contact your administrator.");
            return "";
        }
    }

    // This function is responsible for the display of information pertaining to a selected download version
    // In the event that the user provides erroneous or 'fuzzy' data, the function will print a polite message
    // pertaining to the incorrect input and the 'versions' command, which the user can utilize to learn the appropriate
    // version numbers
    public void getDownloadInfo() throws MalformedURLException, IOException {

        // Wrapper which handles token presence detection
        if (checkToken()) {
            // General handling for a wide berth of potential exceptions including MalformedURL and IO
            try {
                String lookupVersion = " ";

                System.out.println("");
                System.out.println("Please input the number of the version for which you would like information (ex. 1.0): ");
                lookupVersion = scanner.nextLine();

                // Version validity check. This will route users to the aforementioned advice and error read
                if (checkVersionAgainstAvailableVersions(lookupVersion)) {
                    URL url = new URL(this.fullServerAddress + "/download/info/" + lookupVersion);

                    // Simple OAuth-moderated GET Request
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setRequestMethod("GET");
                    http.setRequestProperty("Accept", "application/json");
                    http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                    // Intake of Incoming Data
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                    StringBuilder strBuilder = new StringBuilder();

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        strBuilder.append(line);
                    }

                    HashMap<String, String> downloadMap = new HashMap<>();
                    // As data comes in string form, extraneous punctuation must be removed, regex works well
                    String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                    //  HashMap is populated with KV pairs using further processed strings
                    for (String str : stringArr) {
                        downloadMap.put(str.substring(0, str.indexOf(":")).replaceAll("\"", ""), (str.substring(str.indexOf(":") + 1)).replaceAll("\"", ""));
                    }

                    bufferedReader.close();
                    
                    // Handler for response code. If successful, user receives a nice message with info about the requested download
                    // If unsuccessful, user receives a polite message with advice on how to determine server availability
                    if (http.getResponseCode() == 200) {
                        System.out.println("Version " + downloadMap.get("version") + " is " + downloadMap.get("size") + "Mb and originates from the S3MP server at URL " + downloadMap.get("URL"));
                    } else {
                        System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                    }

                    http.disconnect();
                } else {
                    System.out.println("Invalid Version Number Provided. Please utilize the 'versions' command to observe available core software versions on the server.");
                    return;
                }
            // Verbose, human-readable exception handling
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            System.out.println("Invalid Token. Please use 'refresh' or 'login' to get new valid tokens.");
            return;
        }
    }

    // This function handles the detection of corrupted downloads, provided a user suspects that their core software download
    // is corrupted. While it uses three metrics (Origin URL, SHA256 hash, and File size), the function does not reveal what methods
    // of evaluation are being employed to determine download integrity and accuracy. Users only need to know if the download is accurate or not.
    public void validateDownload() throws IOException, NoSuchAlgorithmException {
        
        // Wrapper which handles token presence detection
        if (checkToken()) {
            // General handling for a wide berth of potential exceptions including MalformedURL and IO
            try {
                URL url = new URL(this.fullServerAddress + "/download/validate/" + this.currentCoreSoftwareVersion);

                // Simple OAuth-moderated GET Request
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                // Intake of incoming data
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder strBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                HashMap<String, String> validationMap = new HashMap<>();
                // As data comes in string form, extraneous punctuation must be removed, regex works well
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                //  HashMap is populated with KV pairs using further processed strings
                for (String str : stringArr) {
                    validationMap.put(str.substring(0, str.indexOf(":")).replaceAll("\"", ""), (str.substring(str.indexOf(":") + 1)).replaceAll("\"", ""));
                }

                bufferedReader.close();

                // The next three lines of code are a standard Java method of implementing a SHA256 hash
                // This SHA256 implementation comes from: https://www.baeldung.com/sha-256-hashing-java
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(this.currentCoreSoftware.getBytes(StandardCharsets.UTF_8));
                // This is powered by a helper function described below
                String hash = bytesToHex(encodedhash);

                // Handler for response code. If the core software installation present is valid(not corrupt), the user receives a nice message about their installation
                // However, if the installation is invalid/corrupt, the user is advised to check with their system administrator
                if (http.getResponseCode() == 200) {
                    if (this.coreSoftwareUrl.equals(validationMap.get("URL")) && hash.equals(validationMap.get("Hash")) && this.coreSoftwareSize.equals(Double.valueOf(validationMap.get("Size")).doubleValue())) {
                        System.out.println("Current Installation is Valid. No Action is Required.");
                    } else {
                        System.out.println("Current Installation may be Invalid. Recommended Action: Check with Administrator");
                    }

                // If the request fails, give a generic message and advise users to check S3MP server availability
                } else {
                    System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                }

                http.disconnect();
            // Verbose, human-readable exception handling
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            System.out.println("Invalid Token. Please use 'refresh' or 'login' to get new valid tokens.");
            return;
        }
    }

    // This Quality of Life function enables users to display information about their current core software installation.
    // Technically the same information can be retrieved from the getDownloadInfo function but this specific function
    // does not require the users to know their core software version ahead of time
    // Importantly, 'check' also provides users with the version of S3MP being run on the server. This can be used for admin
    // configuration without authentication but could be split into a separate more secure function in future iterations if security concerns arise since
    // the function is not secured by tokens. However, no user input travels with the request so SQL injections and such
    // are not the main concern, more so potentially exposing the server S3MP verion info and local core software info to unauthorized
    // parties
    public void checkInstalledVersion() throws MalformedURLException, IOException {

        try {
            URL url = new URL(this.fullServerAddress + "/check");

            // Simple GET request, no OAuth
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Accept", "application/json");
            
            // Intake of Incoming Data
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();

            // Incoming Data will only be comprised of the S3MP server version number
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(line);
            }

            // Handler for response code, if successful, local installation info and the servers S3MP version
            // If unsuccessful, it will display a generic message about an unsuccessful request and using 'ping' for server availability checking
            if (http.getResponseCode() == 200) {
                System.out.println("The current installed core software version is " + this.currentCoreSoftwareVersion);
                System.out.println("   File Size: " + this.coreSoftwareSize + "Mb || Origin URL: " + this.coreSoftwareUrl);
                System.out.println("The current version of S3MP Installed on the Server is " + strBuilder.toString());
            } else {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }

            http.disconnect();
        // Verbose, human-readable exception handling
        } catch (IOException exception) {
            System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
        }
    }

    // This function is responsible for the donwload of a target core software version. In this implementation, the software is downloaded
    // as a stream of octets and is saved locally as a .txt file. This is currently a mocked process to display the ability of the protocol
    // to download a file from the server in the form of a stream of octets. However, in a genuine implementation, it is likely that
    // implementation specific changes would have to be made to address different situational requirements
    public void initiateDownload() throws MalformedURLException, IOException {

        // Wrapper which handles token presence detection
        if (checkToken()) {
            try {
                String downloadVersion = " ";

                // Prompt for user to input desired core software version
                System.out.println("");
                System.out.println("Please input the number of the version which you would like to download (ex. 1.0): ");
                downloadVersion = scanner.nextLine();

                // This wrapper protects the system against invalid version numbers, notifies users of incorrect inputs
                if (checkVersionAgainstAvailableVersions(downloadVersion)) {

                    URL url = new URL(this.fullServerAddress + "/download/initialize/" + downloadVersion);

                    // Simple OAuth-moderated GET request
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setRequestMethod("GET");
                    http.setRequestProperty("Accept", "application/json");
                    http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                    // Intake of Incoming Data
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                    StringBuilder strBuilder = new StringBuilder();

                    String line;
                    try {
                        while ((line = bufferedReader.readLine()) != null) {
                            strBuilder.append(line);
                        }
                    } catch (FileNotFoundException exception) {
                        System.out.println("File Writing Unsuccessful. Please try again.");
                    }

                    // Write the incoming data to a .txt file and store in the working directory
                    BufferedWriter out = new BufferedWriter(new FileWriter("software.txt"));

                    try {

                        out.write(strBuilder.toString());
                    // In case the file writing encounters an error
                    } catch (IOException ioException) {

                        System.out.println("File Writing Unsuccessful. Please attempt the download again.");

                    // Close the FileWriter
                    } finally {
                        out.close();
                    }

                    // Once the core software is downloaded successfully, the state is updated to resemble the new software present
                    // in the client's environment
                    System.out.println("Software downloaded successfully. Please check the directory from which you are running this client for the download.");
                    this.currentCoreSoftwareVersion = downloadVersion;
                    HashMap<String, String> downloadInfoMap = localInstallationPostDownloadInformationUpdate(downloadVersion);
                    this.currentCoreSoftware = downloadInfoMap.get("software");
                    this.coreSoftwareUrl = downloadInfoMap.get("URL");
                    this.coreSoftwareSize = Double.valueOf(downloadInfoMap.get("size")).doubleValue();

                    http.disconnect();

                // In case the user provides an invalid version number or generally invalid input
                } else {
                    System.out.println("Invalid Version Number Provided. Please utilize the 'versions' command to observe available core software versions on the server.");
                    return;
                }
            // Verbose, human-readable exception handling
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            System.out.println("Invalid Token. Please use 'refresh' or 'login' to get new valid tokens.");
            return;
        }
    }

    // This helper function grabs information about a newly downloaded core software package. It is separated from the file download
    // to reduce the likelihood of erroneous information transfer. This request is OAuth-authenticated and is not available for users
    // to utilize manually.
    private HashMap<String, String> localInstallationPostDownloadInformationUpdate(String downloadVersion) throws IOException {

        // Wrapper which handles token presence detection
        if (checkToken()) {
            try {
                URL url = new URL(this.fullServerAddress + "/download/info/" + downloadVersion);

                // Simple OAuth-moderated GET Request
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                // Intake of Incoming Data
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder strBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                HashMap<String, String> downloadMap = new HashMap<>();
                // As data comes in string form, extraneous punctuation must be removed, regex works well
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                //  HashMap is populated with KV pairs using further processed strings
                for (String str : stringArr) {
                    downloadMap.put(str.substring(0, str.indexOf(":")).replaceAll("\"", ""), (str.substring(str.indexOf(":") + 1)).replaceAll("\"", ""));
                }

                bufferedReader.close();

                return downloadMap;
            // Verbose, human-readable exception handling advising user to consult admin, due to the fact that the user would not have known
            // this function were to be running, as they did not trigger it
            } catch (IOException ioException) {
                System.out.println("Download Retrieval Information Update Failed. Please contact your administrator.");
                return new HashMap<String, String>();
            }
        } else {
            System.out.println("Invalid Token. Please use 'refresh' or 'login' to get new valid tokens.");
            return new HashMap<>();
        }
    }

    // This helper function is responsible for checking if user input is valid according to the core software versions stored on the
    // server. It interprets strictly so '1' and '1.0' would be considered 'invalid' and 'valid' version inputs respectively. In future
    // versions of S3MP, it would be a nice quality of life improvement for the validity check to be more forgiving while still
    // handling user input appropriately. This function is not available to users directly.
    private boolean checkVersionAgainstAvailableVersions(String version) throws MalformedURLException, IOException {

        boolean validVersion = false;

        // Wrapper which handles token presence detection
        if (checkToken()) {
            try {
                // If the user role is not already established locally, get it from the server
                if (this.userRole == null) {
                    this.userRole = getRole();
                }

                URL url = new URL(this.fullServerAddress + "/versions/" + this.userRole);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                // A valid access token is required for access to this resource
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));

                StringBuilder strBuilder = new StringBuilder();

                // this loop receives the incoming information from the server and encodes it as a string.
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                // ArrayList to contain all versions on server (visibility depending on role).
                ArrayList<String> versionList = new ArrayList<>();
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                for (String str : stringArr) {
                    if (str.contains("versionNumber")) {
                        versionList.add(str.substring(str.indexOf(":") + 1));
                    }
                }

                // boolean determined by presence or absence of version string in version list.
                validVersion = versionList.contains(version);

                bufferedReader.close();

                return validVersion;

            // Exception advising admin consultation as users would not know that this function has fired, given that they have not
            // triggered it
            } catch (Exception exception) {
                System.out.println("Version Comparison Check Failed. Please contact your administrator.");
                return validVersion;
            }
        } else {
            System.out.println("Invalid Token. Please use 'refresh' or 'login' to get new valid tokens.");
            return false;
        }
    }

    // Helper function which translates a string from a token POST request to a token hashmap.
    // This function is not available to users.
    public HashMap<String, String> convertTokenResponseToHashMap(String response) {

        HashMap<String, String> tokenMap = new HashMap<>();

        // Removal of certain punctuation from input string
        String[] stringArr = response.replaceAll("[\\{\\}\\s]", "").split(",");

        String[] keyValueStringArray;

        // Enhanced for loop which splits remaining strings into KV pairs for the hashmap
        for (String str : stringArr) {
            keyValueStringArray = str.split(":");
            tokenMap.put(keyValueStringArray[0].replaceAll("\"", ""), keyValueStringArray[1].replaceAll("\"", ""));
        }

        return tokenMap;
    }

    // Necessary helper function which translates a byte array hash to a hexadecimal string.
    // This function is not available to users.
    // This implementation of the function comes from: https://www.baeldung.com/sha-256-hashing-java
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Simple helper function which checks if the token is in its initial state of "No Token".
    // Wraps the response to unauthorized access attempts in human readable feedback which provides meaningful/actionable input to users
    private Boolean checkToken() {
        if ((!"No Token".equals(this.accessToken)) || (!"No Token".equals(this.refreshToken))) {
            return true;
        } else {
            System.out.println("Unauthorized Action. Please login to be Authorized.");
            return false;
        }
    }

    // This simple function provides users with the ability to list all v1 S3MP functions and some brief descriptions.
    // Can be used with or without authentication/authorization so users can learn the commands regardless of application state
    // Users are advised to utilize this function via the welcome prompt.
    private void printCommandsAndDescriptions() {
        System.out.println(" ");
        System.out.println("-----------------------------------Commands--------------------------------------");
        System.out.println(" 'login': opens prompt for user credentials. If successful, get an OAuth session token");
        System.out.println(" 'ping': display the availability of the S3MP server.");
        System.out.println(" 'commands': view all S3MP v1 commands and their descriptions.");
        System.out.println(" NOTE: All subsequent commands require successful authentication using the 'login' command");
        System.out.println("");
        System.out.println(" 'refresh': given previously successful login, refresh your OAuth session token once expired.");
        System.out.println(" 'versions': display available core software versions on the server currently.'");
        System.out.println(" 'uptodate': determine if currently installed core software version is the most up to date.");
        System.out.println(" 'validate': determine if currently installed core software version is corrupt or valid.");
        System.out.println(" 'info': display information on a specific core software version available on the server.");
        System.out.println(" 'check': display information about server S3MP version and local core software installation");
        System.out.println(" 'download': download a specific core software version from the server.");
        System.out.println(" 'quit': exit the S3MP client user interface.");
        System.out.println("-------------------------------------End-----------------------------------------");
        System.out.println("-----------------------------------Commands--------------------------------------");
        System.out.println(" ");
    }

    // CLIENT: this function enables users to set the server address to a custom IP or Hostname.
    // Unlike much of the design philosophy of the rest of the client, this function can enable users to break the application
    // provided they are not mindful of the correctness of their input. However, the same function can be called to fix any issues
    // caused by erroneous input. Additionally, this function is available outside of an authenticated/authorized session.
    private void setServerAddress() {
        String newServerAddress = "";
        System.out.println("\n");
        System.out.println("Please input a new server address (Hostname or IP address) or 'default' for the default hostname");
        System.out.println("Note: malformed addresses can cause the S3MP client application to behave unexpectedly / not function.");
        System.out.println("Malformed addresses would, for example, include if 'https://' was included such as 'https://localhost:'");
        System.out.println("or if a trailing slash was added such as 'localhost/'.");
        System.out.println("Since S3MP runs on port 9100, please do not include the port in your custom address.");
        newServerAddress = scanner.nextLine();

        if (newServerAddress.equalsIgnoreCase("default")) {
            newServerAddress = "localhost";
        }

        this.host = newServerAddress;
        this.simpleServerAddress = "https://" + this.host + ":" + this.portNumber;
        this.fullServerAddress = this.simpleServerAddress + "/" + this.s3mpVersion;
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client("v1");
    }
}