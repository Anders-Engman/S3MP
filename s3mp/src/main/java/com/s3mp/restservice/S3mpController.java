package com.s3mp.restservice;

// S3MPController.java
// Author: Anders Engman
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

	@GetMapping("/roles/{username}")
	public String getRole(@PathVariable String username) {

		String role = "Role Not Found";

		Optional<User> targetUser = null;

		ArrayList<User> userList = (ArrayList<User>) userService.findAll();

		try {
			targetUser = userList.stream()
					.filter(u -> u.getUsername().equals(username))
					.findAny();
		} catch (Exception exception) {
			System.out.println("Error Finding User");
		}

		if (targetUser.isPresent()) {
			role = targetUser.get().getRole();
		}

		return role;
	}

	@GetMapping("/download/validate/{version}")
	public Map<String, Object> validateDownload(@PathVariable String version) {

		HashMap<String, Object> map = new HashMap<>();

		ArrayList<CoreSoftware> currentSoftwareVersionsOnServer = (ArrayList<CoreSoftware>) coreSoftwareService.findAll();

		CoreSoftware relevantCoreSoftware = currentSoftwareVersionsOnServer.stream()
			.filter(coreSoftware -> version.equals(coreSoftware.getVersionNumber().toString()))
			.findAny()
			.orElse(null);

		String sha256hex = Hashing.sha256().hashString(relevantCoreSoftware.getContents(), StandardCharsets.UTF_8).toString();

		map.put("Hash", sha256hex);
		map.put("Size", relevantCoreSoftware.getFileSize());
		map.put("URL", "http://localhost:9100/v1/download/initialize/" + version);

		return map;
	}

	@GetMapping("/download/info/{version}")
	public Map<String, Object> initializeDownload(@PathVariable String version) {

		HashMap<String, Object> map = new HashMap<>();

		ArrayList<CoreSoftware> currentSoftwareVersionsOnServer = (ArrayList<CoreSoftware>) coreSoftwareService.findAll();

		CoreSoftware relevantCoreSoftware = currentSoftwareVersionsOnServer.stream()
			.filter(coreSoftware -> version.equals(coreSoftware.getVersionNumber().toString()))
			.findAny()
			.orElse(null);
		
		map.put("version", relevantCoreSoftware.getVersionNumber());
		map.put("software", relevantCoreSoftware.getContents());
		map.put("URL", "http://localhost:9100/v1/download/initialize/" + version);
		map.put("size", relevantCoreSoftware.getFileSize());

		return map;
	}

	@GetMapping("/download/initialize/{version}")
	public ResponseEntity<InputStreamResource> streamFile(@PathVariable String version) throws IOException {

		String filename;

		if ("1.0".equals(version)) {
			filename = "software1.txt";
		} else if ("1.1".equals(version)) {
			filename = "software2.txt";
		} else if ("1.11".equals(version)) {
			filename = "software3.txt";
		} else {
			filename = "software4.txt";
		}

		InputStream inputStream = new TextFileReader().readInTextFile("static/mocks/" + filename);

		int length = inputStream.available();
		MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

		InputStreamResource resource = new InputStreamResource(inputStream);

		return ResponseEntity.ok()
			.contentType(mediaType)
			.contentLength(length)
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
			.body(resource);
	}

	@GetMapping("/check")
	public String checkS3MPVersion() {

		return serverVersion;

	}
}