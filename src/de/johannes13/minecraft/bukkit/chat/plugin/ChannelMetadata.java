package de.johannes13.minecraft.bukkit.chat.plugin;

import java.util.ArrayList;
import java.util.List;

import de.johannes13.minecraft.bukkit.chat.inspircd.IrcUser;

public class ChannelMetadata {
	
	String name = "";
	String ircRelay = null;
	List<PlayerMetadata> players = new ArrayList<PlayerMetadata>();
	String publicName = "";
	String ts = null;
	boolean hidden;
	int priority;
	List<IrcUser> ircuser = new ArrayList<IrcUser>();
	private ChatPlugin plugin;
	
	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the ts
	 */
	public String getTs() {
		return ts;
	}

	/**
	 * @param ts the ts to set
	 */
	public void setTs(String ts) {
		System.out.println("ChannelMetadata.setTs(" + ts + ") " + this);
		this.ts = ts;
	}

	public ChannelMetadata() {
	}

	/**
	 * @param name
	 * @param ircRelay
	 * @param autojoin
	 * @param allow
	 * @param publicName
	 */
	public ChannelMetadata(ChatPlugin p, String name, String ircRelay, String publicName, boolean hidden, int priority) {
		this.plugin = p;
		this.name = name;
		this.ircRelay = ircRelay;
		this.publicName = publicName;
		this.hidden = hidden;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ircRelay
	 */
	public String getIrcRelay() {
		return ircRelay;
	}

	/**
	 * @param ircRelay the ircRelay to set
	 */
	public void setIrcRelay(String ircRelay) {
		this.ircRelay = ircRelay;
	}

	/**
	 * @return the publicName
	 */
	public String getPublicName() {
		return publicName;
	}

	/**
	 * @param publicName the publicName to set
	 */
	public void setPublicName(String publicName) {
		this.publicName = publicName;
	}

	/**
	 * @return the players
	 */
	public List<PlayerMetadata> getPlayers() {
		return players;
	}

	/**
	 * @param players the players to set
	 */
	public void setPlayers(List<PlayerMetadata> players) {
		this.players = players;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public String toString() {
		return "Channel("+name+")";
	}
	
	public void sendMessage(PlayerMetadata src, String message) {
		for(PlayerMetadata t : players) {
			t.getPlayer().sendMessage("[" + publicName + "] " + src.getPlayer().getDisplayName() + ": " + message);
		}
		if (plugin.ircd.isValid(ircRelay)) { 
			plugin.ircd.sendMessage(src, ircRelay, message);
		}
	}
	// msg from IRC
	public void sendMessage(String src, String message) {
		for(PlayerMetadata t : players) {
			t.getPlayer().sendMessage("[" + ircRelay + "] " + src + ": " + message);
		}
	}
	
	public void sendAction(PlayerMetadata src, String message) {
		for(PlayerMetadata t : players) {
			t.getPlayer().sendMessage("[" + publicName + "] * " + src.getPlayer().getDisplayName() + " " + message);
		}
		if (plugin.ircd.isValid(ircRelay)) { 
			plugin.ircd.sendAction(src, ircRelay, message);
		}
	}
	
	public void sendAction(String src, String message) {
		for(PlayerMetadata t : players) {
			t.getPlayer().sendMessage("[" + ircRelay + "] * " + src + " " + message);
		}
	}

}
