/*
 * Decompiled with CFR 0_114.
 */
package me.ShakerLP.Functions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarUtils {
    private static boolean RUNNING_FROM_JAR = false;

    static {
        URL resource = JarUtils.class.getClassLoader().getResource("plugin.yml");
        if (resource != null) {
            RUNNING_FROM_JAR = true;
        }
    }

    public static boolean extractFromJar(String fileName, String dest) throws IOException {
        if (JarUtils.getRunningJar() == null) {
            return false;
        }
        File file = new File(dest);
        if (file.isDirectory()) {
            file.mkdir();
            return false;
        }
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        JarFile jar = JarUtils.getRunningJar();
        Enumeration<JarEntry> e = jar.entries();
        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (!je.getName().contains(fileName)) continue;
            BufferedInputStream in = new BufferedInputStream(jar.getInputStream(je));
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            JarUtils.copyInputStream(in, out);
            jar.close();
            return true;
        }
        jar.close();
        return false;
    }

    private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
        try {
            int n;
            byte[] buff = new byte[4096];
            while ((n = in.read(buff)) > 0) {
                out.write(buff, 0, n);
            }
        }
        finally {
            out.flush();
            out.close();
            in.close();
        }
    }

    public static URL getJarUrl(File file) throws IOException {
        return new URL("jar:" + file.toURI().toURL().toExternalForm() + "!/");
    }

    public static JarFile getRunningJar() throws IOException {
        if (!RUNNING_FROM_JAR) {
            return null;
        }
        String path = new File(JarUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();
        path = URLDecoder.decode(path, "UTF-8");
        return new JarFile(path);
    }
}

