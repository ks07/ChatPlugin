package de.johannes13.minecraft.bukkit.chat.inspircd;

public enum IrcCommands {
	
	ADDLINE, //Ignore (or add a ban for Q-Lines?) (SHUN -> freeze?)
	BURST, // Not used
	CAPAB, // not used
	DELLINE, //Ignore (or remove a ban for Q-Lines?)
	ENCAP, //Ignore
	ENDBURST, // state = 3, introduce users
	FHOST, //Ignore (for now)
	FJOIN, //get ts, modes etc..
	FNAME, // Ignore (for now)
	FMODE, // Ignore (for now)
	FTOPIC, //Ignore (notify users?)
	IDLE, // response
	JOIN, // response with ERROR? (should not happen)
	METADATA, //Ignore (use accountname?)
	MODENOTICE, //Ignore(?)
	MODULES, //Ignore, who cares?
	NICK, // Update internal channel list
	OPERQUIT, // Ignore
	OPERTYPE, // Ignore (use for special formating?)
	PING, // response (IMPORTANT!!)
	PUSH, // Ignore?
	REHASH, // Ignore? or rehash server? (can only be issued by opers)
	RSQUIT, // should not ocour, we are a leaf
	SERVER, // Do we have to handle it?
	SNONOTICE, // might be send to ops, but ignore for now
	SQUIT, // remove all users from this server
	SVSHOLD, // ignore
	SVSJOIN, // force user to join a channel
	SVSMODE, // ignore, bounce (send a MODE back?) or accept?
	SVSNICK, // bounce or ignore, or do whatever? (other servers may assume that it worked)
	SVSPART, // remove a user from a channel
	TIME, // response with line + " " + TS
	UID, // handle it
	USER, // should not occour
	VERSION, // during burst, ignore
	
	// Other commands
	PRIVMSG, // relay (be careful with CTCPs (/me /version etc)
	NOTICE, // relay? (most of it could be Services spam - log?)
	KICK, // remove user from channel (:909AAAAAB KICK #test 403AAAAAB :foobar)
	MODE, // user mode change, ignore
	
	PART,
	QUIT,
	INVITE, // IGNORE?
	
	
	ADMIN, // response with? PUSH?? format? nummerics?
	MOTD, // response with PUSH.. allow a MOTD file? not now (later)
	STATS, // for own usage, supports 1 Parameter :) (Answer wish PUSH)
	SAJOIN, // force user to join a channel
	SAPART, // force user to leave a channel
	SANICK, // IGNORE
	KILL, // kick user from server?
	

}
