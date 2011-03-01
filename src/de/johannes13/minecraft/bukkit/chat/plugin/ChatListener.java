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
		System.out.println("ChatListener.onPlayerChat() " + String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()));
		event.setCancelled(true);
		PlayerMetadata pm = plugin.getMetadata(event.getPlayer());
		if (pm.getCurrentChannel() == null) return;
		if (pm.getCurrentChannel().equals("")) return;
		plugin.ircd.sendMessage(pm, event.getMessage());
		ChannelMetadata cm = plugin.cmeta.get(pm.getCurrentChannel());
		System.out.println("Message goes to " +cm);
		System.out.println("Public name for " +cm+ "=" + cm.publicName);
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
