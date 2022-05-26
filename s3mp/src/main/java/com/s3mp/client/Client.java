package com.s3mp.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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

    public Client(String serverAddress, String s3mpVersion) {

        scanner = new Scanner(System.in);
        this.serverAddress = serverAddress + "/" + s3mpVersion;

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

    public void getVersions() throws IOException {

        URL url = new URL(this.serverAddress + "/versions");

        URLConnection urlConnection = url.openConnection();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String inputLine;
        String versionString = "";
        

        while ((inputLine = bufferedReader.readLine()) != null) {
            System.out.println(inputLine);

            if (inputLine != null) {
                versionString = inputLine;
            }
        }

        System.out.println("Hello " + versionString);
            
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

    public static boolean pingServer(String host, int port, int timeout) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {

            return false; 
        }
    }

    public void login() throws IOException, InterruptedException {
        String username = "JohnGeneric";
        String password = "pass";

        System.out.println("");
        System.out.println("Please Input Username: ");
        username = scanner.nextLine();
        System.out.println("Please Input Password: ");
        password = scanner.nextLine();

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
        Client client = new Client("http://localhost:8080", "v1");
    }
}