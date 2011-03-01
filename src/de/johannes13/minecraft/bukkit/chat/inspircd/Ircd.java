package de.johannes13.minecraft.bukkit.chat.inspircd;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import de.johannes13.minecraft.bukkit.chat.inspircd.utils.UidGenerator;
import de.johannes13.minecraft.bukkit.chat.plugin.ChannelMetadata;
import de.johannes13.minecraft.bukkit.chat.plugin.ChatPlugin;
import de.johannes13.minecraft.bukkit.chat.plugin.PlayerMetadata;

public class Ircd extends Thread {

	final ConfigurationNode config;
	final String sid;
	final UidGenerator uidgen;
	final ChatPlugin plugin;
	String rid;
	// 0 = not connected, 1 = burst, 2 = connected
	int state = 0;
	// key: uid, value: nick
	final Hashtable<String, String> uidmap = new Hashtable<String, String>();
	final Hashtable<String, ChannelMetadata> chmap = new Hashtable<String, ChannelMetadata>();
	final Hashtable<String, String> nick2uid = new Hashtable<String, String>();
	final Hashtable<String, IrcUser> uid2meta = new Hashtable<String, IrcUser>();
	final Hashtable<String, PlayerMetadata> uid2player = new Hashtable<String, PlayerMetadata>();

	Socket socket;
	private boolean stopped;

	PrintStream w;

	public Ircd(ConfigurationNode config, ChatPlugin plugin) {
		super("ChatPlugin.IrcD");
		this.config = config;
		sid = config.getString("serverid");
		uidgen = new UidGenerator();
		this.plugin = plugin;
	}

	public void run() {
		// cache the sid value, it is used very often. (see java bytecode for
		// reason)
		String sid = this.sid;
		String pre = ":" + sid;
		while (!stopped)
			try {
				Socket s = new Socket(config.getString("host"), config.getInt("port", 7000));
				socket = s;
				PrintStream w = new PrintStream(s.getOutputStream());
				this.w = w;
				Scanner r = new Scanner(s.getInputStream(), "UTF-8");
				// We skip capab, InspIRCd feels fine.
				String line;
				while (!(line = r.nextLine()).equalsIgnoreCase("CAPAB END")) {
					System.out.println(line);
				}
				w.println("SERVER " + config.getString("serverhost") + " " + config.getString("password") + " 0 " + sid + " :" + config.getString("servername") + "\r\n");
				state = 1;
				w.println(pre + " BURST " + getTS() + "\r\n");
				w.println(pre + " VERSION : Chat Plugin for Bukkit by Johannes13\r\n"); // DO NOT CHANGE!!!
				// Intoduce all users
				boolean ips = config.getBoolean("propagate-ip", true);
				String defhost = config.getString("userhost", "<host>");
				String ident = config.getString("userident", "<nick>");
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					String uid = sid + uidgen.generateUID();
					PlayerMetadata meta = plugin.getMetadata(p);
					meta.setUid(uid);
					uid2player.put(uid, meta);
					String ip, hostname, realhost;
					if (ips) {
						InetAddress addr = p.getAddress().getAddress();
						ip = addr.getHostAddress();
						realhost = addr.getCanonicalHostName();
						if (defhost.contains("<host>"))
							hostname = defhost.replaceAll("<host>", realhost);
						else
							hostname = defhost;
					} else {
						ip = "127.0.0.1";
						realhost = "localhost";
						if (defhost.contains("<host>"))
							hostname = "minecraft";
						else
							hostname = defhost;
					}
					String id = ident;
					if (ident.contains("<nick>"))
						id = ident.replaceAll("<nick>", p.getName());
					w.println(pre + " UID " + uid + " 2 " + p.getName() + " " + realhost + " " + hostname + " " + id + " " + ip + " " + meta.getSignon() + "+" + (p.isOp() ? "or" : "r") + " :Minecraft Player\r\n");
					if (p.isOp())
						w.println(":" + uid + " OPERTYPE Ops\r\n");
					// log the user in to services (might not work, but might
					// work)
					w.println(":" + uid + " METADATA accountname " + p.getName() + "\r\n");
					w.println(":" + uid + " METADATA swhois :is playing Minecraft\r\n");
				}
				for (ChannelMetadata ch : plugin.getChannels()) {
					String rel = ch.getIrcRelay();
					if (rel == null || rel.length() == 0 || rel.charAt(0) != '#')
						continue;
					chmap.put(ch.getIrcRelay(), ch);
				}
				System.out.println(chmap);
				w.println(pre + " ENDBURST");
				line = r.nextLine();
				System.out.println(line);
				assert (line.contains("SERVER"));
				System.out.println(java.util.Arrays.toString(split(line)));
				rid = split(line)[5];
				line = r.nextLine();
				System.out.println(line);
				assert (line.contains("BURST"));
				line = r.nextLine();
				System.out.println(line);
				assert (line.contains("VERSION"));
				// ok, we have now to read the remote users and channels
				while (!stopped) {
					parseCommands(r.nextLine(), w);
				}
			} catch (IOException e) {
				if (!stopped)
					plugin.getServer().getLogger().log(Level.SEVERE, "[IRCD] Exception in the IrcD thread", e);
			}
	}

	private void parseCommands(String line, PrintStream w2) throws IOException {
		line = line.trim();
		if (line.equals("")) return;
		System.out.println(line);
		String pre = ":" + sid;
		String[] data = split(line);
		try {
			boolean force = false;
			IrcCommands cmd = IrcCommands.valueOf(data[1]);
			switch (cmd) {
			case PING:
				w2.println(pre + " PONG " + sid + " " + data[0] + "\r\n");
				break;
			case ENDBURST:
				System.out.println("Recived Endburst RID:" + data[0] + ", rid = " + rid);
				if (data[0].equals(rid))
					joinchannels(w2);
				state = 2;
				break;
			case FJOIN:
				System.out.println(data[2]);
				ChannelMetadata cm = chmap.get(data[2]);
				System.out.println(chmap);
				if (cm == null)
					break;
				System.out.println("FJOIN: " + cm);
				cm.setTs(data[3]);
				String[] usr = data[5].split(" ");
				for (String uid : usr) {
					uid = uid.split(",", 2)[1];
					IrcUser u = uid2meta.get(uid);
					plugin.ircJoin(u, cm, state == 2);
				}
				break;
			case IDLE:
				// Always respond with 0 0? would be a good idea :)
				// maybe change it later to the real signon time... TODO
				w2.println(":" + data[2] + " IDLE " + data[0] + "0 0\r\n");
				break;
			case NICK:
				IrcUser u = uid2meta.get(data[0]);
				u.nick = data[2];
				break;
			case SQUIT:
				String rsid = data[2];
				List<String> uids = new ArrayList<String>();
				uids.addAll(uid2meta.keySet()); // We need a copy of the keys
				// because the they will be
				// modified
				for (String uid : uids) {
					if (!uid.startsWith(rsid))
						continue;
					plugin.playerQuit(uid2meta.get(uid));
				}
				break;
			case SAJOIN:
				force = true;
			case SVSJOIN:
				// forced join.
				plugin.forceJoin(uid2player.get(data[2]), chmap.get(data[3]), force);
				break;
			case SVSPART:
			case SAPART:
				plugin.forcePart(uid2player.get(data[2]), chmap.get(data[3]));
				break;
			case TIME:
				w2.println(line + " " + getTS() + "\r\n");
				break;
			case UID:
				IrcUser iu = new IrcUser();
				iu.uid = data[2];
				iu.nick = data[4];
				uid2meta.put(data[2], iu);
				break;
			case KICK:
				plugin.forcePart(uid2player.get(data[3]), chmap.get(data[3]));
				break;
			case PRIVMSG:
				if (data[2].startsWith("#")) {
					// do something stupid, relay it
					ChannelMetadata ch = chmap.get(data[2]);
					if (ch != null)
						plugin.sendIrcMessage(uid2meta.get(data[0]).nick, ch, data[3]);
				} else {
					PlayerMetadata pm = uid2player.get(data[2]);
					System.out.println(pm);
					if (pm != null)
						plugin.sendIrcMessage(uid2meta.get(data[0]).nick, pm, data[3]);
					// ignore it :/
				}
				break;
			case PART:
				ChannelMetadata ch = chmap.get(data[2]);
				if (ch == null)
					break;
				plugin.sendIrcPart(uid2meta.get(data[0]), chmap.get(data[2]));
				break;
			case QUIT:
				plugin.playerQuit(uid2meta.get(data[0]));
				break;
			case KILL:
				// wtf should I do now? the user is no longer on IRC..
				// reconnect him :)
				// maybe we could kill him later (kick from server) but I'm not
				// sure if admins want this
				boolean ips = config.getBoolean("propagate-ip", true);
				String defhost = config.getString("userhost", "<host>");
				String ident = config.getString("userident", "<nick>");
				String uid = data[2];
				String ip,
				hostname,
				realhost;
				PlayerMetadata meta = uid2player.get(uid);
				Player p = meta.getPlayer();
				if (ips) {
					InetAddress addr = p.getAddress().getAddress();
					ip = addr.getHostAddress();
					realhost = addr.getCanonicalHostName();
					if (defhost.contains("<host>"))
						hostname = defhost.replaceAll("<host>", realhost);
					else
						hostname = defhost;
				} else {
					ip = "127.0.0.1";
					realhost = "localhost";
					if (defhost.contains("<host>"))
						hostname = "minecraft";
					else
						hostname = defhost;
				}
				String id = ident;
				if (ident.contains("<nick>"))
					id = ident.replaceAll("<nick>", p.getName());
				w2.println(pre + " UID " + uid + " 2 " + p.getName() + " " + realhost + " " + hostname + " " + id + " " + ip + " " + meta.getSignon() + "+" + (p.isOp() ? "or" : "r") + " :Minecraft Player\r\n");
				if (p.isOp())
					w2.println(":" + uid + " OPERTYPE Ops\r\n");
				// log the user in to services (might not work, but might work)
				w2.println(":" + uid + " METADATA accountname " + p.getName() + "\r\n");
				w2.println(":" + uid + " METADATA swhois :is playing Minecraft\r\n");
				for(String s : meta.getChannels()) {
					cm = plugin.getChannel(s);
					if (!chmap.contains(cm)) continue;
					w2.println(":" + sid + " FJOIN " + cm.getIrcRelay() + " " + cm.getTs() + " + :," + uid);
				}
				break;
			default:
				// ignore
			}

		} catch (IllegalArgumentException e) {
			plugin.getServer().getLogger().log(Level.INFO, "[IRCD] Got unknown command: " + data[1], e);
		}
	}

	private void joinchannels(PrintStream w2) throws IOException {
		System.out.println("Ircd.joinchannels()");
		for (ChannelMetadata ch : chmap.values()) {
			System.out.println("Ircd.joinchannels()" + ch);
			String modes = "+";
			String ts = ch.getTs();
			if (ts == null) {
				ch.setTs(ts = getTS());
				if (ch.isHidden()) modes = "+s";
			}
			String usrStr = "";
			String lim = "";
			for (PlayerMetadata m : ch.getPlayers()) {
				usrStr += lim;
				usrStr += ",";
				usrStr += m.getUid();
				lim = " ";
			}
			
			w2.println(":" + sid + " FJOIN " + ch.getIrcRelay() + " " + ts + " " + modes + " " + " :" + usrStr);
		}
	}

	public static String getTS() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}

	public static String[] split(String line) {
		String[] sp1 = line.split(" :", 2);
		String[] sp2 = sp1[0].split(" ");
		String[] res;
		if (!sp2[0].startsWith(":")) {
			res = new String[sp1.length + sp2.length];
			System.arraycopy(sp2, 0, res, 1, sp2.length);
		} else {
			res = new String[sp1.length + sp2.length - 1];
			System.arraycopy(sp2, 0, res, 0, sp2.length);
			res[0] = res[0].substring(1);
			System.out.println(res[0]);
		}
		if (sp1.length == 2)
			res[res.length-1] = sp1[1];
		return res;
	}

	public void exit() {
		stopped = true;
		w.println(":" + sid + " SQUIT " + sid + " :Unloading Plugin\r\n");
		try {
			socket.close();
		} catch (IOException e) {
			// happens
		}
	}

	public void sendMessage(PlayerMetadata metadata, String message) {
		ChannelMetadata cm = plugin.getChannel(metadata.getCurrentChannel());
		if (!chmap.contains(cm)) return;
		System.out.println(":" + metadata.getUid() + " PRIVMSG " + cm.getIrcRelay() + " :" + message);
		w.println(":" + metadata.getUid() + " PRIVMSG " + cm.getIrcRelay() + " :" + message);
	}

	public void addPlayer(Player player) {
		boolean ips = config.getBoolean("propagate-ip", true);
		String defhost = config.getString("userhost", "<host>");
		String ident = config.getString("userident", "<nick>");
		String uid = sid + uidgen.generateUID();
		String ip,
		hostname,
		realhost;
		PlayerMetadata meta = plugin.getMetadata(player);
		meta.setUid(uid);
		uid2player.put(uid, meta);
		Player p = player;
		if (ips) {
			InetAddress addr = p.getAddress().getAddress();
			ip = addr.getHostAddress();
			realhost = addr.getCanonicalHostName();
			if (defhost.contains("<host>"))
				hostname = defhost.replaceAll("<host>", realhost);
			else
				hostname = defhost;
		} else {
			ip = "127.0.0.1";
			realhost = "localhost";
			if (defhost.contains("<host>"))
				hostname = "minecraft";
			else
				hostname = defhost;
		}
		String id = ident;
		if (ident.contains("<nick>"))
			id = ident.replaceAll("<nick>", p.getName());
		System.out.println(":" + sid + " UID " + uid + " 2 " + p.getName() + " " + realhost + " " + hostname + " " + id + " " + ip + " " + meta.getSignon() + " +" + (p.isOp() ? "or" : "r") + " :Minecraft Player");
		w.println(":" + sid + " UID " + uid + " 2 " + p.getName() + " " + realhost + " " + hostname + " " + id + " " + ip + " " + meta.getSignon() + " +" + (p.isOp() ? "or" : "r") + " :Minecraft Player");
		if (p.isOp())
			w.println(":" + uid + " OPERTYPE Ops");
		// log the user in to services (might not work, but might work)
		w.println(":" + sid + " METADATA " + uid + " accountname " + p.getName());
		w.println(":" + sid + " METADATA " + uid + " swhois :is playing Minecraft");
		System.out.println(state);
		if (state != 2) return;
		System.out.println(meta);
		for(String s : meta.getChannels()) {
			ChannelMetadata cm = plugin.getChannel(s);
			System.out.println(cm);
			if (!chmap.contains(cm)) continue;
			System.out.println("Join channel " + cm + " TS: " + cm.getTs());
			w.println(":" + sid + " FJOIN " + cm.getIrcRelay() + " " + cm.getTs() + " + :," + uid + "");
		}
	}

	public void removePlayer(Player player, String string) {
		System.out.println(player);
		System.out.println("Ircd.removePlayer() " + plugin.getMetadata(player));
		System.out.println(plugin.getMetadata(player).getUid());
		w.println(":" + plugin.getMetadata(player).getUid() + " QUIT :Quit: " + string);
	}

	public void removePlayer(Player player, String string, String string2) {
		System.out.println("Ircd.removePlayer() Kicked: " + string2);
		w.println(":" + plugin.getMetadata(player).getUid() + " OPERQUIT :Kicked: " + string2);
		w.println(":" + plugin.getMetadata(player).getUid() + " QUIT :Quit: " + string);
	}
}
