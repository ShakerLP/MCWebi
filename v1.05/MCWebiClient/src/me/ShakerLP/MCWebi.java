package me.ShakerLP;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureRandom;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fi.iki.elonen.util.ServerRunner;
import me.ShakerLP.Functions.Config;
import me.ShakerLP.Functions.DebugServer;
import me.ShakerLP.Functions.Lag;
import me.ShakerLP.Functions.JarUtils;

public class MCWebi extends JavaPlugin{
	public static MCWebi instance;
	public static String version = "1.05";
	public void onEnable(){
		load_libs();
		Config.setupConfig();
		instance = this;
	    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
	    this.getCommand("mcwebi").setExecutor(new me.ShakerLP.commands.mcwebi());
	    if(Config.config.getString("ip").equals("iphere")){
	    	if(!Bukkit.getIp().equals("")){
	    		Config.config.set("ip", Bukkit.getIp());
	    	} else {
	    		Config.config.set("ip", "0.0.0.0");
	    	}
	    }
	    if(Config.config.getString("secret_code").equals("error")){
    		Config.config.set("secret_code", randomString(64));
	    }
	    Config.saveConfig();
	    if(Config.config.getBoolean("metrics")){
	        try {
	            me.ShakerLP.Functions.Metrics metrics = new me.ShakerLP.Functions.Metrics((Plugin)this);
	            metrics.start();
	    		System.out.println("Metrics started!");
	        }
	        catch (IOException metrics) {
	    		System.out.println("Metrics cant start!");
	        }
	    }
		DebugServer.StartServer();
		System.out.println("WebServer started /mcwebi for more infomations!");
	}
	public void onDisable(){
		ServerRunner.web.stop();
	}
	 public void load_libs() {
	        try {
	            File lib;
	            File[] libs;
	            File[] arrfile = libs = new File[]{new File(this.getDataFolder() + "/libs/", "javax.servlet-3.0.0.v201112011016.jar"),new File(this.getDataFolder() + "/libs/", "commons-codec-1.9.jar"), new File(this.getDataFolder() + "/libs/", "commons-fileupload-1.3.1.jar"), new File(this.getDataFolder() + "/libs/", "commons-logging-1.2.jar"), new File(this.getDataFolder() + "/libs/", "fluent-hc-4.5.2.jar"), new File(this.getDataFolder() + "/libs/", "httpclient-4.5.2.jar"), new File(this.getDataFolder() + "/libs/", "httpclient-cache-4.5.2.jar"), new File(this.getDataFolder() + "/libs/", "httpmime-4.5.2.jar"), new File(this.getDataFolder() + "/libs/", "httpcore-4.4.4.jar"), new File(this.getDataFolder() + "/libs/", "jna-4.1.0.jar"), new File(this.getDataFolder() + "/libs/", "jna-platform-4.1.0.jar"), new File(this.getDataFolder() + "/libs/", "org.json-20130603.jar")};
	            int n = arrfile.length;
	            int n2 = 0;
	            while (n2 < n) {
	                lib = arrfile[n2];
	                if (!lib.exists()) {
	                    JarUtils.extractFromJar(lib.getName(), lib.getAbsolutePath());
	                }
	                ++n2;
	            }
	            arrfile = libs;
	            n = arrfile.length;
	            n2 = 0;
	            while (n2 < n) {
	                lib = arrfile[n2];
	                if (!lib.exists()) {
	                    this.getLogger().warning("There was a critical error loading MCWebi :(! Could not find lib: " + lib.getName());
	                    Bukkit.getServer().getPluginManager().disablePlugin((Plugin)this);
	                    return;
	                }
	                this.addClassPath(JarUtils.getJarUrl(lib));
	                ++n2;
	            }
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    private void addClassPath(URL url) throws IOException {
	        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
	        Class<URLClassLoader> sysclass = URLClassLoader.class;
	        try {
	            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
	            method.setAccessible(true);
	            method.invoke(sysloader, url);
	        }
	        catch (Throwable t) {
	            t.printStackTrace();
	            throw new IOException("Error adding " + url + " to system classloader");
	        }
	    }
	    public static MCWebi getInstance(){
	    	return instance;
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