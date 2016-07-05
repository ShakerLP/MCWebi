package me.ShakerLP.Functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.bukkit.Bukkit;
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
import org.json.simple.JSONObject;

import fi.iki.elonen.NFUpload;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.ResponseException;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.util.ServerRunner;
import me.ShakerLP.MCWebi;

public class DebugServer extends NanoHTTPD {
	public static Map<String, String> logged = new HashMap<String, String>();
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
        super(Config.config.getString("ip"),Config.config.getInt("port"));
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();   
        if(session.getParms().containsKey("secret")){
        	if(session.getParms().get("secret").equals(Config.config.getString("secret_code"))){
        		if(session.getParms().containsKey("action")){
        			if(session.getParms().get("action").equals("status")){
                		JSONObject json = new JSONObject();
                		json.put("usedram", (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1024/1024);
                		json.put("maxram", Runtime.getRuntime().totalMemory()/1024/1024);
                		json.put("tps", Lag.getTPS());
                		json.put("online", Bukkit.getOnlinePlayers().size());
                		json.put("version", Bukkit.getVersion());
                		json.put("maxplayers", Bukkit.getMaxPlayers());
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());	        
        			}
        			if(session.getParms().get("action").equals("update")){
        				MCWebi.update();
        				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        			}
        			if(session.getParms().get("action").equals("getlog")){
        				List<String> log = getLastNLogLines(new File("logs/latest.log"), 30);
        				Collections.reverse(log);
        				String text = "";
        				for(String s : log){
        					text += s.replace("[m", "");
        				}
        				text += getLastLineFast(new File("logs/latest.log"))+"<br>";
                		JSONObject json = new JSONObject();
                		json.put("log", text);
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        			}
        			if(session.getParms().get("action").equals("upload_file")){
        				Map<String, String> files = new HashMap<>();
		        	    try { session.parseBody(files); }
		        	    catch (IOException e1) {}
		        	    catch (ResponseException e1) {}
		        	    File dst = null;
		        	    if(session.getParms().get("path").equals("null")){
		        	    	dst = new File(session.getParms().get("file"));
		        	    } else {
		        	    	dst = new File(session.getParms().get("path")+"/"+session.getParms().get("file"));
		        	    }
		        	    File src = new File(files.get("file"));
		        	    try {
		        	          copy(src, dst);
		        	    }catch (Exception e){ e.printStackTrace();}
        			}
        			if(session.getParms().get("action").equals("runcommand")){
        				if(session.getParms().containsKey("command")){
	        				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), session.getParms().get("command"));
	                		JSONObject json = new JSONObject();
	                		json.put("run", true);
	                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        				} else {
	                		JSONObject json = new JSONObject();
	                		json.put("run", false);
	                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        				}
        			}
        			if(session.getParms().get("action").equals("disable")){
        				if(session.getParms().containsKey("plugin")){
        					Plugin pl = Bukkit.getPluginManager().getPlugin(session.getParms().get("plugin"));
        					pl.getPluginLoader().disablePlugin(pl);
        					JSONObject json = new JSONObject();
	                		json.put("disabled", true);
	                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        				} else {
        					JSONObject json = new JSONObject();
	                		json.put("disabled", false);
	                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        				}
        			}
        			if(session.getParms().get("action").equals("enable")){
        				if(session.getParms().containsKey("plugin")){
        					Plugin pl = Bukkit.getPluginManager().getPlugin(session.getParms().get("plugin"));
        					pl.getPluginLoader().enablePlugin(pl);
        					JSONObject json = new JSONObject();
	                		json.put("enable", true);
	                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        				} else {
        					JSONObject json = new JSONObject();
	                		json.put("enable", false);
	                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        				}
        			}
        			if(session.getParms().get("action").equals("getdir")){
        				String msg = "";
        				File folder = new File(".");
        				if(session.getParms().containsKey("path")){
        					if(!session.getParms().get("path").equals("null")){
        						folder = new File(session.getParms().get("path").replace('\\','/'));
        					}
        				}
        				msg +="<span class='label label-default'>Path: "+folder.getPath()+"</span>";
        					msg +="<table class='table'>"
        							+ "<thead>"
        							+ "<tr>"
        			    	  		+ "<td>Type</td>"
        							+ "<th>Filename</th>"
        							+ "<th>Last modify</th>"
        							+ "<th>Size</th>"
        							+ "<th>Options</th>"
        							+ "</tr>"
        							+ "</thead>";
        					if(session.getParms().containsKey("path")){
        			    	  msg += "<tr>"
        			    	  			+ "<td width='20px'><img src='{icon_back}'></td>"
        				    	  		+ "<td><a href='?action=filebrowser&path="+folder.getPath()+"/..'>..</a></td>"
        				    	  		+ "<td></td>"
        				    	  		+ "<td></td>"
        				    	  		+ "<td></td>"
        				    	  		+ "</tr>";
        					}
        					File[] listOfFiles = folder.listFiles();
        					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        					 for (int i = 0; i < listOfFiles.length; i++) {
        						 if (listOfFiles[i].isDirectory()) {
        					    	  msg += "<tr>"
        				    	  			+ "<td width='20px'><img src='{icon_folder}'></td>"
        					    	  		+ "<td><a href='?action=filebrowser&path="+listOfFiles[i].getPath()+"'>"+listOfFiles[i].getName()+"</a></td>"
        					    	  		+ "<td>"+sdf.format(listOfFiles[i].lastModified())+"</td>"
        					    	  		+ "<td></td>"
        					    	  		+ "<td><a href='?action=filebrowser&mode=remove&path="+session.getParms().get("path")+"&file="+listOfFiles[i].getPath()+"'><img src='{icon_remove}'></a></td>"
        					    	  		+ "</tr>";
        					      }
        					    }
        					 
        					    for (int i = 0; i < listOfFiles.length; i++) {
        					      if (listOfFiles[i].isFile()) {
        					    	  msg += "<tr>"
        							    	  	+ "<td width='20px'><img src='{icon_file}'></td>"
        							    	  	+ "<td>"+listOfFiles[i].getName()+"</td>";
        						    	  		msg+= "<td>"+sdf.format(listOfFiles[i].lastModified())+"</td>";
        						    	  		if(listOfFiles[i].length()/1024/1024 != 0){
        						    	  			msg += "<td>"+listOfFiles[i].length()/1024/1024+" MB</td>";
        						    	  		} else if(listOfFiles[i].length()/1024 != 0){
        						    	  			msg += "<td>"+listOfFiles[i].length()/1024+" KB</td>";
        						    	  		} else {
        						    	  			msg += "<td>"+listOfFiles[i].length()+" Byte</td>";
        						    	  		}
        		                                if(listOfFiles[i].getName().contains(".zip")){
        		                                    msg += "<td><a href='?action=filebrowser&mode=unzip&path=" + session.getParms().get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='{icon_compress}'></a> <a href='?action=filebrowser&mode=remove&path=" + session.getParms().get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='{icon_remove}'></a></td>" + "</tr>";
        		                                } else {
        		                                	msg += "<td><a href='?action=filebrowser&mode=edit&path=" + session.getParms().get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='{icon_edit}'></a> <a href='?action=filebrowser&mode=remove&path=" + session.getParms().get("path") + "&file=" + listOfFiles[i].getPath() + "'><img src='{icon_remove}'></a></td>" + "</tr>";
        		                                }
        		                                msg += "</tr>";		     
        					    	  } 
        					    }
        					
        					msg +="</table>";
        					JSONObject json = new JSONObject();
                    		json.put("dir", msg);
                    	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        			}
        			if(session.getParms().get("action").equals("edit")){
        				String msg = "";
        				File file = new File(session.getParms().get("file"));
						msg +="<h1>Edit "+file.getName()+"</h2>";
                        msg += "<form method='POST' action='?action=filebrowser&mode=save&path=" + session.getParms().get("path") + "&file=" + file.getPath() + "'><div class='form-group'><div id='editor_div'>" + readFile(file) + "</div><textarea class='form-control' rows='25' name='text' id='file'>" + readFile(file) + "</textarea></div><input type='submit' value='Save' class='btn btn-success' id='editor_save'></form><br>";
    					JSONObject json = new JSONObject();
                		json.put("edit", msg);
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        			}
        			if (session.getParms().get("action").equals("unzip")) {
        				JSONObject json = new JSONObject();
                        File file = new File(session.getParms().get("file"));
                    	UnzipUtility unzipper = new UnzipUtility();
                        try {
                        	if(session.getParms().get("path").equals("null")){
                            	unzipper.unzip(session.getParms().get("file"), ".");
                        	} else {
                            	unzipper.unzip(session.getParms().get("file"), session.getParms().get("path"));
                        	}
                        	json.put("unziped", true);
                        } catch (Exception ex) {
                        	json.put("unziped", false);
                        }
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
                    }
        			if(session.getParms().get("action").equals("remove")){
        				JSONObject json = new JSONObject();
						File file = new File(session.getParms().get("file"));
						if(!file.isDirectory()){
							if(file.delete()){
								json.put("removed", true);
							} else {
								json.put("removed", false);
							}
						} else {
							if(deleteDir(file)){
								json.put("removed", true);
							} else {
								json.put("removed", false);
							}
						}
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        			}
        			if(session.getParms().get("action").equals("save")){
        				File file = new File(session.getParms().get("file"));
					    try {
							FileWriter writer = new FileWriter(file);
							writer.write(session.getParms().get("text"));
							writer.flush();
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
    					JSONObject json = new JSONObject();
                		json.put("save", true);
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        			}
        			if(session.getParms().get("action").equals("removepl")){
        				Plugin pl = Bukkit.getPluginManager().getPlugin(session.getParms().get("plugin"));
						if(!pl.getName().equals("MCWebi")){
							unload(pl);
					        File f = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(5).replace("%20", " "));
					        if(!f.delete()){
						        File f1 = new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(5));
						        while(f1.delete()){
						        	f1.delete();
						        }
					        }
						}
    					JSONObject json = new JSONObject();
                		json.put("removed", true);
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        			}
        			if(session.getParms().get("action").equals("downloadpl")){
        				try {
							FileUtils.copyURLToFile(new URL(session.getParms().get("url")), new File("plugins/"+session.getParms().get("name")));
							if(session.getParms().get("load").equals("true")){
								load(session.getParms().get("name"));
							}
						} catch (IOException e) {
						}
    					JSONObject json = new JSONObject();
                		json.put("downloaded", true);
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());
        			}
        			if(session.getParms().get("action").equals("plugins")){
                		JSONObject json = new JSONObject();
                		String pls = "";
                		for(Plugin p : Bukkit.getPluginManager().getPlugins()){
                			pls += p.getName()+":"+p.isEnabled()+",";
                		}
                		json.put("plugins", pls);
                	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());	        
        			}
        		}
        		JSONObject json = new JSONObject();
        		json.put("info", "MCWebi Client for BungeeServer Support");
        		json.put("version", MCWebi.version);
        		json.put("access", true);
        	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());	     
        	} else {
        		JSONObject json = new JSONObject();
        		json.put("info", "MCWebi Client for BungeeServer Support");
        		json.put("version", MCWebi.version);
        		json.put("access", false);
        	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, json.toJSONString());	        
        	}
        }
        
        JSONObject empy = new JSONObject();
        empy.put("info", "MCWebi Client for BungeeServer Support");
        empy.put("version", MCWebi.version);
	    return newFixedLengthResponse(Status.OK , MIME_PLAINTEXT, empy.toJSONString());	        
    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

	public static double round(double unrounded, int precision, int roundingMode)
	{
	    BigDecimal bd = new BigDecimal(unrounded);
	    BigDecimal rounded = bd.setScale(precision, roundingMode);
	    return rounded.doubleValue();
	}
	public static List<String> getLastNLogLines(File file, int nLines) {
		int counter = 0; 
		List<String> text = new ArrayList();
		ReversedLinesFileReader object;
		try {
			object = new ReversedLinesFileReader(file);
			while(!object.readLine().isEmpty()  && counter < nLines)
			{
				text.add(object.readLine()+"<br>");
				counter++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}
	public static String tail( File file ) {
	    RandomAccessFile fileHandler = null;
	    try {
	        fileHandler = new RandomAccessFile( file, "r" );
	        long fileLength = fileHandler.length() - 1;
	        StringBuilder sb = new StringBuilder();

	        for(long filePointer = fileLength; filePointer != -1; filePointer--){
	            fileHandler.seek( filePointer );
	            int readByte = fileHandler.readByte();

	            if( readByte == 0xA ) {
	                if( filePointer == fileLength ) {
	                    continue;
	                }
	                break;

	            } else if( readByte == 0xD ) {
	                if( filePointer == fileLength - 1 ) {
	                    continue;
	                }
	                break;
	            }

	            sb.append( ( char ) readByte );
	        }

	        String lastLine = sb.reverse().toString();
	        return lastLine;
	    } catch( java.io.FileNotFoundException e ) {
	        e.printStackTrace();
	        return null;
	    } catch( java.io.IOException e ) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        if (fileHandler != null )
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

	        // open the file for read-only mode
	        try {
	            RandomAccessFile fileAccess = new RandomAccessFile(file, "r");
	            char breakLine = '\n';
	            // offset of the current filesystem block - start with the last one
	            long blockStart = (file.length() - 1) / 4096 * 4096;
	            // hold the current block
	            byte[] currentBlock = new byte[(int) (file.length() - blockStart)];
	            // later (previously read) blocks
	            List<byte[]> laterBlocks = new ArrayList<byte[]>();
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
	                        for (byte[] laterBlock : laterBlocks) {
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
	                ex.printStackTrace();
	        }
	        // oops, no line break found or some exception happened
	        return "";
	    }
	    public static boolean unload(Plugin plugin) {

	        String name = plugin.getName();

	        PluginManager pluginManager = Bukkit.getPluginManager();
	        SimpleCommandMap commandMap = null;

	        List<Plugin> plugins = null;

	        Map<String, Plugin> names = null;
	        Map<String, Command> commands = null;
	        Map<Event, SortedSet<RegisteredListener>> listeners = null;

	        boolean reloadlisteners = true;

	        if (pluginManager != null) {

	            pluginManager.disablePlugin(plugin);

	            try {

	                Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
	                pluginsField.setAccessible(true);
	                plugins = (List<Plugin>) pluginsField.get(pluginManager);

	                Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
	                lookupNamesField.setAccessible(true);
	                names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

	                try {
	                    Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
	                    listenersField.setAccessible(true);
	                    listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
	                } catch (Exception e) {
	                    reloadlisteners = false;
	                }

	                Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
	                commandMapField.setAccessible(true);
	                commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

	                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
	                knownCommandsField.setAccessible(true);
	                commands = (Map<String, Command>) knownCommandsField.get(commandMap);

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
	            for (SortedSet<RegisteredListener> set : listeners.values()) {
	                for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
	                    RegisteredListener value = it.next();
	                    if (value.getPlugin() == plugin) {
	                        it.remove();
	                    }
	                }
	            }
	        }

	        if (commandMap != null) {
	            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
	                Map.Entry<String, Command> entry = it.next();
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
	            } catch (IOException ex) {
	            }
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
	            for (File f : pluginDir.listFiles()) {
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
				reader = new BufferedReader(new FileReader (file));
	        String         line = null;
	        StringBuilder  stringBuilder = new StringBuilder();
	        String         ls = System.getProperty("line.separator");

	        try {
	            while((line = reader.readLine()) != null) {
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
	    public static boolean deleteDir(File dir) 
	    { 
	      if (dir.isDirectory()) 
	    { 
	      String[] children = dir.list(); 
	      for (int i=0; i<children.length; i++)
	      { 
	        boolean success = deleteDir(new File(dir, children[i])); 
	        if (!success) 
	        {  
	          return false; 
	        } 
	      } 
	    }  
	      // The directory is now empty or this is a file so delete it 
	      return dir.delete(); 
	    }
	    public static void copy(File src, File dst) throws IOException {
	        InputStream in = new FileInputStream(src);
	        OutputStream out = new FileOutputStream(dst);

	        // Transfer bytes from in to out
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        in.close();
	        out.close();
	    }
static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
static SecureRandom rnd = new SecureRandom();

public static String randomString( int len ){
   StringBuilder sb = new StringBuilder( len );
   for( int i = 0; i < len; i++ ) 
      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
   return sb.toString();
}
}