package de.johannes13.minecraft.bukkit.chat.inspircd;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class IrcUser implements CommandSender {
	
	/**
	 * @param ircd
	 */
	public IrcUser(Ircd ircd) {
		this.ircd = ircd;
	}

	String uid, nick;
	final Ircd ircd;

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
		this.uid = uid;
	}

	/**
	 * @return the nick
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * @param nick the nick to set
	 */
	public void setNick(String nick) {
		this.nick = nick;
	}

	@Override
	public Server getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOp() {
		return false;
	}

	@Override
	public boolean isPlayer() {
		return false;
	}

	@Override
	public void sendMessage(String message) {
		// TODO Auto-generated method stub
		
	}
}
