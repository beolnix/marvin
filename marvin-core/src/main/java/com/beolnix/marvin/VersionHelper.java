package com.beolnix.marvin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

/**
 * Class provides functionality to extract version number from MANIFEST.MF file
 * Created by beolnix on 29/08/15.
 */
public class VersionHelper {
    private final static String versionLabel = "Implementation-Version";

    private VersionHelper() {}

    /**
     * Method extracts version from the MANIFEST.MF file
     * using versionLabel as an attribute name
     * @return
     */
    public static String getVersion() {
        try {
            Manifest manifest = getManifest();
            String version = manifest.getMainAttributes().getValue(versionLabel);
            if (version != null) {
                return version;
            }
        } catch (IOException e) {
            //nop
        }

        return "UNDETERMINED";
    }

    private static Manifest getManifest() throws IOException {
        URLClassLoader cl = (URLClassLoader) VersionHelper.class.getClassLoader();
        URL url = cl.findResource("META-INF/MANIFEST.MF");
        return new Manifest(url.openStream());
    }
}
