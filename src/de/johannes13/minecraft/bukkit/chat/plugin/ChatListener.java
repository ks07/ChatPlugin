package de.johannes13.minecraft.bukkit.chat.plugin;

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
		event.setCancelled(true);
		PlayerMetadata pm = plugin.getMetadata(event.getPlayer());
		if (pm.getCurrentChannel() == null) return;
		if (pm.getCurrentChannel().equals("")) return;
		plugin.ircd.sendMessage(pm, event.getMessage());
		ChannelMetadata cm = plugin.cmeta.get(pm.getCurrentChannel());
		for(PlayerMetadata t : cm.players) {
			t.getPlayer().sendMessage("[" + cm.publicName + "] " + pm.getPlayer().getDisplayName() + ": " + event.getMessage());
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
		plugin.removePlayer(event.getPlayer());
		plugin.ircd.removePlayer(event.getPlayer(), "Disconnected");
	}
	
	@Override
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) return;
		plugin.removePlayer(event.getPlayer());
		plugin.ircd.removePlayer(event.getPlayer(), "Kicked", "Kicked: " + event.getReason());
	}

}
