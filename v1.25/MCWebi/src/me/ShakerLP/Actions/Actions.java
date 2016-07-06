package me.ShakerLP.Actions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.util.ServerRunner;
import me.ShakerLP.MCWebi;
import me.ShakerLP.Functions.Config;
import me.ShakerLP.Functions.DebugServer;
import me.ShakerLP.Functions.JsonReader;
import me.ShakerLP.Functions.Lag;
import me.ShakerLP.Functions.UnzipUtility;
import me.ShakerLP.Functions.icon;

public class Actions {@
    SuppressWarnings("all")
    public static String Action(Map < String, String > map, IHTTPSession session) throws JSONException {
        String msg = "";
        JSONObject info = null;
        try {
            info = JsonReader.getJson("http://eriks-it.com/mcwebi.php?q=");
            if (!info.getString("version").equals(MCWebi.version)) {
                msg += info.getString("updateinfo");
            }
            info = JsonReader.getJson("http://eriks-it.com/mcwebi.php?q=siteinfo&a=" + map.get("action"));
            if (info.getBoolean("exist")) {
                msg += info.getString("text");
            }
        } catch (IOException e) {
            msg += "<div class='alert alert-warning' role='alert'>Cant connect to MCWebi API!</div>";
        }
        if (session.getCookies().read("server") != null) {
            for (String s: Config.data.getStringList("servers")) {
                if (s.contains(session.getCookies().read("server"))) {
                    String[] sa = s.split(",");
                    try {
                        JSONObject json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=");
                        if (!json.getString("version").equals(MCWebi.version)) {
                            session.getCookies().delete("server");
                            return "<div class='alert alert-danger' role='alert'>Please update MCWebi on the remote server!<br>Please wait ...</div>" + "<meta http-equiv='refresh' content='3; URL=?action='>";
                        }
                        if (!json.getBoolean("access")) {
                            session.getCookies().delete("server");
                            return "<div class='alert alert-danger' role='alert'>Secure key has changed!<br>Please wait ...</div>" + "<meta http-equiv='refresh' content='3; URL=?action='>";
                        }
                    } catch (JSONException | IOException e) {
                        session.getCookies().delete("server");
                        return "<div class='alert alert-danger' role='alert'>Cant connect to the server!<br>Please wait ...</div>" + "<meta http-equiv='refresh' content='3; URL=?action='>";
                    }
                }
            }
        }
        if (!map.isEmpty()) {
            if (map.get("action").equals("")) {
                JSONArray json = null;
                try {
                    json = JsonReader.getJsonArray("http://eriks-it.com/mcwebi.php?q=news");
                    for (int i = json.length() - 1; 0 < i + 1; i--) {
                        JSONObject row = json.getJSONObject(i);
                        String title = row.getString("title");
                        String text = row.getString("text");
                        msg += "<div class='page-header'>" + title + "</div>";
                        msg += text;
                        msg += "<br>";
                    }
                } catch (IOException e) {}
            }
            if (map.get("action").equals("ops")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "ops")) {
	                if (session.getCookies().read("server") == null) {
		            	if(map.containsKey("remove")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "deop "+map.get("remove"));
		                    msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("remove")+"</b> is no longer an op!</div>";
		            	}
		            	if(map.containsKey("add")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "op "+map.get("add"));
		                    msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("add")+"</b> is now an op!</div>";
		            	}
		            	msg += "<h1>OPs</h1>";
		            	msg +="<form method='POST' action='?action=ops'><div class='col-sm-2'><input type='text' name='add' class='form-control' placeholder='Username'></div> <div class='col-sm-1'><input type='submit' value='Add' class='btn btn-success'></div></form><br><br><br>";
		            	msg += "  <table class='table'><thead><tr><th>Name</th><th>Last Played</th><th>Options</th></tr></thead><tbody>";
		            	Set<OfflinePlayer> ops = Bukkit.getOperators();
		                Iterator<OfflinePlayer> itr = ops.iterator();
		                while(itr.hasNext()) {
		                   OfflinePlayer element = itr.next();
		                   Date date = new Date(element.getLastPlayed());
		                   msg += "<tr>"
		                   		+ "<td>"+element.getName()+"</td>";
		                   if(element.getLastPlayed() == 0){
		                	   msg += "<td>Never logged in</td>";
		                   } else {
		                	   msg += "<td>"+date+"</td>";
		                   }
		                   msg += "<td><a href='?action=ops&remove="+element.getName()+"'><img src='"+icon.remove+"'></a></td>"
		                   		+ "</tr>";
		                }
		                msg += "</tbody></table>";
	            	} else {
		            	if(map.containsKey("remove")){
		            		for (String s: Config.data.getStringList("servers")) {
		                        if (s.contains(session.getCookies().read("server"))) {
		                            String[] sa = s.split(",");
		                            try {
		                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=ops&remove="+map.get("remove"));
		                            } catch (JSONException | IOException e) {
		                            }
		                        }
		                    }
		            		msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("remove")+"</b> is no longer an op!</div>";
		            	}
		            	if(map.containsKey("add")){
		            		for (String s: Config.data.getStringList("servers")) {
		                        if (s.contains(session.getCookies().read("server"))) {
		                            String[] sa = s.split(",");
		                            try {
		                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=ops&add="+map.get("add"));
		                            } catch (JSONException | IOException e) {
		                            }
		                        }
		                    }	                    
		            		msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("add")+"</b> is now an op!</div>";
		            	}
	            		JSONObject json = null;
	                    for (String s: Config.data.getStringList("servers")) {
	                        if (s.contains(session.getCookies().read("server"))) {
	                            String[] sa = s.split(",");
	                            try {
	                                json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=ops");
	                            } catch (JSONException | IOException e) {
	                            }
	                        }
	                    }
	                    msg += json.getString("text").replace("{icon_remove}", icon.remove);
	            	}
                }else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("whitelist")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "whitelist")) {
	                if (session.getCookies().read("server") == null) {
		            	if(map.containsKey("remove")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove "+map.get("remove"));
		                    msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("remove")+"</b> is no longer whitelisted!</div>";
		                    Bukkit.reloadWhitelist();
		            	}
		            	if(map.containsKey("add")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add "+map.get("add"));
		                    msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("add")+"</b> is now whitelisted!</div>";
		                    Bukkit.reloadWhitelist();
		            	}
		            	if(map.containsKey("set")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist "+map.get("set"));
		            		Bukkit.reloadWhitelist();
		            	}
		            	msg += "<h1>Whitelist</h1>";
		            	msg +="<form method='POST' action='?action=whitelist'><div class='col-sm-2'><input type='text' name='add' class='form-control' placeholder='Username'></div> <div class='col-sm-1'><input type='submit' value='Add' class='btn btn-success'></div></form>";
		            	if(Bukkit.hasWhitelist()){
		                    msg += "<a href='?action=whitelist&set=off' class='btn btn-info' role='button'>Disable Whitelist</a><br><br><br>";
		            	} else {
		                    msg += "<a href='?action=whitelist&set=on' class='btn btn-info' role='button'>Enable Whitelist</a><br><br><br>";
		            	}
		            	msg += "  <table class='table'><thead><tr><th>Name</th><th>Last Played</th><th>Options</th></tr></thead><tbody>";
		            	Set<OfflinePlayer> ops = Bukkit.getWhitelistedPlayers();
		                Iterator<OfflinePlayer> itr = ops.iterator();
		                while(itr.hasNext()) {
		                   OfflinePlayer element = itr.next();
		                   Date date = new Date(element.getLastPlayed());
		                   msg += "<tr>"
		                   		+ "<td>"+element.getName()+"</td>";
		                   if(element.getLastPlayed() == 0){
		                	   msg += "<td>Never logged in</td>";
		                   } else {
		                	   msg += "<td>"+date+"</td>";
		                   }
		                   msg += "<td><a href='?action=whitelist&remove="+element.getName()+"'><img src='"+icon.remove+"'></a></td>"
		                   		+ "</tr>";
		                }
		                msg += "</tbody></table>";
	            	} else {
		            	if(map.containsKey("remove")){
		            		for (String s: Config.data.getStringList("servers")) {
		                        if (s.contains(session.getCookies().read("server"))) {
		                            String[] sa = s.split(",");
		                            try {
		                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=whitelist&remove="+map.get("remove"));
		                            } catch (JSONException | IOException e) {
		                            }
		                        }
		                    }
		            		msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("remove")+"</b> is now unwhitelisted!</div>";
		            	}
		            	if(map.containsKey("set")){
		            		for (String s: Config.data.getStringList("servers")) {
		                        if (s.contains(session.getCookies().read("server"))) {
		                            String[] sa = s.split(",");
		                            try {
		                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=whitelist&set="+map.get("set"));
		                            } catch (JSONException | IOException e) {
		                            }
		                        }
		                    }
		            	}
		            	if(map.containsKey("add")){
		            		for (String s: Config.data.getStringList("servers")) {
		                        if (s.contains(session.getCookies().read("server"))) {
		                            String[] sa = s.split(",");
		                            try {
		                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=whitelist&add="+map.get("add"));
		                            } catch (JSONException | IOException e) {
		                            }
		                        }
		                    }	                    
		            		msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("add")+"</b> is now whitelisted!</div>";
		            	}
	            		JSONObject json = null;
	                    for (String s: Config.data.getStringList("servers")) {
	                        if (s.contains(session.getCookies().read("server"))) {
	                            String[] sa = s.split(",");
	                            try {
	                                json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=whitelist");
	                            } catch (JSONException | IOException e) {
	                            }
	                        }
	                    }
	                    msg += json.getString("text").replace("{icon_remove}", icon.remove);
	            	}
                }else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("bans")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "bans")) {
	                if (session.getCookies().read("server") == null) {
		            	if(map.containsKey("remove")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon "+map.get("remove"));
		                    msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("remove")+"</b> is now unbanned!</div>";
		            	}
		            	if(map.containsKey("add")){
		            		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban "+map.get("add"));
		                    msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("add")+"</b> is now banned!</div>";
		            	}
		            	msg += "<h1>Bans</h1>";
		            	msg +="<form method='POST' action='?action=bans'><div class='col-sm-2'><input type='text' name='add' class='form-control' placeholder='Username'></div> <div class='col-sm-1'><input type='submit' value='Add' class='btn btn-success'></div></form><br><br><br>";
		            	msg += "  <table class='table'><thead><tr><th>Name</th><th>Last Played</th><th>Options</th></tr></thead><tbody>";
		            	Set<OfflinePlayer> ops = Bukkit.getBannedPlayers();
		                Iterator<OfflinePlayer> itr = ops.iterator();
		                while(itr.hasNext()) {
		                   OfflinePlayer element = itr.next();
		                   Date date = new Date(element.getLastPlayed());
		                   msg += "<tr>"
		                   		+ "<td>"+element.getName()+"</td>";
		                   if(element.getLastPlayed() == 0){
		                	   msg += "<td>Never logged in</td>";
		                   } else {
		                	   msg += "<td>"+date+"</td>";
		                   }
		                   msg += "<td><a href='?action=bans&remove="+element.getName()+"'><img src='"+icon.remove+"'></a></td>"
		                   		+ "</tr>";
		                }
		                msg += "</tbody></table>";
		        	} else {
		            	if(map.containsKey("remove")){
		            		for (String s: Config.data.getStringList("servers")) {
		                        if (s.contains(session.getCookies().read("server"))) {
		                            String[] sa = s.split(",");
		                            try {
		                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=bans&remove="+map.get("remove"));
		                            } catch (JSONException | IOException e) {
		                            }
		                        }
		                    }
		            		msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("remove")+"</b> is now unbanned!</div>";
		            	}
		            	if(map.containsKey("add")){
		            		for (String s: Config.data.getStringList("servers")) {
		                        if (s.contains(session.getCookies().read("server"))) {
		                            String[] sa = s.split(",");
		                            try {
		                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=bans&add="+map.get("add"));
		                            } catch (JSONException | IOException e) {
		                            }
		                        }
		                    }	                    
		            		msg += "<div class='alert alert-success' role='alert'>Player <b>"+map.get("add")+"</b> is now banned!</div>";
		            	}
		        		JSONObject json = null;
		                for (String s: Config.data.getStringList("servers")) {
		                    if (s.contains(session.getCookies().read("server"))) {
		                        String[] sa = s.split(",");
		                        try {
		                            json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=bans");
		                        } catch (JSONException | IOException e) {
		                        }
		                    }
		                }
		                msg += json.getString("text").replace("{icon_remove}", icon.remove);
		        	}
                }else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("status")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "status")) {
                    if (session.getCookies().read("server") == null) {
                        msg += "<div class='row'><div class='col-sm-4'>";
                        msg += "<h1>Host</h1>";
                        msg += "Version: " + Bukkit.getVersion() + "<br>";
                        msg += "RAM: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB<br>";
                        msg += "Disks:<br>";
                        for (Path root: FileSystems.getDefault().getRootDirectories()) {
                            try {
                                FileStore store = Files.getFileStore(root);
                                msg += "- " + root + ": " + (store.getUsableSpace() / 1024) / 1024 / 1024 + " GB of " + (store.getTotalSpace() / 1024) / 1024 / 1024 + " GB<br>";
                            } catch (IOException e) {
                                msg += "Error<br>";
                            }
                        }
                        msg += "</div><div class='col-sm-8'><h1>Status</h1><div class='col-sm-3'>" + "<h4>TPS</h4>" + round(Lag.getTPS(), 3, BigDecimal.ROUND_HALF_UP) + " TPS<br>" + "<progress class='progress' value='" + Lag.getTPS() + "' max='20'></progress>";
                        msg += "</div><div class='col-sm-3'>";
                        msg += "<h4>Ram</h4>" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB<br>" + "<progress class='progress' value='" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "' max='" + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "'></progress>";
                        msg += "</div>";
                        msg += "<div class='col-sm-3'>";
                        msg += "<h4>Online</h4>" + Bukkit.getOnlinePlayers().size() + " Players<br>" + "<progress class='progress' value='" + Bukkit.getOnlinePlayers().size() + "' max='" + Bukkit.getMaxPlayers() + "'></progress>";
                        msg += "</div><a href='?action=status&autorefresh' class='btn btn-info' role='button'>Auto refresh</a></div>";
                        if (session.getParms().containsKey("autorefresh")) {
                            msg += " <meta http-equiv='refresh' content='3' />";
                        }
                    } else {
                        JSONObject json = null;
                        for (String s: Config.data.getStringList("servers")) {
                            if (s.contains(session.getCookies().read("server"))) {
                                String[] sa = s.split(",");
                                try {
                                    json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=status");
                                } catch (JSONException | IOException e) {
                                    msg += "<div class='alert alert-danger' role='alert'>Cannt connect to the server!</div>";
                                }
                            }
                        }
                        msg += "<div class='row'><div class='col-sm-4'>";
                        msg += "<h1>Host</h1>";
                        msg += "Version: " + json.getString("version") + "<br>";
                        msg += "RAM: " + json.getInt("maxram") + " MB<br>";
                        msg += "Disks:<br>";
                        for (Path root: FileSystems.getDefault().getRootDirectories()) {
                            try {
                                FileStore store = Files.getFileStore(root);
                                msg += "- " + root + ": " + (store.getUsableSpace() / 1024) / 1024 / 1024 + " GB of " + (store.getTotalSpace() / 1024) / 1024 / 1024 + " GB<br>";
                            } catch (IOException e) {
                                msg += "Error<br>";
                            }
                        }
                        msg += "</div><div class='col-sm-8'><h1>Status</h1><div class='col-sm-3'>" + "<h4>TPS</h4>" + round(json.getDouble("tps"), 3, BigDecimal.ROUND_HALF_UP) + " TPS<br>" + "<progress class='progress' value='" + Lag.getTPS() + "' max='20'></progress>";
                        msg += "</div><div class='col-sm-3'>";
                        msg += "<h4>Ram</h4>" + json.getInt("usedram") + " MB<br>" + "<progress class='progress' value='" + json.getInt("usedram") + "' max='" + json.getInt("maxram") + "'></progress>";
                        msg += "</div>";
                        msg += "<div class='col-sm-3'>";
                        msg += "<h4>Online</h4>" + json.getInt("online") + " Players<br>" + "<progress class='progress' value='" + json.getInt("online") + "' max='" + json.getInt("maxplayers") + "'></progress>";
                        msg += "</div><a href='?action=status&autorefresh' class='btn btn-info' role='button'>Auto refresh</a></div>";
                        if (session.getParms().containsKey("autorefresh")) {
                            msg += " <meta http-equiv='refresh' content='3' />";
                        }
                    }
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }

            if (map.get("action").equals("bungeecord")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), null, "bungeecord")) {
                    if (map.containsKey("a")) {
                        if (map.get("a").equals("remove")) {
                            List < String > serverla = Config.data.getStringList("servers");
                            for (String s: Config.data.getStringList("servers")) {
                                if (s.contains(map.get("id"))) {
                                    serverla.remove(s);
                                }
                            }
                            Config.data.set("servers", serverla);
                            Config.saveData();
                            msg += "<div class='alert alert-success' role='alert'>Server removed!</div>";
                        }
                    }
                    if (map.containsKey("name")) {
                        JSONObject json = null;
                        try {
                            json = JsonReader.getJson("http://" + map.get("ip") + ":" + map.get("port") + "/?secret=" + map.get("secret") + "");
                            if (json.getBoolean("access")) {
                                if (json.getString("version").equals(MCWebi.version)) {
                                    if (Config.data.contains("servers")) {
                                        String server = DebugServer.randomString(16) + "," + map.get("name") + "," + map.get("ip") + "," + map.get("port") + "," + map.get("secret");
                                        List < String > servers = Config.data.getStringList("servers");
                                        servers.add(server);
                                        Config.data.set("servers", servers);
                                        Config.saveData();
                                        msg += "<div class='alert alert-success' role='alert'>Server added!</div>";
                                    } else {
                                        String server = DebugServer.randomString(16) + "," + map.get("name") + "," + map.get("ip") + "," + map.get("port") + "," + map.get("secret");
                                        List < String > servers = new ArrayList < String > ();
                                        servers.add(server);
                                        Config.data.set("servers", servers);
                                        Config.saveData();
                                        msg += "<div class='alert alert-success' role='alert'>Server added!</div>";
                                    }
                                } else {
                                    msg += "<div class='alert alert-danger' role='alert'>Incompatible version!<br>Host " + MCWebi.getInstance() + " and remote host " + json.getString("version") + "</div>";
                                }
                            } else {
                                msg += "<div class='alert alert-danger' role='alert'>Secure key wrong!</div>";
                            }
                        } catch (JSONException | IOException e) {
                            msg += "<div class='alert alert-danger' role='alert'>Cannt connect to the server!</div>";
                        }
                    }
                    msg += "<h1>Bungee/Multiserver</h1>" + "<form method='POST' action='?action=bungeecord'><div class='col-sm-2'><input type='text' name='name' class='form-control' placeholder='Server Name'></div> <div class='col-sm-2'><input type='text' name='ip' class='form-control' placeholder='IP'></div><div class='col-sm-1'><input type='text' name='port' class='form-control' placeholder='MCWebi Port'></div><div class='col-sm-5'><input type='text' name='secret' class='form-control' placeholder='Secret Code'></div><div class='col-sm-1'><input type='submit' value='Add & Test' class='btn btn-success'></div></form><br><br><br>";
                    msg += "<table class='table'>" + "<thead>" + "<tr>" + "<th>Name</th>" + "<th>IP</th>" + "<th>TPS</th>" + "<th>Ram</th>" + "<th>Online</th>" + "<th>Options</th>" + "</tr>" + "</thead>";
                    if (Config.data.contains("servers")) {
                        for (String s: Config.data.getStringList("servers")) {
                            String[] ser = s.split(",");
                            JSONObject json = null;
                            try {
                                json = JsonReader.getJson("http://" + ser[2] + ":" + ser[3] + "/?secret=" + ser[4] + "&action=status");
                                msg += "<tr>" + "<td>" + ser[1] + "</td>" + "<td>" + ser[2] + "</td>" + "<td>" + round(json.getDouble("tps"), 3, BigDecimal.ROUND_HALF_UP) + "</td>" + "<td>" + json.getInt("usedram") + "/" + json.getInt("maxram") + "</td>" + "<td>" + json.getInt("online") + " Players</td>" + "<td><a href='?action=bungeecord&a=remove&id=" + ser[0] + "'><img src=" + icon.remove + "></a></td>" + "</tr>";
                            } catch (JSONException | IOException e) {
                                msg += "<tr>" + "<td>" + ser[1] + "</td>" + "<td>" + ser[2] + "</td>" + "<td>-</td>" + "<td>-</td>" + "<td>-</td>" + "<td><a href='?action=bungeecord&a=remove&id=" + ser[0] + "'><img src=" + icon.remove + "></a></td>" + "</tr>";
                            }
                        }
                    }
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("remote")) {
                if (map.containsKey("id")) {
                    session.getCookies().set("server", map.get("id"), 6000);
                    msg += "<div class='alert alert-info' role='alert'>Remote...<br>Switching server...</div>" + "<meta http-equiv='refresh' content='0; URL=?action=status'>";
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>Error! Server not found!</div>";
                }
            }

            if (map.get("action").equals("users")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "users")) {
                    if (map.containsKey("name")) {
                        if (map.containsKey("pw")) {
                            if (map.get("pw").length() > 5) {
                                if (Config.data.contains("users")) {
                                    boolean exist = false;
                                    for (String s: Config.data.getStringList("users")) {
                                        if (s.contains(map.get("name"))) {
                                            exist = true;
                                        }
                                    }
                                    if (!exist) {
                                        List < String > users = Config.data.getStringList("users");
                                        String user_builder = map.get("name") + "," + MD5("i love hashing" + map.get("pw").hashCode()) + ",Never logged in";
                                        users.add(user_builder);
                                        Config.data.set("users", users);
                                        msg += "<div class='alert alert-warning' role='alert'>User added!<br>Please set the permissions</div>";
                                        Config.saveData();
                                    } else {
                                        msg += "<div class='alert alert-danger' role='alert'>User already exist!</div>";
                                    }
                                } else {
                                    List < String > users = new ArrayList < String > ();
                                    String user_builder = map.get("name") + "," + map.get("pw") + ",Never logged in";
                                    users.add(user_builder);
                                    Config.data.set("users", users);
                                    msg += "<div class='alert alert-warning' role='alert'>User added!<br>Please set the permissions</div>";
                                    Config.saveData();
                                }
                            } else {
                                msg += "<div class='alert alert-danger' role='alert'>The password must be at least 6 characters!</div>";
                            }
                        } else {
                            msg += "<div class='alert alert-danger' role='alert'>Please enter a password!</div>";
                        }
                    }
                    if (map.containsKey("a")) {
                        if (map.get("a").equals("remove")) {
                            List < String > serverla = Config.data.getStringList("users");
                            for (String s: Config.data.getStringList("users")) {
                                if (s.contains(map.get("user"))) {
                                    serverla.remove(s);
                                }

                            }
                            Config.data.set("users", serverla);
                            Config.data.set("permissions." + map.get("user"), null);
                            Config.saveData();
                            msg += "<div class='alert alert-success' role='alert'>User removed!</div>";
                        }
                        if (map.get("a").equals("save")) {
                            Config.data.set("permissions." + map.get("user"), null);
                            for (Entry < String, String > entry: map.entrySet()) {
                                String key = entry.getKey();
                                if (key.contains("_")) {
                                    String value = entry.getValue();
                                    Config.data.set("permissions." + map.get("user") + "." + key, Boolean.parseBoolean(value));
                                }
                            }
                            msg += "<div class='alert alert-success' role='alert'>Permissions saved!</div>";
                            Config.saveData();
                        }
                        if (map.get("a").equals("edit")) {
                            msg += "<h1>" + map.get("user") + " permissions</h1>" + "<form method='POST' action='?action=users&a=save&user=" + map.get("user") + "'>";
                            msg += "<b>Default Server</b><br>" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "status") + " name='_status' value='true'>&nbsp;Stats</label></span>&nbsp;&nbsp;" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "plugins") + " name='_plugins' value='true'>&nbsp;Plugins</label></span>&nbsp;&nbsp;" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "console") + " name='_console' value='true'>&nbsp;Console</label></span>&nbsp;&nbsp;" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "filebrowser") + " name='_filebrowser' value='true'>&nbsp;Filebrowser</label></span>&nbsp;&nbsp;" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "bungeecord") + " name='_bungeecord' value='true'>&nbsp;Bunge/Multiserver</label></span>&nbsp;&nbsp;" 
                            + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "restart") + " name='_restart' value='true'>&nbsp;Restart</label></span>"
                            + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "ops") + " name='_ops' value='true'>&nbsp;OPs</label></span>"
                            + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "bans") + " name='_bans' value='true'>&nbsp;Bans</label></span>"
                            + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), null, "whitelist") + " name='_whitelist' value='true'>&nbsp;Whitelist</label></span>"
                            + "<br>";
                            for (String s: Config.data.getStringList("servers")) {
                                String[] arsch = s.split(",");
                                msg += "<b>" + arsch[1] + " Server</b><br>" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "status") + " name='" + arsch[0] + "_status' value='true'>&nbsp;Stats</label></span>&nbsp;&nbsp;" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "plugins") + " name='" + arsch[0] + "_plugins' value='true'>&nbsp;Plugins</label></span>&nbsp;&nbsp;" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "console") + " name='" + arsch[0] + "_console' value='true'>&nbsp;Console</label></span>&nbsp;&nbsp;" + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "filebrowser") + " name='" + arsch[0] + "_filebrowser' value='true'>&nbsp;Filebrowser</label></span>&nbsp;&nbsp;" 
                                + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "restart") + " name='" + arsch[0] + "_restart' value='true'>&nbsp;Restart</label></span>" 
                                + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "ops") + " name='" + arsch[0] + "_ops' value='true'>&nbsp;OPs</label></span>" 
                                + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "bans") + " name='" + arsch[0] + "_bans' value='true'>&nbsp;Bans</label></span>" 
                                + "<span class=''><label><input type='checkbox' " + hasPermissionsC(map.get("user"), arsch[0], "whitelist") + " name='" + arsch[0] + "_whitelist' value='true'>&nbsp;Whitelist</label></span>" 
                                + "<br>";
                            }
                            msg += "<input type='submit' value='Save' class='btn btn-success'></form><br>";

                        }
                    }
                    msg += "<h1>Users</h1>" + "<form method='POST' action='?action=users'><div class='col-sm-2'><input type='text' name='name' class='form-control' placeholder='Username'></div> <div class='col-sm-2'><input type='password' name='pw' class='form-control' placeholder='Password'></div><div class='col-sm-1'><input type='submit' value='Add' class='btn btn-success'></div></form><br><br><br>";
                    msg += "<table class='table'>" + "<thead>" + "<tr>" + "<th>Username</th>" + "<th>Last login</th>" + "<th>Options</th>" + "</tr>" + "</thead>";
                    for (String s: Config.data.getStringList("users")) {
                        String[] ser = s.split(",");
                        msg += "<tr>" + "<td>" + ser[0] + "</td><br>" + "<td>" + ser[2] + "</td>" + "<td><a href='?action=users&a=edit&user=" + ser[0] + "'><img src=" + icon.edit + "></a> <a href='?action=users&a=remove&user=" + ser[0] + "'><img src=" + icon.remove + "></a></td>" + "</tr>";

                    }
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>This area is only for the admin!</div>";
                }
            }
            if (map.get("action").equals("cpw")) {
                if (!DebugServer.logged.get(session.getCookies().read("session")).equals(Config.config.getString("username"))) {
                    if (map.containsKey("oldpw")) {
                        if (map.containsKey("newpw")) {
                            if (map.get("newpw").length() > 6) {
                                if (map.get("newpw").equals(map.get("newpwrep"))) {
                                    for (String s: Config.data.getStringList("users")) {
                                        String[] us = s.split(",");
                                        if (us[0].equals(DebugServer.logged.get(session.getCookies().read("session")))) {
                                            if (us[1].equals(MD5("i love hashing" + session.getParms().get("oldpw").hashCode()))) {
                                                System.out.println("[" + session.getRemoteIpAddress() + "] User " + DebugServer.logged.get(session.getCookies().read("session")) + " has changed his password!");
                                                List < String > users = Config.data.getStringList("users");
                                                users.remove(s);
                                                users.add(DebugServer.logged.get(session.getCookies().read("session")) + "," + MD5("i love hashing" + session.getParms().get("newpw").hashCode()) + "," + (new Date()));
                                                Config.data.set("users", users);
                                                Config.saveData();
                                                msg += "<div class='alert alert-success' role='alert'>Your password was now changed!</div>";
                                                break;
                                            } else {
                                                msg += "<div class='alert alert-danger' role='alert'>Thats not your current password!</div>";
                                            }
                                        }
                                    }
                                } else {
                                    msg += "<div class='alert alert-danger' role='alert'>New password and password repeat are not the same!</div>";
                                }
                            } else {
                                msg += "<div class='alert alert-danger' role='alert'>The password must be at least 6 characters!</div>";
                            }
                        }
                    }
                    msg += "<h1>Change Password</h1>" + "<center><form method='POST' style='width:30%'>" + "<div class='form-group'><label for='usr'>Current password:</label><input type='password' class='form-control' value='' name='oldpw'></div>" + "<div class='form-group'><label for='usr'>New password:</label><input type='password' class='form-control' value='' name='newpw'></div>" + "<div class='form-group'><label for='usr'>New password repeat:</label><input type='password' class='form-control' value='' name='newpwrep'></div>" + "<input type='submit' value='Save' class='btn btn-success'></form></center><br>";
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>The admin password must be changed in config.yml!</div>";
                }
            }
            if (map.get("action").equals("plugins")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "plugins")) {
                    String search = "";
                    if (map.containsKey("b")) {
                        if (map.get("b").equals("disable")) {
                            if (session.getCookies().read("server") == null) {
                                Plugin pl = Bukkit.getPluginManager().getPlugin(map.get("name"));
                                if (!pl.getName().equals("MCWebi")) {
                                    pl.getPluginLoader().disablePlugin(pl);
                                }
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + pl.getName() + "</b> is now disabled!</div>";
                            } else {
                                JSONObject json = null;
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=disable&plugin=" + map.get("name"));
                                        } catch (JSONException | IOException e) {

                                        }
                                    }
                                }
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + map.get("name") + "</b> is now disabled!</div>";
                            }
                        }
                        if (map.get("b").equals("remove")) {
                            if (session.getCookies().read("server") == null) {
                                Plugin pl = Bukkit.getPluginManager().getPlugin(map.get("name"));
                                if (!pl.getName().equals("MCWebi")) {
                                    unload(pl);
                                    File f = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(5).replace("%20", " "));
                                    if (!f.delete()) {
                                        File f1 = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(5));
                                        while (f1.delete()) {
                                            f1.delete();
                                        }
                                    }
                                }
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + pl.getName() + "</b> is now removed!</div>";
                            } else {
                                JSONObject json = null;
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=removepl&plugin=" + map.get("name"));
                                        } catch (JSONException | IOException e) {

                                        }
                                    }
                                }
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + map.get("name") + "</b> is now removed!</div>";
                            }
                        }
                        if (map.get("b").equals("enable")) {
                            if (session.getCookies().read("server") == null) {
                                Plugin pl = Bukkit.getPluginManager().getPlugin(map.get("name"));
                                pl.getPluginLoader().enablePlugin(pl);
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + pl.getName() + "</b> is now enabled!</div>";
                            } else {
                                JSONObject json = null;
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=enable&plugin=" + map.get("name"));
                                        } catch (JSONException | IOException e) {

                                        }
                                    }
                                }
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + map.get("name") + "</b> is now enable!</div>";
                            }
                        }
                        if (map.get("b").equals("download")) {
                            if (session.getCookies().read("server") == null) {
                                try {
                                    FileUtils.copyURLToFile(new URL(map.get("url")), new File("plugins/" + map.get("name")));
                                    if (map.get("load").equals("true")) {
                                        load(map.get("name"));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + map.get("name") + "</b> installed!</div>";
                            } else {
                                JSONObject json = null;
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=downloadpl&url=" + map.get("url") + "&name=" + map.get("name") + "&load=" + map.get("load"));
                                        } catch (JSONException | IOException e) {

                                        }
                                    }
                                }
                                msg += "<div class='alert alert-success' role='alert'>Plugin <b>" + map.get("name") + "</b> installed!</div>";
                            }
                        }
                        if (map.get("b").equals("install")) {
                            JSONArray json = null;
                            try {
                                json = JsonReader.getJsonArray("https://api.curseforge.com/servermods/files?projectIds=" + map.get("id"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            search += "<ul class='list-group'>";
                            for (int i = json.length() - 1; 0 < i + 1; i--) {
                                JSONObject row = json.getJSONObject(i);
                                String status = row.getString("releaseType");
                                if (status.equals("release")) {
                                    status = "success";
                                } else
                                if (status.equals("alpha")) {
                                    status = "success";
                                } else
                                if (status.equals("beta")) {
                                    status = "success";
                                } else {
                                    status = "default";
                                }
                                search += "<li class='list-group-item'><span class='label label-" + status + "'>" + row.getString("releaseType") + "</span> " + row.getString("name") + " <br>for " + row.getString("gameVersion") + "" + "<span style='float:right;'><div class='btn-group'><button class='btn btn-default btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Options<span class='caret'></span></button><ul class='dropdown-menu'>" + "<li><a href='?action=plugins&b=download&url=" + row.getString("downloadUrl") + "&name=" + row.getString("fileName") + "&load=true'>Install & Load</a></li>" + "<li><a href='?action=plugins&b=download&url=" + row.getString("downloadUrl") + "&name=" + row.getString("fileName") + "&load=false'>Install</a></li>" + "</ul></div></span>";
                            }
                            search += "</ul>";
                        }
                    }
                    if (map.containsKey("pl")) {
                        JSONArray json = null;
                        try {
                            json = JsonReader.getJsonArray("https://api.curseforge.com/servermods/projects?search=" + map.get("pl").toLowerCase().replace(" ", ""));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        search += "<ul class='list-group'>";
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject row = json.getJSONObject(i);
                            String status = row.getString("stage");
                            if (status.equals("deleted")) {
                                status = "danger";
                            } else
                            if (status.equals("release")) {
                                status = "success";
                            } else
                            if (status.equals("alpha")) {
                                status = "success";
                            } else
                            if (status.equals("beta")) {
                                status = "success";
                            } else
                            if (status.equals("inactive")) {
                                status = "warning";
                            } else {
                                status = "default";
                            }
                            search += "<li class='list-group-item'><span class='label label-" + status + "'>" + row.getString("stage") + "</span> " + row.getString("name") + "" + "<span style='float:right;'><div class='btn-group'><button class='btn btn-default btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Options<span class='caret'></span></button><ul class='dropdown-menu'>" + "<li><a href='http://dev.bukkit.org/bukkit-plugins/" + row.getString("slug") + "' target='_blank'>Bukkit dev page</a></li>" + "<li><a href='?action=plugins&b=install&id=" + row.getInt("id") + "'>Download</a></li>" + "</ul></div></span>";
                        }
                        search += "</ul>";
                    }
                    msg += "<div class='col-sm-5'><h1>Search</h1>" + "<form method='POST' action='?action=plugins'><div class='col-sm-9'><input type='text' name='pl' class='form-control' placeholder='Plugin Name'></div><div class='col-sm-1'>  <input type='submit' value='Search' class='btn btn-success'></div></form><br><br><br>" + search + "</div><div class='col-sm-7'><h1>Installed Plugins</h1><ul class='list-group'>";
                    if (session.getCookies().read("server") == null) {
                        for (Plugin pl: Bukkit.getPluginManager().getPlugins()) {
                            if (pl.isEnabled()) {
                                msg += "<li class='list-group-item'><span class='label label-success'>Enabled</span> " + pl.getName() + " " + "<span style='float:right;'><div class='btn-group'><button class='btn btn-default btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Options<span class='caret'></span></button><ul class='dropdown-menu'>" + "<li><a href='?action=plugins&b=disable&name=" + pl.getName() + "'>Disable</a></li>" + "<li><a href='?action=filebrowser&path=./plugins/" + pl.getName() + "'>Config</a></li>" + "<li><a href='?action=plugins&b=remove&name=" + pl.getName() + "'>Remove</a></li>" + "</ul></div></span>";
                            } else {
                                msg += "<li class='list-group-item'><span class='label label-danger'>Disabled</span> " + pl.getName() + " " + "<span style='float:right;'><div class='btn-group'><button class='btn btn-default btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Options<span class='caret'></span></button><ul class='dropdown-menu'>" + "<li><a href='?action=plugins&b=enable&name=" + pl.getName() + "'>Enable</a></li>" + "<li><a href='?action=filebrowser&path=./plugins/" + pl.getName() + "'>Config</a></li>" + "<li><a href='?action=plugins&b=remove&name=" + pl.getName() + "'>Remove</a></li>" + "</ul></div></span>";
                            }
                        }
                    } else {
                        JSONObject json = null;
                        for (String s: Config.data.getStringList("servers")) {
                            if (s.contains(session.getCookies().read("server"))) {
                                String[] sa = s.split(",");
                                try {
                                    json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=plugins");
                                } catch (JSONException | IOException e) {
                                    msg += "<div class='alert alert-danger' role='alert'>Cannt connect to the server!</div>";
                                }
                            }
                        }
                        String[] pls = json.getString("plugins").split(",");
                        for (String p: pls) {
                            String[] spax = p.split(":");
                            if (spax[1].equals("true")) {
                                msg += "<li class='list-group-item'><span class='label label-success'>Enabled</span> " + spax[0] + " " + "<span style='float:right;'><div class='btn-group'><button class='btn btn-default btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Options<span class='caret'></span></button><ul class='dropdown-menu'>" + "<li><a href='?action=plugins&b=disable&name=" + spax[0] + "'>Disable</a></li>" + "<li><a href='?action=filebrowser&path=./plugins/" + spax[0] + "'>Config</a></li>" + "<li><a href='?action=plugins&b=remove&name=" + spax[0] + "'>Remove</a></li>" + "</ul></div></span>";
                            } else {
                                msg += "<li class='list-group-item'><span class='label label-danger'>Disabled</span> " + spax[0] + " " + "<span style='float:right;'><div class='btn-group'><button class='btn btn-default btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Options<span class='caret'></span></button><ul class='dropdown-menu'>" + "<li><a href='?action=plugins&b=enable&name=" + spax[0] + "'>Enable</a></li>" + "<li><a href='?action=filebrowser&path=./plugins/" + spax[0] + "'>Config</a></li>" + "<li><a href='?action=plugins&b=remove&name=" + spax[0] + "'>Remove</a></li>" + "</ul></div></span>";
                            }
                        }
                    }

                    msg += "</div></div>";
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("updater")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "users")) {
	            	msg += "<h1>Updater</h1>";
	                try {
						info = JsonReader.getJson("http://eriks-it.com/mcwebi.php?q=");
						msg += "<b>Newest version:</b> "+info.getString("version")+"<br><br>";
		                msg += "<b>Default:</b> "+MCWebi.version+"<br>";
					} catch (IOException e) {
	                    msg += "<div class='alert alert-danger' role='alert'>Cant reach MCWebi API!</div>";
					}
	                for (String s: Config.data.getStringList("servers")) {
	                    String[] sa = s.split(",");
	                    try {
                            JSONObject json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3]+"/");
	    	                msg += "<b>"+sa[1]+":</b> "+json.getString("version")+"<br>";
	                    } catch (JSONException | IOException e) {
	    	                msg += "<b>"+sa[1]+":</b> CANT CONNECT<br>";
	                    }
	                }
	                msg +="<br><br><b>Please note! When you using this updater all server was then automaticly restarting with the command restart!</b><br><br>";
	                msg +="<a href='?action=updater&startupdate' class='btn btn-info' role='button'>Start update</a>";
	                if(map.containsKey("startupdate")){
	                    for (String s: Config.data.getStringList("servers")) {
	                        String[] sa = s.split(",");
	                        try {
		                        JSONObject json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=update");
	                        } catch (JSONException | IOException e) {
	                        }
	                    }
	                    if(!Config.config.getBoolean("demo")){
		                	MCWebi.update();
		                	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
	                    }
	                }
                }else {
                    msg += "<div class='alert alert-danger' role='alert'>This area is only for the admin!</div>";
                }
            }
            if(map.get("action").equals("app")){
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "users")) {
	            	msg += "<h1>App</h1>";
	            	msg += "Please note this app is only for android! Please download the app in the google playstore <a href='#'></a><br>"
	            			+ "More infomations in the MCWebi app :)<br><br>";
	            	msg += "<center><img src='https://api.qrserver.com/v1/create-qr-code/?size=250x250&data="+Config.config.getString("ip")+":"+Config.config.getInt("port")+","+Config.config.getString("api_key")+"'></center>";
                }else {
                    msg += "<div class='alert alert-danger' role='alert'>This area is only for the admin!</div>";
                }
            }
            if (map.get("action").equals("filebrowser")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "filebrowser")) {
                    File folder = new File(".");
                    if (map.containsKey("path")) {
                        if (!map.get("path").equals("null")) {
                            folder = new File(map.get("path"));
                        }
                    }
                    if (!Config.config.getString("username").equals(DebugServer.logged.get(session.getCookies().read("session")))) {
                        if (folder.getPath().contains("MCWebi")) {
                            return "<div class='alert alert-danger' role='alert'>You cant as user in the dir MCWebi!</div>";
                        }
                    }
                    if (session.getParms().containsKey("upload")) {
                        msg += "<div class='alert alert-success' role='alert'>Upload successfull!</div>";
                    }
                    if (session.getCookies().read("server") == null) {
                        if (map.containsKey("mode")) {
                            if (map.get("mode").equals("edit")) {
                                File file = new File(map.get("file"));
                                msg += "<h1>Edit " + file.getName() + "</h2>";
                                msg += "<form method='POST' action='?action=filebrowser&mode=save&path=" + map.get("path") + "&file=" + file.getPath() + "'><div class='form-group'><div id='editor_div'>" + readFile(file) + "</div><textarea class='form-control' rows='25' name='text' id='file'>" + readFile(file) + "</textarea></div><input type='submit' value='Save' class='btn btn-success' id='editor_save'></form><br>";
                            }
                            if (map.get("mode").equals("remove")) {
                                File file = new File(map.get("file"));
                                if (!Config.config.getBoolean("demo")) {
                                    if (!file.isDirectory()) {
                                        if (file.delete()) {
                                            msg += "<div class='alert alert-success' role='alert'>File <b>" + file.getName() + "</b> removed!</div>";
                                        } else {
                                            msg += "<div class='alert alert-danger' role='alert'>File <b>" + file.getName() + "</b> cant removed!</div>";
                                        }
                                    } else {
                                        if (deleteDir(file)) {
                                            msg += "<div class='alert alert-success' role='alert'>File <b>" + file.getName() + "</b> removed!</div>";
                                        } else {
                                            msg += "<div class='alert alert-danger' role='alert'>File <b>" + file.getName() + "</b> cant removed!</div>";
                                        }
                                    }
                                } else {
                                    msg += "<div class='alert alert-success' role='alert'>File <b>" + file.getName() + "</b> removed!</div>";
                                }
                            }
                            if (map.get("mode").equals("save")) {
                                File file = new File(map.get("file"));
                                if (!Config.config.getBoolean("demo")) {
                                    try {
                                        FileWriter writer = new FileWriter(file);
                                        writer.write(map.get("text"));
                                        writer.flush();
                                        writer.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                msg += "<div class='alert alert-success' role='alert'>File <b>" + file.getName() + "</b> saved!</div>";
                            }
                            if (map.get("mode").equals("unzip")) {
                                File file = new File(map.get("file"));
                            	UnzipUtility unzipper = new UnzipUtility();
                                try {
                                	if(map.get("path").equals("null")){
                                    	unzipper.unzip(map.get("file"), ".");
                                	} else {
                                    	unzipper.unzip(map.get("file"), map.get("path"));
                                	}
                                    msg += "<div class='alert alert-success' role='alert'>File <b>" + file.getName() + "</b> unziped!</div>";
                                } catch (Exception ex) {
                                    msg += "<div class='alert alert-danger' role='alert'>File <b>" + file.getName() + "</b> cant unziped!</div>";
                                }
                            }
                        }

                        msg += "<span class='label label-default'>Path: " + folder.getPath() + "</span>";
                        msg += "<table class='table'>" + "<thead>" + "<tr>" + "<td>Type</td>" + "<th>Filename</th>" + "<th>Last modify</th>" + "<th>Size</th>" + "<th>Options</th>" + "</tr>" + "</thead>";
                        if (map.containsKey("path")) {
                            msg += "<tr>" + "<td width='20px'><img src='" + icon.back + "'></td>" + "<td><a href='?action=filebrowser&path=" + folder.getPath() + "/..'>..</a></td>" + "<td></td>" + "<td></td>" + "<td></td>" + "</tr>";
                        }
                        File[] listOfFiles = folder.listFiles();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isDirectory()) {
                                msg += "<tr>" + "<td width='20px'><img src='" + icon.folder + "'></td>" + "<td><a href='?action=filebrowser&path=" + listOfFiles[i].getPath() + "'>" + listOfFiles[i].getName() + "</a></td>" + "<td>" + sdf.format(listOfFiles[i].lastModified()) + "</td>" + "<td></td>" + "<td><a href='?action=filebrowser&mode=remove&path=" + map.get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='" + icon.remove + "'></a></td>" + "</tr>";
                            }
                        }

                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isFile()) {
                                msg += "<tr>" + "<td width='20px'><img src='" + icon.file + "'></td>" + "<td>" + listOfFiles[i].getName() + "</td>";
                                msg += "<td>" + sdf.format(listOfFiles[i].lastModified()) + "</td>";
                                if (listOfFiles[i].length() / 1024 / 1024 != 0) {
                                    msg += "<td>" + listOfFiles[i].length() / 1024 / 1024 + " MB</td>";
                                } else if (listOfFiles[i].length() / 1024 != 0) {
                                    msg += "<td>" + listOfFiles[i].length() / 1024 + " KB</td>";
                                } else {
                                    msg += "<td>" + listOfFiles[i].length() + " Byte</td>";
                                }
                                if(listOfFiles[i].getName().contains(".zip")){
                                    msg += "<td><a href='?action=filebrowser&mode=unzip&path=" + map.get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='" + icon.compress + "'></a> <a href='?action=filebrowser&mode=remove&path=" + map.get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='" + icon.remove + "'></a></td>" + "</tr>";
                                } else {
                                	msg += "<td><a href='?action=filebrowser&mode=edit&path=" + map.get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='" + icon.edit + "'></a> <a href='?action=filebrowser&mode=remove&path=" + map.get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='" + icon.remove + "'></a></td>" + "</tr>";
                                }
                            }
                        }

                        msg += "</table>";
                        msg += "<form method='post' enctype='multipart/form-data' action='?action=filebrowser&path=" + session.getParms().get("path") + "&upload=true'><input id='input-1a' type='file' class='file' name='file' data-show-preview='false'></form><br><br>";

                    } else {
                        if (map.containsKey("mode")) {
                            if (map.get("mode").equals("edit")) {
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            JSONObject json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=edit&path=" + map.get("path") + "&file=" + map.get("file"));
                                            msg += json.getString("edit");
                                        } catch (JSONException | IOException e) {}
                                    }
                                }
                            }
                            if (map.get("mode").equals("unzip")) {
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            JSONObject json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=unzip&path=" + map.get("path") + "&file=" + map.get("file"));
                                            if (json.getBoolean("unziped")) {
                                                msg += "<div class='alert alert-success' role='alert'>File <b>" + map.get("file") + "</b> unziped!</div>";
                                            } else {
                                                msg += "<div class='alert alert-danger' role='alert'>File <b>" + map.get("file") + "</b> cant unziped!</div>";
                                            }
                                        } catch (JSONException | IOException e) {}
                                    }
                                }
                            }
                            if (map.get("mode").equals("remove")) {
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            JSONObject json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=remove&file=" + map.get("file"));
                                            if (json.getBoolean("removed")) {
                                                msg += "<div class='alert alert-success' role='alert'>File <b>" + map.get("file") + "</b> removed!</div>";
                                            } else {
                                                msg += "<div class='alert alert-danger' role='alert'>File <b>" + map.get("file") + "</b> cant removed!</div>";
                                            }
                                        } catch (JSONException | IOException e) {}
                                    }
                                }
                            }
                            if (map.get("mode").equals("save")) {
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            JSONObject json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=save&path=" + map.get("path") + "&file=" + map.get("file") + "&text=" + URLEncoder.encode(map.get("text")));
                                            msg += "<div class='alert alert-success' role='alert'>File <b>" + map.get("file") + "</b> saved!</div>";
                                        } catch (JSONException | IOException e) {
                                            e.printStackTrace();
                                            msg += "<div class='alert alert-danger' role='alert'>File <b>" + map.get("file") + "</b> cant saved!</div>";
                                        }
                                    }
                                }
                            }
                        }
                        JSONObject json = null;
                        for (String s: Config.data.getStringList("servers")) {
                            if (s.contains(session.getCookies().read("server"))) {
                                String[] sa = s.split(",");
                                try {
                                    json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=getdir&path=" + URLEncoder.encode(folder.getPath()));
                                    msg += json.getString("dir").replace("{icon_file}", icon.file).replace("{icon_back}", icon.back).replace("{icon_folder}", icon.folder).replace("{icon_remove}", icon.remove).replace("{icon_edit}", icon.edit).replace("{icon_compress}", icon.compress);
                                    msg += "<form method='post' enctype='multipart/form-data' action='?action=filebrowser&path=" + session.getParms().get("path") + "&upload=true'><input id='input-1a' type='file' class='file' name='file' data-show-preview='false'></form><br><br>";
                                } catch (JSONException | IOException e) {}
                            }
                        }
                    }
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("server")) {
                msg += "<h1>Server</h1>" + "<form method='POST'><div class='col-sm-6'>" + "<div class='page-header'><h2>Config</h2></div>" + "<div class='form-group'><label for='usr'>Max Players:</label><input type='text' class='form-control' value='" + Bukkit.getMaxPlayers() + "' name='max-players'></div>" + "<div class='form-group'><label for='usr'>Spawn Protection:</label><input type='text' value='" + Bukkit.getSpawnRadius() + "' class='form-control' name='spawn-protection'></div>" + "<div class='form-group'><label for='usr'>GameMode:</label><input type='text' class='form-control' value='" + Bukkit.getDefaultGameMode().getValue() + "' name='gamemode'></div>" + "<div class='form-group'><label for='usr'>IP:</label><input type='text' class='form-control' value='" + Bukkit.getIp() + "' name='server-ip'></div>" + "<div class='form-group'><label for='usr'>Port:</label><input type='text' class='form-control' value='" + Bukkit.getPort() + "' name='server-port'></div>" + "<div class='form-group'><label for='usr'>View Distance:</label><input type='text' class='form-control' value='" + Bukkit.getViewDistance() + "' name='view-distance'></div>" + "<div class='form-group'><label for='usr'>View Distance:</label><input type='text' class='form-control' name='view-distance'></div>" + "<div class='checkbox-inline'><label><input type='checkbox' value='" + Bukkit.getOnlineMode() + "' name='online-mode'>Online-Mode</label></div>" + "<div class='checkbox-inline'><label><input type='checkbox' value='" + Bukkit.getAllowNether() + "' name='allow-nether'>Allow Nether</label></div>" + "<div class='checkbox-inline'><label><input type='checkbox' value='" + Bukkit.isHardcore() + "' name='hardcore'>Hardcore</label></div>" + "<div class='checkbox-inline'><label><input type='checkbox' value='" + Bukkit.getAllowFlight() + "' name='allow-flight'>Allow Flight</label></div>" + "<div class='checkbox-inline'><label><input type='checkbox' value='" + Bukkit.getGenerateStructures() + "' name='generate-structures'>Generate Structures</label></div>" + "<br><input type='submit' value='Save' class='btn btn-success'><br> <br></div>" + "<form method='POST'><div class='col-sm-6'>" + "<div class='page-header'><h2>Motd</h2></div>" + "<div class='form-group'><label for='usr'>Motd:</label><input type='text' class='form-control' name='motd'></div>" + "<div class='form-group'><label for='usr'>Motd line tow:</label><input type='text' class='form-control' name='motd2'></div>" + "</div></form><br>";
            }
            if (map.get("action").equals("reload")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "restart")) {
                    if (session.getCookies().read("server") == null) {
                        msg += "<h1>Server reload...</h1>" + "Please wait..." + "<meta http-equiv='refresh' content='6; URL=index.html'>";
                        MCWebi.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(MCWebi.getInstance(), new Runnable() {

                            public void run() {
                                if (!Config.config.getBoolean("demo")) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rl");
                                }
                            }
                        }, 20L);
                    } else {
                        msg += "<h1>Server reload...</h1>" + "Please wait..." + "<meta http-equiv='refresh' content='3; URL=web.html?action=remote&id='>";
                        if (!Config.config.getBoolean("demo")) {
                            JSONObject json = null;
                            for (String s: Config.data.getStringList("servers")) {
                                if (s.contains(session.getCookies().read("server"))) {
                                    String[] sa = s.split(",");
                                    try {
                                        json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=runcommand&command=rl");
                                    } catch (JSONException | IOException e) {
                                        msg += "<div class='alert alert-danger' role='alert'>Cannt connect to the server!</div>";
                                    }
                                }
                            }
                        }
                    }
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("logout")) {
                DebugServer.logged.remove(session.getCookies().read("session"));
                msg += "<h1>Logout...</h1>" + "Please wait..." + "<meta http-equiv='refresh' content='3; URL=index.html'>";
            }
            if (map.get("action").equals("stop")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "restart")) {
                    if (session.getCookies().read("server") == null) {
                        msg += "<h1>Server stop...</h1>" + "Please wait..." + "<meta http-equiv='refresh' content='6; URL=index.html'>";
                        MCWebi.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(MCWebi.getInstance(), new Runnable() {

                            public void run() {
                                if (!Config.config.getBoolean("demo")) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                                }
                            }
                        }, 20L);
                    } else {
                        msg += "<h1>Server stop...</h1>" + "Please wait..." + "<meta http-equiv='refresh' content='3; URL=web.html?action=remote&id='>";
                        if (!Config.config.getBoolean("demo")) {
                            JSONObject json = null;
                            for (String s: Config.data.getStringList("servers")) {
                                if (s.contains(session.getCookies().read("server"))) {
                                    String[] sa = s.split(",");
                                    try {
                                        json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=runcommand&command=stop");
                                    } catch (JSONException | IOException e) {
                                        msg += "<div class='alert alert-danger' role='alert'>Cannt connect to the server!</div>";
                                    }
                                }
                            }
                        }
                    }
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            if (map.get("action").equals("console")) {
                if (hasPermissions(DebugServer.logged.get(session.getCookies().read("session")), session.getCookies().read("server"), "console")) {
                    if (session.getCookies().read("server") == null) {
                        if (map.containsKey("command")) {
                            if (!Config.config.getBoolean("demo")) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), map.get("command"));
                            }
                        }
                        List < String > log = new ArrayList<String>();
                        try {
                        	log = getLastNLogLines(new File("logs/latest.log"), 40);
						} catch (Exception e) {
	                        log.add("Soory but the log is to short<br><br>");
						}
                        Collections.reverse(log);
                        String text = "";
                        for (String s: log) {
                            text += s;
                        }
                        text += getLastLineFast(new File("logs/latest.log")) + "<br>";
                        text = text.replaceAll("[\\[\\]][1234567890];[1234567890][1234567890];[1234567890]m", "").replaceAll("[\\[\\]][1234567890];[1234567890][1234567890];[1234567890][1234567890]m", "").replace("[m", "");
                        msg += "<h1>Console</h1><div class='panel panel-default'><div class='panel-body' style='font-size:12px'>" + text + "</div></div>" + "<div class='panel panel-default'><div class='panel-body'>" + "<form method='POST'><div class='col-sm-11'><input type='text' name='command' class='form-control'></div><div class='col-sm-1'>  <input type='submit' value='Send' class='btn btn-success'></div></form>" + "</div></div>";
                    } else {
                        if (map.containsKey("command")) {
                            if (!Config.config.getBoolean("demo")) {
                                JSONObject json = null;
                                for (String s: Config.data.getStringList("servers")) {
                                    if (s.contains(session.getCookies().read("server"))) {
                                        String[] sa = s.split(",");
                                        try {
                                            json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=runcommand&command=" + map.get("command").replace(" ", "%20"));
                                        } catch (JSONException | IOException e) {

                                        }
                                    }
                                }
                            }
                        }
                        String text = "";
                        JSONObject json = null;
                        for (String s: Config.data.getStringList("servers")) {
                            if (s.contains(session.getCookies().read("server"))) {
                                String[] sa = s.split(",");
                                try {
                                    json = JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=getlog");
                                } catch (JSONException | IOException e) {
                                    msg += "<div class='alert alert-danger' role='alert'>Cannt connect to the server!</div>";
                                }
                            }
                        }
                        text = json.getString("log");
                        msg += "<h1>Console</h1><div class='panel panel-default'><div class='panel-body' style='font-size:12px'>" + text + "</div></div>" + "<div class='panel panel-default'><div class='panel-body'>" + "<form method='POST'><div class='col-sm-11'><input type='text' name='command' class='form-control'></div><div class='col-sm-1'>  <input type='submit' value='Send' class='btn btn-success'></div></form>" + "</div></div>";
                    }
                } else {
                    msg += "<div class='alert alert-danger' role='alert'>You dont have permissions for this area!</div>";
                }
            }
            return msg;
        }
        return "<meta http-equiv='refresh' content='0; URL=?action='>";
    }
    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }
    public static List < String > getLastNLogLines(File file, int nLines) {
        int counter = 0;
        List < String > text = new ArrayList < String > ();
        ReversedLinesFileReader object;
        try {
            object = new ReversedLinesFileReader(file);
            while (!object.readLine().isEmpty() && counter < nLines) {
                text.add(object.readLine() + "<br>");
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
    public static String tail(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    /* ignore */
                }
        }
    }
    public static String getLastLineFast(final File file) {
        // file needs to exist
        if (file.exists() == false || file.isDirectory()) {
            return "";
        }

        // avoid empty files
        if (file.length() <= 2) {
            return "";
        }
        Logger loga = Bukkit.getLogger();
        // open the file for read-only mode
        try {@
            SuppressWarnings("resource")
            RandomAccessFile fileAccess = new RandomAccessFile(file, "r");
            char breakLine = '\n';
            // offset of the current filesystem block - start with the last one
            long blockStart = (file.length() - 1) / 4096 * 4096;
            // hold the current block
            byte[] currentBlock = new byte[(int)(file.length() - blockStart)];
            // later (previously read) blocks
            List < byte[] > laterBlocks = new ArrayList < byte[] > ();
            while (blockStart >= 0) {
                fileAccess.seek(blockStart);
                fileAccess.readFully(currentBlock);
                // ignore the last 2 bytes of the block if it is the first one
                int lengthToScan = currentBlock.length - (laterBlocks.isEmpty() ? 2 : 0);
                for (int i = lengthToScan - 1; i >= 0; i--) {
                    if (currentBlock[i] == breakLine) {
                        // we found our end of line!
                        StringBuilder result = new StringBuilder();
                        // RandomAccessFile#readLine uses ISO-8859-1, therefore
                        // we do here too
                        result.append(new String(currentBlock, i + 1, currentBlock.length - (i + 1), "ISO-8859-1"));
                        for (byte[] laterBlock: laterBlocks) {
                            result.append(new String(laterBlock, "ISO-8859-1"));
                        }
                        // maybe we had a newline at end of file? Strip it.
                        if (result.charAt(result.length() - 1) == breakLine) {
                            // newline can be \r\n or \n, so check which one to strip
                            int newlineLength = result.charAt(result.length() - 2) == '\r' ? 2 : 1;
                            result.setLength(result.length() - newlineLength);
                        }
                        return result.toString();
                    }
                }
                // no end of line found - we need to read more
                laterBlocks.add(0, currentBlock);
                blockStart -= 4096;
                currentBlock = new byte[4096];
            }
        } catch (Exception ex) {
            return "Soory an error with reading the log";
        }
        // oops, no line break found or some exception happened
        return "";
    }@
    SuppressWarnings("unchecked")
    public static boolean unload(Plugin plugin) {

        String name = plugin.getName();

        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = null;

        List < Plugin > plugins = null;

        Map < String, Plugin > names = null;
        Map < String, Command > commands = null;
        Map < Event, SortedSet < RegisteredListener >> listeners = null;

        boolean reloadlisteners = true;

        if (pluginManager != null) {

            pluginManager.disablePlugin(plugin);

            try {

                Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
                pluginsField.setAccessible(true);
                plugins = (List < Plugin > ) pluginsField.get(pluginManager);

                Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
                lookupNamesField.setAccessible(true);
                names = (Map < String, Plugin > ) lookupNamesField.get(pluginManager);

                try {
                    Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                    listenersField.setAccessible(true);
                    listeners = (Map < Event, SortedSet < RegisteredListener >> ) listenersField.get(pluginManager);
                } catch (Exception e) {
                    reloadlisteners = false;
                }

                Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commands = (Map < String, Command > ) knownCommandsField.get(commandMap);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null && plugins.contains(plugin))
            plugins.remove(plugin);

        if (names != null && names.containsKey(name))
            names.remove(name);

        if (listeners != null && reloadlisteners) {
            for (SortedSet < RegisteredListener > set: listeners.values()) {
                for (Iterator < RegisteredListener > it = set.iterator(); it.hasNext();) {
                    RegisteredListener value = it.next();
                    if (value.getPlugin() == plugin) {
                        it.remove();
                    }
                }
            }
        }

        if (commandMap != null) {
            for (Iterator < Map.Entry < String, Command >> it = commands.entrySet().iterator(); it.hasNext();) {
                Map.Entry < String, Command > entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        // Attempt to close the classloader to unlock any handles on the plugin's
        // jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();

        if (cl instanceof URLClassLoader) {
            try {
                ((URLClassLoader) cl).close();
            } catch (IOException ex) {}
        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag,
        // but lets try it anyway. This tries to get around the issue where Windows
        // refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();

        return true;

    }
    public static boolean load(String name) {

        Plugin target = null;

        File pluginDir = new File("plugins");

        if (!pluginDir.isDirectory())
            return false;

        File pluginFile = new File(pluginDir, name);

        if (!pluginFile.isFile()) {
            for (File f: pluginDir.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    try {
                        PluginDescriptionFile desc = MCWebi.getInstance().getPluginLoader().getPluginDescription(f);
                        if (desc.getName().equalsIgnoreCase(name)) {
                            pluginFile = f;
                            break;
                        }
                    } catch (InvalidDescriptionException e) {
                        return false;
                    }
                }
            }
        }

        try {
            target = Bukkit.getPluginManager().loadPlugin(pluginFile);
        } catch (InvalidDescriptionException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidPluginException e) {
            e.printStackTrace();
            return false;
        }

        target.onLoad();
        Bukkit.getPluginManager().enablePlugin(target);

        return true;

    }
    private static String readFile(File file) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            try {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }

                return stringBuilder.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "File error";
    }
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty or this is a file so delete it 
        return dir.delete();
    }
    public static Boolean hasPermissions(String user, String server, String site) {
        if (!Config.config.getString("username").equals(user)) {
            if (server == null) {
                server = "";
            }
            if (Config.data.contains("permissions." + user + "." + server + "_" + site)) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }
    public static String hasPermissionsC(String user, String server, String site) {
        if (!Config.config.getString("username").equals(user)) {
            if (server == null) {
                server = "";
            }
            if (Config.data.contains("permissions." + user + "." + server + "_" + site)) {
                return "checked";
            }
        } else {
            return "checked";
        }
        return "";
    }
    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {}
        return null;
    }
}