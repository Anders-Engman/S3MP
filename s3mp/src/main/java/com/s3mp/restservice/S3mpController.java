package com.s3mp.restservice;

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

@RestController
@RequestMapping("/${s3mp.serverversion}")
public class S3mpController {

	@Value("${s3mp.serverversion}")
    private String serverVersion;

	@Autowired
	CoreSoftwareService coreSoftwareService;

	@Autowired
	UserService userService;

	@Autowired
	OAuthUserService oAuthUserService;

	@GetMapping("/versions/{role}")
	public List<CoreSoftware> getCoreSoftwareVersions(@PathVariable String role) {
		
		List<CoreSoftware> coreSoftwareOnServer = (List<CoreSoftware>) coreSoftwareService.findAll();

		if ("EXPERIMENTAL".equals(role)) {
			return (List<CoreSoftware>) coreSoftwareService.findAll();
		} else {
			return (List<CoreSoftware>) coreSoftwareOnServer.stream()
			.filter(cs -> cs.getStability().equals(true))
			.collect(Collectors.toList());
		}
	}

	@GetMapping("/latest/{version}/{role}")
	public Map<String, Object> upToDate(@PathVariable String version, @PathVariable String role) {

		Boolean isUpToDate = false;
		HashMap<String, Object> map = new HashMap<>();

		ArrayList<CoreSoftware> currentSoftwareVersionsOnServer = (ArrayList<CoreSoftware>) coreSoftwareService.findAll();

		CoreSoftware latestCoreSoftware;
		
		if ("EXPERIMENTAL".equals(role)) {
			latestCoreSoftware = currentSoftwareVersionsOnServer.stream().max(Comparator.comparing(CoreSoftware::getVersionNumber)).get();
		} else {
			latestCoreSoftware = currentSoftwareVersionsOnServer.stream().filter(coreSoftware -> coreSoftware.getStability() == true).max(Comparator.comparing(CoreSoftware::getVersionNumber)).get();
		}

		try {
			if ( Double.valueOf(version).doubleValue() == latestCoreSoftware.getVersionNumber()) {
				isUpToDate = true;
			}
		} catch (TypeMismatchException typeMismatchException) {
			System.out.println(typeMismatchException);
		}

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
		map.put("URL", "http://localhost:8080/v1/download/initialize/" + version);

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
		map.put("URL", "http://localhost:8080/v1/download/initialize/" + version);
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