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
    // TODO: Add in Versioning Negotiations
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

                    Boolean serverAvailable = pingServer("127.0.0.1", 8080, 2000);

                    if (serverAvailable) {
                        System.out.println("S3MP Server Available.");
                    } else {
                        System.out.println("S3MP Server Unavailable.");
                    }

                } else if("validate".equalsIgnoreCase(userInput)) {
                    //  System.out.println(new File(".").getAbsolutePath());
                    validateDownload();
                } else if ("info".equalsIgnoreCase(userInput)) {
                    getDownloadInfo();
                } else if ("check".equalsIgnoreCase(userInput)) {
                    checkInstalledVersion();
                } else if ("download".equalsIgnoreCase(userInput)) {
                    // initiateDownload();
                } else if ("refresh".equalsIgnoreCase(userInput)) {
                    refreshToken();
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
            return;
        }
    }

    public void checkIfUpToDate() throws IOException {

        if (checkToken()) {
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

            if (updateMap.get("IsUpToDate").equals("true")) {
                System.out.println("You're all set! Version " + this.currentCoreSoftwareVersion + " is installed locally and is the latest version.");
            } else {
                System.out.println("Your current installation is Version " + this.currentCoreSoftwareVersion + " and is not the latest version available.");
                System.out.println("Version " + updateMap.get("LatestVersion") + " is live on the server.");
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

            URL url = new URL("http://localhost:8080/oauth/token");
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
            } else {
                System.out.println("Authentication failed. User not Found");
            }

            http.disconnect();

            this.userRole = getRole();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void refreshToken() throws IOException {
        URL url = new URL("http://localhost:8080/oauth/token");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Accept", "application/json");
        http.setRequestProperty("Authorization", "refresh_token " + this.refreshToken);
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

        HashMap<String, String> tokenMap = convertTokenResponseToHashMap(strBuilder.toString());

        System.out.println(tokenMap);

    }

    private String getRole() throws MalformedURLException, IOException {

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

        return strBuilder.toString();
    }

    
    public void getDownloadInfo() throws MalformedURLException, IOException {

        if (checkToken()) {
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

            System.out.println(downloadMap);

            // this.coreSoftwareSize = Double.valueOf(downloadMap.get("size")).doubleValue();
            // this.coreSoftwareUrl = downloadMap.get("URL");
            // this.currentCoreSoftware = downloadMap.get("software");
            // this.currentCoreSoftwareVersion = downloadMap.get("version");
        } else {
            return;
        }
    }

    public void validateDownload() throws IOException, NoSuchAlgorithmException {
        
        if (checkToken()) {
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
                validationMap.put(str.substring(0, str.indexOf(":")), (str.substring(str.indexOf(":") + 1)).replaceAll("\"", ""));
            }

            bufferedReader.close();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(this.currentCoreSoftware.getBytes(StandardCharsets.UTF_8));
            String hash = bytesToHex(encodedhash);

            if (this.coreSoftwareUrl.equals(validationMap.get(validationMap.keySet().toArray()[0])) && hash.equals(validationMap.get(validationMap.keySet().toArray()[2])) && this.coreSoftwareSize.equals(Double.valueOf(validationMap.get(validationMap.keySet().toArray()[1])).doubleValue())) {
                System.out.println("Current Installation is Valid. No Action is Required.");
            } else {
                System.out.println("Current Installation may be Invalid. Recommended Action: Check with Administrator");
            }
        } else {
            return;
        }
    }

    public void checkInstalledVersion() throws MalformedURLException, IOException {

        if (checkToken()) {
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

            System.out.println("The current installed core software version is " + this.currentCoreSoftwareVersion);
            System.out.println("   File Size: " + this.coreSoftwareSize + "Mb || Origin URL: " + this.coreSoftwareUrl);
            System.out.println("The current version of S3MP Installed on the Server is " + strBuilder.toString());
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

    public static void main(String[] args) throws Exception {
        Client client = new Client("http://localhost:8080", "v1");
    }
}