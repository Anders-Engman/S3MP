package com.s3mp.restservice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.s3mp.config.OAuthUserService;
import com.s3mp.sqlite.CoreSoftware;
import com.s3mp.sqlite.User;

import org.hibernate.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/v1")
public class S3mpController {

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
	// public ArrayList<CoreSoftware> getCoreSoftwareVersions() {
	// 	return (ArrayList<CoreSoftware>) coreSoftwareService.findAll();
	// }

	// @CrossOrigin(origins = "http://localhost:8089")    
    @GetMapping(value = "/{id}")
    public CoreSoftware findOne(@PathVariable Integer id) {
        CoreSoftware coreSoftware = coreSoftwareService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return coreSoftware;
    }

	@GetMapping("/latest/{version}")
	public Map<String, Object> upToDate(@PathVariable String version) {

		Boolean isUpToDate = false;
		HashMap<String, Object> map = new HashMap<>();

		ArrayList<CoreSoftware> currentSoftwareVersionsOnServer = (ArrayList<CoreSoftware>) coreSoftwareService.findAll();

		CoreSoftware latestCoreSoftware = currentSoftwareVersionsOnServer.stream().max(Comparator.comparing(CoreSoftware::getVersionNumber)).get();

		try {
			if ( Float.valueOf(version).floatValue() == latestCoreSoftware.getVersionNumber()) {
				isUpToDate = true;
			}
		} catch (TypeMismatchException typeMismatchException) {
			System.out.println(typeMismatchException);
		}

		map.put("Is Up to Date:", isUpToDate);
		map.put("Latest Version:", latestCoreSoftware.getVersionNumber());

		return map;
	}

	// Change this to point at the oauth/token endpoint and accept a grant_type field
	// may need to also accept refresh_token depending
	// Spring Security handles this automatically
	// @PostMapping(path = "/login", 
    //     consumes = MediaType.APPLICATION_JSON_VALUE, 
    //     produces = MediaType.APPLICATION_JSON_VALUE)
	// public ResponseEntity<String> login(@RequestBody Map<String, String> body) {

	// 	Optional<User> targetUser = null;

	// 	ArrayList<User> userList = (ArrayList<User>) userService.findAll();

	// 	try {
	// 		targetUser = userList.stream()
	// 				.filter(u -> u.getUsername().equals(body.get("username")))
	// 				.findAny();
	// 	} catch (Exception exception) {
	// 		System.out.println("Error Finding User");
	// 	}

	// 	if (targetUser.isPresent()) {
	// 		UserDetails userDetails = oAuthUserService.loadUserByUsername(targetUser.get().getUsername());

	// 		System.out.println("HelLoooo");
	// 		System.out.println(userDetails);
	// 	}

	// 	if (targetUser.isPresent() && body.get("password").equals(targetUser.get().getPassword())) {
	// 		// return new ResponseEntity<>("Login Successful. Welcome " + body.get("username"), HttpStatus.ACCEPTED);
	// 		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	// 	} else {
	// 		// return new ResponseEntity<>("Login Unsuccessful. Please Contact your Administrator", HttpStatus.UNAUTHORIZED);
	// 		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	// 	}
    // }

	// @GetMapping("/users")
	// public List<User> getUsers() {
	// 	return (List<User>) userService.findAll();
	// }

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
}