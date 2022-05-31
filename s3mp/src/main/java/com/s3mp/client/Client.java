package com.s3mp.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
// import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {

    private Scanner scanner;
    private String serverAddress;
    private String s3mpVersion;
    private String accessToken = "No Token";
    private String refreshToken = "No Token";
    private String userRole = null;
    private String currentCoreSoftware = "kk3wtkNTct";
    private String currentCoreSoftwareVersion = "1.0";
    private String coreSoftwareUrl;
    private Double coreSoftwareSize;
    private String username;

    public Client(String serverAddress, String s3mpVersion) throws NoSuchAlgorithmException, URISyntaxException {

        scanner = new Scanner(System.in);
        this.serverAddress = serverAddress + "/" + s3mpVersion;
        this.coreSoftwareUrl = this.serverAddress + "/download/initialize/" + currentCoreSoftwareVersion;
        this.coreSoftwareSize = 325.22;

        System.out.println("\n");
        System.out.println("S3MP " + s3mpVersion + " Client Session Initialized...");
        System.out.println("---------------------------------------------------------");
        System.out.println("Please type 'login' and enter valid credentials to begin.");
        System.out.println(" For a list of commands, type 'commands' to view all S3MP v1 commands and their descriptions.");
        System.out.println("---------------------------------------------------------");

        String userInput = " ";

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
                    Boolean serverAvailable = pingServer("127.0.0.1", 443, 2000);

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
                }

            } catch (IOException ioException) {
                System.out.println(ioException);
            }

            System.out.println("");
        }

        System.out.println("S3MP Client Session Terminated.");

    }

    public void getVersions() throws IOException {

        if (checkToken()) {
            try {
                if (this.userRole == null) {
                    this.userRole = getRole();
                }

                URL url = new URL(this.serverAddress + "/versions/" + this.userRole);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));

                StringBuilder strBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                bufferedReader.close();

                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s\\[|\\]]", "").split(",");

                if (http.getResponseCode() == 200) {
                    System.out.println(" ");
                    System.out.println("Versions currently live on the server: ");
        
                    for (String str : stringArr) {
                        if (str.contains("id")) {
                            System.out.print(str.substring(str.indexOf(":") + 1) + ". ");
                        } else if (str.contains("versionNumber")) {
                            System.out.println("Version " + str.substring(str.indexOf(":") + 1));
                        }
                    }
                } else {
                    System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                }
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            return;
        }
    }

    public void checkIfUpToDate() throws IOException {

        if (checkToken()) {
            try {
                if (this.userRole == null) {
                    this.userRole = getRole();
                }

                URL url = new URL(this.serverAddress + "/latest/" + this.currentCoreSoftwareVersion + "/" + this.userRole);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));

                StringBuilder strBuilder = new StringBuilder();
                String inputLine;

                while ((inputLine = bufferedReader.readLine()) != null) {
                    strBuilder.append(inputLine);
                }

                HashMap<String, String> updateMap = new HashMap<>();
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                for (String str : stringArr) {
                    updateMap.put(str.substring(0, str.indexOf(":")).replaceAll("\"", ""), (str.substring(str.indexOf(":") + 3)).replaceAll("\"", ""));
                }

                bufferedReader.close();

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
            return;
        }
    }

    public static boolean pingServer(String host, int port, int timeout) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {

            return false; 
        }
    }

    public void login() throws IOException, InterruptedException {
        try {
            String username = " ";
            String password = " ";

            System.out.println("");
            System.out.println("Please Input Username: ");
            username = scanner.nextLine();
            System.out.println("Please Input Password: ");
            password = scanner.nextLine();

            URL url = new URL("https://localhost:443/oauth/token");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            String authHeader = "Basic " + new String(Base64.getEncoder().encode("s3mp:secret".getBytes()));
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setRequestProperty("Authorization", authHeader);

            String grant_type = URLEncoder.encode("password", "UTF-8");
            String client_id = URLEncoder.encode("s3mp", "UTF-8");
            String client_secret = URLEncoder.encode("secret", "UTF-8");

            String postDataParams = "grant_type=" + grant_type + 
                                    "&client_id=" + client_id + 
                                    "&client_secret=" + client_secret +
                                    "&username=" + username +
                                    "&password=" + password;

            byte[] out = postDataParams.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            StringBuilder strBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                strBuilder.append(line);
            }

            HashMap<String, String> tokenMap = convertTokenResponseToHashMap(strBuilder.toString());

            this.refreshToken = tokenMap.get("refresh_token");
            this.accessToken = tokenMap.get("access_token");
            this.username = username;

            if (http.getResponseCode() == 200) {
                System.out.println("User Authenticated. Welcome " + this.username);
                this.userRole = getRole();
            } else {
                System.out.println("Authentication failed. User not Found");
            }

            http.disconnect();

        } catch (IOException exception) {
            System.out.println("Login Attempt Failed. Please use the 'ping' command to determine if the S3MP server is available.");
            System.out.println("   Additionally, please try logging in again.");
        }
    }

    public void refreshToken() throws IOException {
        try {
            URL url = new URL("https://localhost:443/oauth/token");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            String authHeader = "Basic " + new String(Base64.getEncoder().encode("s3mp:secret".getBytes()));
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setRequestProperty("Authorization", authHeader);

            String grant_type = URLEncoder.encode("refresh_token", "UTF-8");
            String client_id = URLEncoder.encode("s3mp", "UTF-8");
            String client_secret = URLEncoder.encode("secret", "UTF-8");
            String refresh_token = URLEncoder.encode(this.refreshToken, "UTF-8");

            String postDataParams = "grant_type=" + grant_type + 
                                    "&client_id=" + client_id + 
                                    "&client_secret=" + client_secret +
                                    "&refresh_token=" + refresh_token;

            byte[] out = postDataParams.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            StringBuilder strBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                strBuilder.append(line);
            }

            http.disconnect();

            HashMap<String, String> tokenMap = convertTokenResponseToHashMap(strBuilder.toString());

            this.refreshToken = tokenMap.get("refresh_token");
            this.accessToken = tokenMap.get("access_token");

            System.out.println("Successful OAuth Token Refresh.");
        } catch (IOException exception) {
            System.out.println("Token Refresh Attempt Failed. Please login to receive a valid token.");
        }
    }

    private String getRole() throws MalformedURLException, IOException {

        try {
            URL url = new URL(this.serverAddress + "/roles/" + this.username);

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Accept", "application/json");
            http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(line);
            }

            bufferedReader.close();
            http.disconnect();

            return strBuilder.toString();
        } catch (IOException exception) {
            System.out.println("Role Request Unsuccessful. Please contact your administrator.");
            return "";
        }
    }

    // TODO: Fix Info Printing (It's currently very ugly)
    public void getDownloadInfo() throws MalformedURLException, IOException {

        if (checkToken()) {
            try {
                String lookupVersion = " ";

                System.out.println("");
                System.out.println("Please input the number of the version for which you would like information (ex. 1.0): ");
                lookupVersion = scanner.nextLine();

                URL url = new URL(this.serverAddress + "/download/info/" + lookupVersion);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder strBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                HashMap<String, String> downloadMap = new HashMap<>();
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                for (String str : stringArr) {
                    downloadMap.put(str.substring(0, str.indexOf(":")).replaceAll("\"", ""), (str.substring(str.indexOf(":") + 1)).replaceAll("\"", ""));
                }

                bufferedReader.close();
                
                if (http.getResponseCode() == 200) {
                    System.out.println(downloadMap);
                } else {
                    System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                }

                http.disconnect();
                // this.coreSoftwareSize = Double.valueOf(downloadMap.get("size")).doubleValue();
                // this.coreSoftwareUrl = downloadMap.get("URL");
                // this.currentCoreSoftware = downloadMap.get("software");
                // this.currentCoreSoftwareVersion = downloadMap.get("version");
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            return;
        }
    }

    public void validateDownload() throws IOException, NoSuchAlgorithmException {
        
        if (checkToken()) {
            try {
                URL url = new URL(this.serverAddress + "/download/validate/" + this.currentCoreSoftwareVersion);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder strBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                HashMap<String, String> validationMap = new HashMap<>();
                String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

                for (String str : stringArr) {
                    validationMap.put(str.substring(0, str.indexOf(":")).replaceAll("\"", ""), (str.substring(str.indexOf(":") + 1)).replaceAll("\"", ""));
                }

                bufferedReader.close();

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(this.currentCoreSoftware.getBytes(StandardCharsets.UTF_8));
                String hash = bytesToHex(encodedhash);

                if (http.getResponseCode() == 200) {
                    if (this.coreSoftwareUrl.equals(validationMap.get("URL")) && hash.equals(validationMap.get("Hash")) && this.coreSoftwareSize.equals(Double.valueOf(validationMap.get("Size")).doubleValue())) {
                        System.out.println("Current Installation is Valid. No Action is Required.");
                    } else {
                        System.out.println("Current Installation may be Invalid. Recommended Action: Check with Administrator");
                    }

                } else {
                    System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                }

                http.disconnect();

            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            return;
        }
    }

    public void checkInstalledVersion() throws MalformedURLException, IOException {

        if (checkToken()) {
            try {
                URL url = new URL(this.serverAddress + "/check");

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder strBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuilder.append(line);
                }

                if (http.getResponseCode() == 200) {
                    System.out.println("The current installed core software version is " + this.currentCoreSoftwareVersion);
                    System.out.println("   File Size: " + this.coreSoftwareSize + "Mb || Origin URL: " + this.coreSoftwareUrl);
                    System.out.println("The current version of S3MP Installed on the Server is " + strBuilder.toString());
                } else {
                    System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
                }

                http.disconnect();

            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            return;
        }
    }

    // TODO: Ask Joe what should be done with a download once it is received
    public void initiateDownload() throws MalformedURLException, IOException {

        if (checkToken()) {
            try {
                String downloadVersion = " ";

                System.out.println("");
                System.out.println("Please input the number of the version which you would like to download (ex. 1.0): ");
                downloadVersion = scanner.nextLine();

                URL url = new URL(this.serverAddress + "/download/initialize/" + downloadVersion);

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Authorization", "Bearer " + this.accessToken);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder strBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // strBuilder.append(line);
                    System.out.println(line);
                }

                http.disconnect();
                
            } catch (IOException exception) {
                System.out.println("Request Unsuccessful. Please utilize the 'ping' command to determine if S3MP server is available.");
            }
        } else {
            return;
        }
    }

    public HashMap<String, String> convertTokenResponseToHashMap(String response) {

        HashMap<String, String> tokenMap = new HashMap<>();

        String[] stringArr = response.replaceAll("[\\{\\}\\s]", "").split(",");

        String[] keyValueStringArray;

        for (String str : stringArr) {
            keyValueStringArray = str.split(":");
            tokenMap.put(keyValueStringArray[0].replaceAll("\"", ""), keyValueStringArray[1].replaceAll("\"", ""));
        }

        return tokenMap;
    }

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

    private Boolean checkToken() {
        if ((!"No Token".equals(this.accessToken)) || (!"No Token".equals(this.refreshToken))) {
            return true;
        } else {
            System.out.println("Unauthorized Action. Please login to be Authorized.");
            return false;
        }
    }

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

    public static void main(String[] args) throws Exception {
        Client client = new Client("https://localhost:443", "v1");
    }
}