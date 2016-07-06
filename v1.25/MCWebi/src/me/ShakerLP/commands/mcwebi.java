package me.ShakerLP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.ShakerLP.Functions.Config;

public class mcwebi implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		sender.sendMessage("§8[§aMCWebi§8] §aThanks for using my plugin!");
		sender.sendMessage("§8[§aMCWebi§8] §aIn the config can you set a username and password!");
		sender.sendMessage("§8[§aMCWebi§8] §aInterface: http://YOUR-SERVER-IP:"+Config.config.getInt("port"));
		return false;
	}

}
