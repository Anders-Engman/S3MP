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
// import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import com.google.common.hash.Hashing;

// import com.google.common.hash.Hashing;

// import com.s3mp.utility.TextFileReader;

import java.nio.charset.StandardCharsets;

public class Client {

    private Scanner scanner;
    private String serverAddress;
    // TODO: Add in Versioning Negotiations
    private String s3mpVersion;
    private String accessToken;
    private String refreshToken;
    private String userRole;
    private String currentCoreSoftware = "kk3wtkNTct";
    private String currentCoreSoftwareVersion = "1.0";
    private String coreSoftwareUrl;
    private Double coreSoftwareSize;

    // private TextFileReader textFileReader;

    public Client(String serverAddress, String s3mpVersion) {

        scanner = new Scanner(System.in);
        this.serverAddress = serverAddress + "/" + s3mpVersion;
        this.coreSoftwareUrl = this.serverAddress + "/download/" + currentCoreSoftwareVersion;
        this.coreSoftwareSize = 325.22;

        System.out.println("\n");
        System.out.println("S3MP " + s3mpVersion + " Client Session Initialized...");

        String userInput = " ";

        while (!"Quit".equalsIgnoreCase(userInput)) {

            System.out.println("Please input a Command:");
            userInput = scanner.nextLine();

            try {
                if ("versions".equalsIgnoreCase(userInput)) {
                    getVersions(this.userRole);
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

                } 
                 else if("validate".equalsIgnoreCase(userInput)) {
                    //  System.out.println(new File(".").getAbsolutePath());
                    // readTextFile("src\\main\\resources\\mocks\\software1.txt");
                    validateDownload();
                 }
                // else {
                //     System.out.println("Command Not Recognized. Please check spelling and syntax.");
                // }
            } catch (IOException ioException) {
                System.out.println(ioException);
            }

            System.out.println("");
        }

        System.out.println("S3MP Client Session Terminated.");

    }

    public void getVersions(String role) throws IOException {

        URL url = new URL(this.serverAddress + "/versions/" + role);

        // HttpURLConnection http = (HttpURLConnection) url.openConnection();
        URLConnection urlConnection = url.openConnection();
        // http.setRequestMethod("GET");
        // http.setRequestProperty("Accept", "application/json");
        // http.setRequestProperty("Authorization", "Bearer " + this.accessToken);
        
        // System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        // http.disconnect();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String inputLine;
        String versionString = "";

        StringBuilder strBuilder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            strBuilder.append(line);
            System.out.println(line);
        }

        // TODO: Clean up printing of versions
        HashMap<String, String> versionMap = convertTokenResponseToHashMap(strBuilder.toString());

        // System.out.println(versionMap);
            
        bufferedReader.close();

        // System.out.println(textFileReader.readInTextFile("location"));
    }

    public void checkIfUpToDate() throws IOException {

        URL url = new URL(this.serverAddress + "/latest/" + this.currentCoreSoftwareVersion);

        URLConnection urlConnection = url.openConnection();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String inputLine;

        while ((inputLine = bufferedReader.readLine()) != null) 
            System.out.println(inputLine);

        bufferedReader.close();
    }

    public static boolean pingServer(String host, int port, int timeout) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {

            return false; 
        }
    }

    // To get token, add "Grant_type: password" to user and pass. point at oauth/token
    // Save both Auth and Refresh token locally. Send Auth in all requests and Refresh only in Refresh flow
    // When using refresh to get new auth token, will need to get a new refresh token as well, save both locally
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

            //Read response
            StringBuilder strBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                strBuilder.append(line);
            }

            HashMap<String, String> tokenMap = convertTokenResponseToHashMap(strBuilder.toString());

            // System.out.println(tokenMap.get(tokenMap.keySet().toArray()[0]));
            // System.out.println(tokenMap.get(tokenMap.keySet().toArray()[5]));

            this.refreshToken = tokenMap.get(tokenMap.keySet().toArray()[0]);

            this.accessToken = tokenMap.get(tokenMap.keySet().toArray()[5]);

            System.out.println(this.accessToken);

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            http.disconnect();

            this.userRole = getRole(username);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public String getRole(String username) throws MalformedURLException, IOException {

        URL url = new URL(this.serverAddress + "/roles/" + username);

        URLConnection urlConnection = url.openConnection();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder strBuilder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            strBuilder.append(line);
        }

        bufferedReader.close();

        return strBuilder.toString();
    }

    
    public void initiateDownload(String version) {
        // TODO: mock download process from server
    }

    public void validateDownload() throws IOException {
        
        URL url = new URL(this.serverAddress + "/download/validate/" + this.currentCoreSoftwareVersion);

        URLConnection urlConnection = url.openConnection();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder strBuilder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            strBuilder.append(line);
        }

        HashMap<String, String> validationMap = new HashMap<>();
        String[] stringArr = strBuilder.toString().replaceAll("[\\{\\}\\s]", "").split(",");

        for (String str : stringArr) {
            validationMap.put(str.substring(0, str.indexOf(":")), str.substring(str.indexOf(":") + 1));
        }

        bufferedReader.close();

        String hash = Hashing.sha256().hashString(this.currentCoreSoftware, StandardCharsets.UTF_8).toString();


        // if (this.coreSoftwareUrl.equals(validationMap.get("URL")) && hash.equals(validationMap.get("Hash")) && this.coreSoftwareSize.equals(validationMap.get(validationMap.keySet().toArray()[1]))) {

        // // }

    }

    public HashMap<String, String> convertTokenResponseToHashMap(String response) {

        HashMap<String, String> tokenMap = new HashMap<>();

        String[] stringArr = response.replaceAll("[\\{\\}\\s]", "").split(",");

        String[] keyValueStringArray;

        for (String str : stringArr) {
            keyValueStringArray = str.split(":");
            tokenMap.put(keyValueStringArray[0], keyValueStringArray[1]);
        }

        return tokenMap;
    }

    public String readTextFile(String location) throws FileNotFoundException, IOException {

        File file = new File(location);
    
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        StringBuilder stringBuilder = new StringBuilder();
    
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            // Print the string
            System.out.println(line);
        }

        bufferedReader.close();

        return stringBuilder.toString();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client("http://localhost:8080", "v1");
    }
}