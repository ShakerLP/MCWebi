package me.ShakerLP.Functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.util.Streams;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.omg.CORBA.Environment;

import fi.iki.elonen.NFUpload;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.util.ServerRunner;
import me.ShakerLP.MCWebi;
import me.ShakerLP.Actions.Actions;

public class DebugServer extends NanoHTTPD {
    public static Map < String, String > logged = new HashMap < String, String > ();
    public static final String
    MIME_PLAINTEXT = "text/plain",
        MIME_HTML = "text/html",
        MIME_JS = "application/javascript",
        MIME_CSS = "text/css",
        MIME_PNG = "image/png",
        MIME_EOT = "font/opentype",
        MIME_WOFF = "font/opentype",
        MIME_TTF = "font/opentype",
        MIME_SVG = "image/svg+xml",
        MIME_JPG = "image/jpg",
        MIME_ico = "image/icon",
        MIME_DEFAULT_BINARY = "application/octet-stream",
        MIME_XML = "text/xml";
    static NFUpload uploader;

    public static void StartServer() {
        ServerRunner.run(DebugServer.class);
        uploader = new NFUpload(new DiskFileItemFactory());

    }

    public DebugServer() {
        super(Config.config.getString("ip"), Config.config.getInt("port"));
    }@
    Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        try {
            if (session.getParms().containsKey("testerror")) {
                String error2 = "";
                for (Plugin p: Bukkit.getPluginManager().getPlugins()) {
                    error2 += p.getName() + ", ";
                }
                String exceptionAsString = "<br><br>Version: " + MCWebi.version + "<br>MC-Version: " + Bukkit.getVersion() + "<br>Plugins: " + error2 + "<br>Time: " + System.currentTimeMillis() + "<br>Parms: " + session.getParms() + "<br>Error:<br>";
                exceptionAsString += "Thats a test error!";
                return newFixedLengthResponse(Status.OK, MIME_HTML, "<img src='http://i1239.photobucket.com/albums/ff512/nat_har/GIFS/SNL%20and%20The%20Lonely%20Island%20GIFS/sorry.gif'><br><br>Please send this error to developer ShakerLP<br>And please restart/reload you plugins!<a href='web.html?action='>Back</a><br>" + exceptionAsString);
            }
            final HashMap < String, String > map = new HashMap < String, String > ();
            String msg = "";
            if (logged.containsKey(session.getCookies().read("session"))) {
                if (session.getParms().containsKey("upload")) {
                    if (!Config.config.getBoolean("demo")) {
                        if (session.getMethod() == Method.POST) {
                            if (session.getCookies().read("server") != null) {
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        Map < String, String > files = new HashMap < > ();
                                        try {
                                            session.parseBody(files);
                                        } catch (IOException e1) {} catch (ResponseException e1) {}
                                        File dst = null;
                                        dst = new File("mcwebi_temp/" + session.getParms().get("file"));
                                        dst.getParentFile().mkdirs();
                                        File src = new File(files.get("file"));
                                        try {
                                            copy(src, dst);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        upload_file("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=upload_file&path=" + session.getParms().get("path").replace("\\", "/"), dst);
                                        dst.delete();
                                    }
                                }
                            } else {
                                Map < String, String > files = new HashMap < > ();
                                try {
                                    session.parseBody(files);
                                } catch (IOException e1) {} catch (ResponseException e1) {}
                                File dst = null;
                                if (session.getParms().get("path").equals("null")) {
                                    dst = new File(session.getParms().get("file"));
                                } else {
                                    dst = new File(session.getParms().get("path") + "/" + session.getParms().get("file"));
                                }
                                File src = new File(files.get("file"));
                                try {
                                    copy(src, dst);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    try {
                        session.parseBody(map);
                    } catch (IOException | ResponseException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    session.parseBody(map);
                } catch (IOException | ResponseException e) {
                    e.printStackTrace();
                }
            }
            if (session.getCookies().read("design") == null) {
                session.getCookies().set("design", "mcwebi", 999999);
            }
            if (session.getParms().containsKey("design")) {
                session.getCookies().set("design", session.getParms().get("design"), 999999);
                msg += "<meta http-equiv='refresh' content='0; URL=index.html'>";
            }
            if (uri.equals("/")) {
                uri = "/index.html";
            }
            InputStream in = this.getClass().getResourceAsStream("/web" + uri);
            if ( in == null) {
                InputStream notfound = this.getClass().getResourceAsStream("/web/404.html");
                return newFixedLengthResponse(Status.OK, MIME_HTML, convertStreamToString(notfound));
            }
            String ac = "";
            msg += convertStreamToString( in );
            boolean error = false;
            if (session.getParms().containsKey("password")) {
                if (session.getParms().containsKey("username")) {
                    if (session.getParms().get("username").equals(Config.config.getString("username"))) {
                        if (session.getParms().get("password").equals(Config.config.getString("password"))) {
                            String rand = randomString(32);
                            session.getCookies().set("session", rand, 900000);
                            logged.put(rand, session.getParms().get("username"));
                            ac += "<div class='alert alert-success' role='alert'>Logged in please wait...!</div>";
                            ac += "<meta http-equiv='refresh' content='3; URL=web.html?action='>";
                            System.out.println("[" + session.getRemoteIpAddress() + "] User " + session.getParms().get("username") + " logged in MCWebi!");
                        } else {
                            error = true;
                        }
                    } else {
                        for (String s: Config.data.getStringList("users")) {
                            String[] us = s.split(",");
                            if (us[0].equals(session.getParms().get("username"))) {
                                if (us[1].equals(Actions.MD5("i love hashing" + session.getParms().get("password").hashCode()))) {
                                    String rand = randomString(32);
                                    session.getCookies().set("session", rand, 900000);
                                    logged.put(rand, session.getParms().get("username"));
                                    ac += "<div class='alert alert-success' role='alert'>Logged in please wait...!</div>";
                                    ac += "<meta http-equiv='refresh' content='3; URL=web.html?action='>";
                                    System.out.println("[" + session.getRemoteIpAddress() + "] User " + session.getParms().get("username") + " logged in MCWebi!");
                                    List < String > users = Config.data.getStringList("users");
                                    users.remove(s);
                                    users.add(session.getParms().get("username") + "," + Actions.MD5("i love hashing" + session.getParms().get("password").hashCode()) + "," + (new Date()));
                                    Config.data.set("users", users);
                                    Config.saveData();
                                    error = false;
                                    break;
                                } else {
                                    error = true;
                                }
                                if (us[1].equals(session.getParms().get("password"))) {
                                    String rand = randomString(32);
                                    session.getCookies().set("session", rand, 900000);
                                    logged.put(rand, session.getParms().get("username"));
                                    ac += "<div class='alert alert-success' role='alert'>Logged in please wait...!</div>";
                                    ac += "<meta http-equiv='refresh' content='3; URL=web.html?action='>";
                                    System.out.println("[" + session.getRemoteIpAddress() + "] User " + session.getParms().get("username") + " logged in MCWebi!");
                                    List < String > users = Config.data.getStringList("users");
                                    users.remove(s);
                                    users.add(session.getParms().get("username") + "," + Actions.MD5("i love hashing" + session.getParms().get("password").hashCode()) + "," + (new Date()));
                                    Config.data.set("users", users);
                                    Config.saveData();
                                    error = false;
                                    break;
                                } else {
                                    error = true;
                                }
                            } else {
                                error = true;
                            }
                        }
                    }
                }
            }
            if (error) {
                System.out.println("[" + session.getRemoteIpAddress() + "] Has tried to log in a password or user name is incorrect!");
                ac += "<div class='alert alert-danger' role='alert'>Error username or password wrong!</div>";
            }
            if (Config.config.getBoolean("demo")) {
                ac += "<div class='alert alert-warning' role='alert'>This site is running on demo mode!</div>";
            }
            if (logged.containsKey(session.getCookies().read("session"))) {
                if (uri.contains(".html")) {
                    String serv = "";
                    ac = Actions.Action(session.getParms(), session);
                    for (String s: Config.data.getStringList("servers")) {
                        String[] str = s.split(",");
                        serv += "<li><a href='?action=remote&id=" + str[0] + "'>" + str[1] + "</a></li>";
                    }
                    msg = msg.replace("{site}", ac);
                    msg = msg.replace("{login}", "<div class='alert alert-danger' role='alert'>You are alredy logged in!<br>Please wait...</div><meta http-equiv='refresh' content='3; URL=web.html?action='>");
                    msg = msg.replace("{server_list}", serv);
                    if (DebugServer.logged.containsKey(session.getCookies().read("session"))) {
                        msg = msg.replace("{username}", DebugServer.logged.get(session.getCookies().read("session")));
                    }
                    if (session.getCookies().read("design") == null) {
                        msg = msg.replace("{css}", "mcwebi");
                    } else {
                        msg = msg.replace("{css}", session.getCookies().read("design"));
                    }
                    if (session.getCookies().read("server") != null) {
                        for (String s: Config.data.getStringList("servers")) {
                            if (s.contains(session.getCookies().read("server"))) {
                                String[] sa = s.split(",");
                                msg = msg.replace("{server}", sa[1]);
                            }
                        }
                    } else {
                        msg = msg.replace("{server}", "Default");
                    }
                    return newFixedLengthResponse(Status.OK, MIME_HTML, msg);
                }
                if (uri.contains(".css")) {
                    return newFixedLengthResponse(Status.OK, MIME_CSS, msg);
                }
                if (uri.contains(".js")) {
                    return newFixedLengthResponse(Status.OK, MIME_JS, msg);
                }
                if (uri.contains(".eot")) {
                    return newFixedLengthResponse(Status.OK, MIME_EOT, msg);
                }
                if (uri.contains(".woff")) {
                    return newFixedLengthResponse(Status.OK, MIME_WOFF, msg);
                }
                if (uri.contains(".ttf")) {
                    return newFixedLengthResponse(Status.OK, MIME_TTF, msg);
                }
                if (uri.contains(".svg")) {
                    return newFixedLengthResponse(Status.OK, MIME_SVG, msg);
                }
                if (uri.contains(".png")) {
                    return newFixedLengthResponse(Status.OK, MIME_PNG, msg);
                }
                if (uri.contains(".jpg")) {
                    return newFixedLengthResponse(Status.OK, MIME_JPG, msg);
                }
                if (uri.contains(".ico")) {
                    return newFixedLengthResponse(Status.OK, MIME_ico, msg);
                }
                if (uri.contains(".zip")) {
                    return newFixedLengthResponse(Status.OK, MIME_DEFAULT_BINARY, msg);
                }
                return newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, msg);
            } else {
                if (session.getCookies().read("design") == null) {
                    msg = msg.replace("{css}", "mcwebi");
                } else {
                    msg = msg.replace("{css}", session.getCookies().read("design"));
                }
                msg = msg.replace("{login}", ac);
                if (uri.contains("web.html")) {
                    return newFixedLengthResponse(Status.FORBIDDEN, MIME_HTML, "Acces Denied<br>Please wait...<meta http-equiv='refresh' content='1; URL=index.html'>");
                } else {
                    if (uri.contains(".html")) {
                        return newFixedLengthResponse(Status.OK, MIME_HTML, msg);
                    }
                    if (uri.contains(".css")) {
                        return newFixedLengthResponse(Status.OK, MIME_CSS, msg);
                    }
                    if (uri.contains(".js")) {
                        return newFixedLengthResponse(Status.OK, MIME_JS, msg);
                    }
                    if (uri.contains(".eot")) {
                        return newFixedLengthResponse(Status.OK, MIME_EOT, msg);
                    }
                    if (uri.contains(".woff")) {
                        return newFixedLengthResponse(Status.OK, MIME_WOFF, msg);
                    }
                    if (uri.contains(".ttf")) {
                        return newFixedLengthResponse(Status.OK, MIME_TTF, msg);
                    }
                    if (uri.contains(".svg")) {
                        return newFixedLengthResponse(Status.OK, MIME_SVG, msg);
                    }
                    if (uri.contains(".png")) {
                        return newFixedLengthResponse(Status.OK, MIME_PNG, msg);
                    }
                    if (uri.contains(".jpg")) {
                        return newFixedLengthResponse(Status.OK, MIME_JPG, msg);
                    }
                    if (uri.contains(".ico")) {
                        return newFixedLengthResponse(Status.OK, MIME_ico, msg);
                    }
                    if (uri.contains(".zip")) {
                        return newFixedLengthResponse(Status.OK, MIME_DEFAULT_BINARY, msg);
                    }
                    return newFixedLengthResponse(Status.OK, MIME_HTML, msg);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String error2 = "";
            for (Plugin p: Bukkit.getPluginManager().getPlugins()) {
                error2 += p.getName() + ", ";
            }
            String exceptionAsString = "<br><br>Version: " + MCWebi.version + "<br>MC-Version: " + Bukkit.getVersion() + "<br>Plugins: " + error2 + "<br>Time: " + System.currentTimeMillis() + "<br>Parms: " + session.getParms() + "<br>Error:<br>";
            exceptionAsString += sw.toString().replace("\n", "<br>");
            return newFixedLengthResponse(Status.OK, MIME_HTML, "<img src='http://i1239.photobucket.com/albums/ff512/nat_har/GIFS/SNL%20and%20The%20Lonely%20Island%20GIFS/sorry.gif'><br><br>Please send this error to developer ShakerLP<br>And please restart/reload you plugins!<a href='web.html?action='>Back</a><br>" + exceptionAsString);
        }
    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in .read(buf)) > 0) {
            out.write(buf, 0, len);
        } in .close();
        out.close();
    }
    public static String upload_file(String url, File file) {
        try {
            FileUpload fileUpload = new FileUpload();
            String response = fileUpload.executeMultiPartRequest(url, file, file.getName(), "file");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}