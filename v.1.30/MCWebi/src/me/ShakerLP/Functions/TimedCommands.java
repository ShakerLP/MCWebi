package me.ShakerLP.Functions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.json.JSONException;
/*
 * (Coptight) MCWebi by ShakerLP 
 * http://creativecommons.org/licenses/by-nd/4.0/
 */
public class TimedCommands implements Runnable {
	public static Map<String, Integer> commands = new HashMap<String, Integer>();
	@Override
	public void run() {
		final Map<String, Integer> backi = commands;
		for(String s : backi.keySet()){
			if(backi.get(s) < 2){
				String[] st = s.split("###");
				if(st[0].equals("null")){
					commands.put(s, Integer.parseInt(st[2]));
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), st[1]);
				} else {
					commands.put(s, Integer.parseInt(st[2]));
					for (String e: Config.data.getStringList("servers")) {
                        if (s.contains(st[0])) {
                            String[] sa = e.split(",");
                            try {
                                JsonReader.getJson("http://" + sa[2] + ":" + sa[3] + "/?secret=" + sa[4] + "&action=runcommand&command="+st[1].replace(" ", "%20"));
                            } catch (JSONException | IOException eas) {
                            }
                        }
                    }	                    
				}
			} else {
				commands.put(s, (backi.get(s)-1));
			}
		}
	}

}
