package com.s3mp.restservice;

// S3MPController.java
// Author: Anders Engman
// Date: 6/3/22
// Description: This controller handles the RESTful functionality which powers S3MP on the server side. While some
//				permissions and data processing work is done here, the server implementation is fairly lightweight.

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.hash.Hashing;
import com.s3mp.config.OAuthUserService;
import com.s3mp.sqlite.CoreSoftware;
import com.s3mp.sqlite.User;
import com.s3mp.utility.TextFileReader;

import org.hibernate.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Required Spring Annotations to power API
@RestController
// Drawn from application.properties file
@RequestMapping("/${s3mp.serverversion}")
public class S3mpController {
	// Drawn from application.properties file
	@Value("${s3mp.serverversion}")
    private String serverVersion;

	// The following services are necessary for different security and db data retrieval functions
	@Autowired
	CoreSoftwareService coreSoftwareService;

	@Autowired
	UserService userService;

	@Autowired
	OAuthUserService oAuthUserService;

	// This routing handles the 'versions' function, returning all relevant versions to the requester
	// Note: this function discriminates on the basis of role so that Generic users will only see stable versions
	// where Experimental users will also see unstable versions.
	@GetMapping("/versions/{role}")
	public List<CoreSoftware> getCoreSoftwareVersions(@PathVariable String role) {
		// pull all core software versions on server
		List<CoreSoftware> coreSoftwareOnServer = (List<CoreSoftware>) coreSoftwareService.findAll();

		// If experimental, show all versions
		if ("EXPERIMENTAL".equals(role)) {
			return coreSoftwareOnServer;
		// Using Java Streams, only show stable versions to Generic users
		} else {
			return (List<CoreSoftware>) coreSoftwareOnServer.stream()
			.filter(cs -> cs.getStability().equals(true))
			.collect(Collectors.toList());
		}
	}

	// This routing handles the 'uptodate' function, returning two fields:
	//		"IsUpToDate:" a boolean which describes if the users' local installation is up to date
	//		"LatestVersion:" the version number of the most up to date core software version on the server
	@GetMapping("/latest/{version}/{role}")
	public Map<String, Object> upToDate(@PathVariable String version, @PathVariable String role) {

		Boolean isUpToDate = false;
		HashMap<String, Object> map = new HashMap<>();

		// Pull all versions of core software on the server
		ArrayList<CoreSoftware> currentSoftwareVersionsOnServer = (ArrayList<CoreSoftware>) coreSoftwareService.findAll();

		CoreSoftware latestCoreSoftware;
		
		// This conditional block features Java streams responsible for returning the appropriate results depending on the users role
		// Again, experimental users will often see a higher version number than generic users
		if ("EXPERIMENTAL".equals(role)) {
			latestCoreSoftware = currentSoftwareVersionsOnServer.stream().max(Comparator.comparing(CoreSoftware::getVersionNumber)).get();
		} else {
			latestCoreSoftware = currentSoftwareVersionsOnServer.stream().filter(coreSoftware -> coreSoftware.getStability() == true).max(Comparator.comparing(CoreSoftware::getVersionNumber)).get();
		}

		// Determine if the users' version is equal to the servers highest version
		try {
			if ( Double.valueOf(version).doubleValue() == latestCoreSoftware.getVersionNumber()) {
				isUpToDate = true;
			}
		} catch (TypeMismatchException typeMismatchException) {
			System.out.println(typeMismatchException);
		}

		// add the two fields to the map
		map.put("IsUpToDate:", isUpToDate);
		map.put("LatestVersion:", latestCoreSoftware.getVersionNumber());

		return map;
	}

	// This routing handles the 'getRoles' internal function, returning a single field 'role'. 
	//		"Role:" using the path variable 'username', the db is queried for users, which are then filtered
	// to return either the users role or 'Role Not Found"'
	@GetMapping("/roles/{username}")
	public String getRole(@PathVariable String username) {

		String role = "Role Not Found";

		// There may or may not be a match so this user is wrapped as an Optional
		Optional<User> targetUser = null;

		// Query DB for All Users
		ArrayList<User> userList = (ArrayList<User>) userService.findAll();

		// Using Java Streams, attempt to filter the users pulled from the db to the user that matches the username
		try {
			targetUser = userList.stream()
					.filter(u -> u.getUsername().equals(username))
					.findAny();
		} catch (Exception exception) {
			System.out.println("Error Finding User");
		}

		// If the targetUser is found, assign the role accordingly
		if (targetUser.isPresent()) {
			role = targetUser.get().getRole();
		}

		return role;
	}

	// This routing handles the 'validateDownload' function, returning three fields:
	//		"Hash:" A SHA256 hash of the core software (or part of it). For example, this mocked system only uses a single line of the sample file
	//		"Size:" A double representing the size of the file in megabytes. In a true implementation, this could be changed easily.
	//		"URL:" A string representing the URL of origin from which the download was sourced
	@GetMapping("/download/validate/{version}")
	public Map<String, Object> validateDownload(@PathVariable String version) {

		HashMap<String, Object> map = new HashMap<>();

		// Pull all core software from the server
		ArrayList<CoreSoftware> currentSoftwareVersionsOnServer = (ArrayList<CoreSoftware>) coreSoftwareService.findAll();

		// Using Java Streams, find the desired core software version
		CoreSoftware relevantCoreSoftware = currentSoftwareVersionsOnServer.stream()
			.filter(coreSoftware -> version.equals(coreSoftware.getVersionNumber().toString()))
			.findAny()
			.orElse(null);

		// Generate the hash using Guava
		String sha256hex = Hashing.sha256().hashString(relevantCoreSoftware.getContents(), StandardCharsets.UTF_8).toString();

		// Add KV pairs to HashMap
		map.put("Hash", sha256hex);
		map.put("Size", relevantCoreSoftware.getFileSize());
		map.put("URL", "https://localhost:9100/v1/download/initialize/" + version);

		return map;
	}

	// This routing handles the 'info' function, returning four fields:
	//		"version:" the version of the core software being queried about by the user
	//		"software:" the mocked contents of the software package. In a genuine implementation, this could be the HEAD of the software or some other identifying aspect of the contents
	//		"URL:" The URL of origin from which the core software package would be downloaded
	//		"size:" the file sixe, a double representing megabytes in this implementation
	@GetMapping("/download/info/{version}")
	public Map<String, Object> initializeDownload(@PathVariable String version) {

		HashMap<String, Object> map = new HashMap<>();

		// Pull all core software from db
		ArrayList<CoreSoftware> currentSoftwareVersionsOnServer = (ArrayList<CoreSoftware>) coreSoftwareService.findAll();

		// Using Java Streams, filter for the desired core software package
		CoreSoftware relevantCoreSoftware = currentSoftwareVersionsOnServer.stream()
			.filter(coreSoftware -> version.equals(coreSoftware.getVersionNumber().toString()))
			.findAny()
			.orElse(null);
		
		// Pull desired data from core software object, return to client
		map.put("version", relevantCoreSoftware.getVersionNumber());
		map.put("software", relevantCoreSoftware.getContents());
		map.put("URL", "https://localhost:9100/v1/download/initialize/" + version);
		map.put("size", relevantCoreSoftware.getFileSize());

		return map;
	}

	// This routing is responsible for the download of core software, in the form of an octet stream
	// As such, the product of the request is a stream containing the core software.
	// For the purposes of this assignment, the data drawn for the stream is comprised of text files which consist of
	// hundreds of lines of randomly generated alphanumeric characters.
	@GetMapping("/download/initialize/{version}")
	public ResponseEntity<InputStreamResource> streamFile(@PathVariable String version) throws IOException {

		String filename;

		// The filename being determined by request. In a genuine implementation this filename should likely be drawn
		// from either a core software record or from a purpose-built db table containing relevant filenames for the
		// various core software versions stored on the db
		if ("1.0".equals(version)) {
			filename = "software1.txt";
		} else if ("1.1".equals(version)) {
			filename = "software2.txt";
		} else if ("1.11".equals(version)) {
			filename = "software3.txt";
		} else {
			filename = "software4.txt";
		}

		// create an inputstream of the selected text file using the proprietary TextFileReader class
		InputStream inputStream = new TextFileReader().readInTextFile("static/mocks/" + filename);

		// For the content-length http header
		int length = inputStream.available();
		// Setting the mediatype http header
		MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

		// Convert to an InputStreamResource for transit
		InputStreamResource resource = new InputStreamResource(inputStream);

		// Return properly prepared octet stream
		return ResponseEntity.ok()
			.contentType(mediaType)
			.contentLength(length)
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
			.body(resource);
	}

	// Integral to the 'check' function, this routing returns the version of S3MP deployed on the server
	// 'ServerVersion' is drawn from the application.properties file and could be updated there if desired.
	@GetMapping("/check")
	public String checkS3MPVersion() {

		return serverVersion;

	}
}