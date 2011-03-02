package de.johannes13.minecraft.bukkit.chat.plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import de.johannes13.minecraft.bukkit.chat.inspircd.Ircd;

public class PlayerMetadata {
	
	private final Player player;
	private final List<String> channels = new ArrayList<String>();
	private ChannelMetadata currentChannel = null;
	private String uid;
	private String signon;
	
	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		System.out.println("Seting uid of player " + player.getName() + " to " + uid);
		this.uid = uid;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return the channels
	 */
	public List<String> getChannels() {
		return channels;
	}

	/**
	 * @return the currentChannel
	 */
	public ChannelMetadata getCurrentChannel() {
		return currentChannel;
	}

	public PlayerMetadata(Player p) {
		player = p;
		signon = Ircd.getTS();
	}

	/**
	 * @return the signon
	 */
	public String getSignon() {
		return signon;
	}

	/**
	 * @param signon the signon to set
	 */
	public void setSignon(String signon) {
		this.signon = signon;
	}

	/**
	 * @param currentChannel the currentChannel to set
	 */
	public void setCurrentChannel(ChannelMetadata currentChannel) {
		this.currentChannel = currentChannel;
	}
	
	@Override
	public String toString() {
		return super.toString() + " " + player.getName() + " C: " + channels ;
	}

}
