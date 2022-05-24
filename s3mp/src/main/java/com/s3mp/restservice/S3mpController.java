package com.s3mp.restservice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.s3mp.sqlite.CoreSoftware;

import org.hibernate.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class S3mpController {

	@Autowired
	CoreSoftwareService coreSoftwareService;

	@GetMapping("/versions")
	public List<CoreSoftware> getCoreSoftwareVersions() {
		return (List<CoreSoftware>) coreSoftwareService.findAll();
	}

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

		for (CoreSoftware coreSoftware : currentSoftwareVersionsOnServer) {
			System.out.println(coreSoftware.getVersionNumber());
		}

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

	@PostMapping(path = "/login", 
        consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = MediaType.APPLICATION_JSON_VALUE)
	// public void credentials(@RequestBody String requestBody) {
	public ResponseEntity<String> login(@RequestBody Map<String, String> body) {

		// System.out.println(body.get("username"));
		// System.out.println(body.get("password"));

		if ("User".equals(body.get("username")) && ("Pass").equals(body.get("password"))) {
			System.out.println("Login Successful. Welcome " + body.get("username"));
		}

		return new ResponseEntity<>("Hello", HttpStatus.OK);
    }
}