package me.ShakerLP.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.ShakerLP.Functions.Config;

public class mcwebi implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		sender.sendMessage("§8[§aMCWebi§8] §aOn this server running mcwebi!");
		sender.sendMessage("§8[§aMCWebi§8] §ahttps://www.spigotmc.org/resources/mcwebi-multiserver-support-support-mobilebrowser-minecraft-in-the-web-support-1-10.24720/");
		return false;
	}

}
