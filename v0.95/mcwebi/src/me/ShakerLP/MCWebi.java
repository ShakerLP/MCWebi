package me.ShakerLP;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

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
	public static String version = "0.95";
	public void onEnable(){
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
	    public static MCWebi getInstance(){
	    	return instance;
	    }
}