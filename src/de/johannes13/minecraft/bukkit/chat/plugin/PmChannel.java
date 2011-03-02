package de.johannes13.minecraft.bukkit.chat.plugin;

import org.bukkit.command.CommandSender;
import de.johannes13.minecraft.bukkit.chat.inspircd.IrcUser;

public class PmChannel extends ChannelMetadata {
	final CommandSender target;
	final boolean irc;
	final ChatPlugin plugin;
	
	public PmChannel(ChatPlugin plugin, CommandSender p) {
		this.plugin = plugin;
		target = p;
		irc = p instanceof IrcUser;
	}
	
	
	
	@Override
	public void sendAction(PlayerMetadata src, String message) {
		src.getPlayer().sendMessage("(MSG) * " + src.getPlayer().getDisplayName() + " " + message); 
		if (irc) {
			plugin.ircd.sendAction(src, ((IrcUser) target).getUid(), message);
		} else {
			target.sendMessage("(MSG) * " + src.getPlayer().getDisplayName() + " " + message);
		}
	}
	
	@Override
	public void sendMessage(PlayerMetadata src, String message) {
		src.getPlayer().sendMessage("(MSG) " + src.getPlayer().getDisplayName() + ": " + message); 
		if (irc) {
			plugin.ircd.sendMessage(src, ((IrcUser) target).getUid(), message);
		} else {
			target.sendMessage("(MSG) " + src.getPlayer().getDisplayName() + ": " + message);
		}
	}
}
