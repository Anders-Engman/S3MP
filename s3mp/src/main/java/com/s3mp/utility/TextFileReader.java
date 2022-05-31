package com.s3mp.utility;

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
