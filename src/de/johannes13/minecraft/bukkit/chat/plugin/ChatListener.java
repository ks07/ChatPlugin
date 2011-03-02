package de.johannes13.minecraft.bukkit.chat.plugin;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;

public class ChatListener extends PlayerListener {
	
	private final ChatPlugin plugin;
	

	public ChatListener(ChatPlugin chatPlugin) {
		plugin = chatPlugin;
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled()) return;
		PlayerMetadata pm = plugin.getMetadata(event.getPlayer());
		ChannelMetadata cm = pm.getCurrentChannel();
		if (cm != null) {
			cm.sendMessage(pm, event.getMessage());
			event.setCancelled(true);
		} else {
			event.getPlayer().sendMessage(ChatColor.RED + "No channel joined. Join a channel to speak");
		}
	}
	
	@Override
	public void onPlayerJoin(PlayerEvent event) {
		plugin.pluginAddPlayer(event.getPlayer());
		// ok, we need to notify the ircd too :)
		plugin.ircd.addPlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerQuit(PlayerEvent event) {
		// We have first to remove the player from IRC because the plugin cleans up the PlayerMetadate that is required by the IRC to quit.
		plugin.ircd.removePlayer(event.getPlayer(), "Disconnected");
		plugin.removePlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) return;
		plugin.ircd.removePlayer(event.getPlayer(), "Kicked", "Kicked: " + event.getReason());
		plugin.removePlayer(event.getPlayer());
	}

}
