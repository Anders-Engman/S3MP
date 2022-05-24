package com.s3mp.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
// import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
import java.util.Scanner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Client {

    private Scanner scanner;
    private String serverAddress;
    // TODO: Add in Versioning
    private String s3mpVersion;

    public Client(String serverAddress) {

        scanner = new Scanner(System.in);
        this.serverAddress = serverAddress;

        System.out.println("\n");
        System.out.println("S3MP Client Session Initialized...");
        System.out.println("Please input a Command:");

        String userInput = " ";

        while (!"Quit".equalsIgnoreCase(userInput)) {

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
                } else {
                    System.out.println("Command Not Recognized. Please check spelling and syntax.");
                }
            } catch (IOException ioException) {
                System.out.println(ioException);
            }
        }

        System.out.println("S3MP Client Session Terminated.");

    }

    public void getVersions() throws IOException {

        URL url = new URL(this.serverAddress + "/versions");

        URLConnection urlConnection = url.openConnection();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String inputLine;

        while ((inputLine = bufferedReader.readLine()) != null) 
            System.out.println(inputLine);

        bufferedReader.close();
    }

    public void checkIfUpToDate() throws IOException {

        URL url = new URL(this.serverAddress + "/latest/1.0");

        URLConnection urlConnection = url.openConnection();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String inputLine;

        while ((inputLine = bufferedReader.readLine()) != null) 
            System.out.println(inputLine);

        bufferedReader.close();
    }

    public void login() throws IOException, InterruptedException {
        String username = "User";
        String password = "Pass";
        // Scanner credentialsScanner = new Scanner(System.in);

        // System.out.println("Please Input Username: ");
        // username = credentialsScanner.nextLine();
        // System.out.println("Please Input Password: ");
        // password = credentialsScanner.nextLine();
        // credentialsScanner.close();
        // String putEndpoint = this.serverAddress + "/login";

        URL url = new URL(this.serverAddress + "/login");
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");

        String data = "{\n  \"username\": " + "\"" + username + "\"" + ", \n  \"password\": " + "\"" + password + "\"" + "\n}";

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        http.disconnect();
    }

    public void validateDownload() throws IOException {
        // TODO: Use Guava to produce SHA256 Hash
        //    Example: String sha256hex = Hashing.sha256().hashString(originalString, StandardCharsets.UTF_8).toString();
    }

    
    public static void main(String[] args) throws Exception {
        Client client = new Client("http://localhost:8080");
    }
}