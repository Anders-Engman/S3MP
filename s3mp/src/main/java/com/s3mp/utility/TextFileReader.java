package com.s3mp.utility;

// TextFileReader.java
// Author: Anders Engman
// Date: 6/3/22
// This class reads static files from the resources directory. This is used to stream txt files as mocked core software
// downloads.

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class TextFileReader {

    public InputStream readInTextFile(String location) throws IOException {
		return getClass().getClassLoader().getResourceAsStream(location);
    }
}
