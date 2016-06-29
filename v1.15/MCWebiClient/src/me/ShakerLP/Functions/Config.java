package me.ShakerLP.Functions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ShakerLP.MCWebi;

public class Config {
	public static YamlConfiguration config = new YamlConfiguration();
	public static YamlConfiguration data = new YamlConfiguration();
	public static void setupConfig(){
		File file = new File("plugins/MCWebi/config.yml");
		if(file.exists()){
			try {
				config.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			file.getParentFile().mkdirs();
			copy("config.yml", "plugins/MCWebi/config.yml");
			try {
				config.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		File file1 = new File("plugins/MCWebi/data.dont_edit");
		if(file1.exists()){
			try {
				data.load(file1);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			copy("base.dont_edit", "plugins/MCWebi/data.dont_edit");
			try {
				data.load(file1);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void saveData(){
		File file1 = new File("plugins/MCWebi/data.dont_edit");
		try {
			data.save(file1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File file111 = new File("plugins/MCWebi/data.dont_edit");
		if(file111.exists()){
			try {
				data.load(file111);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			copy("base.dont_edit", "plugins/MCWebi/data.dont_edit");
			try {
				data.load(file111);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	
	static void copy(String resource, String destination) {
        InputStream resStreamIn = MCWebi.class.getClassLoader().getResourceAsStream(resource);
        File resDestFile = new File(destination);
        try {
            OutputStream resStreamOut = new FileOutputStream(resDestFile);
            int readBytes;
            byte[] buffer = new byte[1024];
            while ((readBytes = resStreamIn.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }

        } catch (Exception ex) {
        	ex.getStackTrace();
        }

    }

	public static void saveConfig() {
		File file1 = new File("plugins/MCWebi/config.yml");
		try {
			config.save(file1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
