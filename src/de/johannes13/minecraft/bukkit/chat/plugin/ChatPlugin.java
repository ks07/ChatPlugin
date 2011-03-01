package de.johannes13.minecraft.bukkit.chat.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.ConfigurationNode;

import de.johannes13.minecraft.bukkit.chat.inspircd.IrcUser;
import de.johannes13.minecraft.bukkit.chat.inspircd.Ircd;

public class ChatPlugin extends JavaPlugin implements Listener {

	private Hashtable<Player, PlayerMetadata> umeta;
	/* package private */Ircd ircd;
	private Method gmGetPermission;
	private Method gmHas;
	private Plugin gm;
	Hashtable<String, ChannelMetadata> cmeta;
	private Player last;

	public ChatPlugin() {
	}

	public ChatPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader loader) {
		super(pluginLoader, instance, desc, folder, plugin, loader);
	}

	@Override
	public void onDisable() {
		getServer().broadcastMessage("Chat Channels and IRC relay have been disabled");
		ircd.exit();
		ircd = null;
		umeta = null;
		cmeta = null;

	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		ChatListener cl = new ChatListener(this);
		pm.registerEvent(Type.PLAYER_CHAT, cl, Priority.High, this);
		pm.registerEvent(Type.PLAYER_JOIN, cl, Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_QUIT, cl, Priority.Monitor, this);
		pm.registerEvent(Type.PLAYER_KICK, cl, Priority.Monitor, this);
		List<ConfigurationNode> empty = Collections.emptyList();

		umeta = new Hashtable<Player, PlayerMetadata>();
		cmeta = new Hashtable<String, ChannelMetadata>();

		// parse config
		List<ConfigurationNode> chs = getConfiguration().getNodeList("channels", empty);
		for (ConfigurationNode conf : chs) {
			cmeta.put(conf.getString("name"), new ChannelMetadata(conf.getString("name"), conf.getString("irc"), conf.getString("public-name"), conf.getBoolean("hidden", false), conf.getInt("priority", 0)));
		}
		System.out.println(cmeta);
		ircd = new Ircd(getConfiguration().getNode("ircd"), this);
		for (Player p : getServer().getOnlinePlayers()) {
			pluginAddPlayer(p);
		}
		ircd.start();
	}

	void pluginAddPlayer(Player p) {
		last = p;
		PlayerMetadata meta = new PlayerMetadata(p);
		int priority = -1;
		for (ChannelMetadata ch : cmeta.values()) {
			if (check(p, "chatplugin.autojoin." + ch.name) && check(p, "chatplugin.join." + ch.name)) {
				ch.players.add(meta);
				meta.getChannels().add(ch.name);
				if (ch.priority > priority) {
					priority = ch.priority;
					meta.setCurrentChannel(ch.name);
				}
			}
		}
		umeta.put(p, meta);
		System.out.println(meta);
	}

	public boolean check(Player p, String perm) {
		if (gm == null)
			gm = getServer().getPluginManager().getPlugin("GroupManager");
		if (gm == null)
			// gm is still not loaded, assume it works
			return true;
		if (gmGetPermission == null) {
			try {
				Class<?> clazz = gm.getClass();
				gmGetPermission = clazz.getMethod("getPermissionHandler");
			} catch (Exception e) {
				getServer().getLogger().log(Level.WARNING, "Tried to access GroupManager, got exception", e);
				// allow it - not a big security risk imho
				return true;
			}
		}
		if (gmHas == null) {
			try {
				Class<?> cl2 = gm.getClass().getClassLoader().loadClass("com.nijiko.permissions.PermissionHandler");
				gmHas = cl2.getMethod("has", Player.class, String.class);
			} catch (Exception e) {
				getServer().getLogger().log(Level.WARNING, "Tried to access GroupManager, got exception", e);
				return true;
			}

		}
		try {
			return (Boolean) gmHas.invoke(gmGetPermission.invoke(gm), p, perm);
		} catch (Exception e) {
			getServer().getLogger().log(Level.WARNING, "Tried to invoke GroupManager, got exception", e);
			return true;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		System.out.println("ChatPlugin.onCommand("+sender+", "+cmd+", "+commandLabel+", "+java.util.Arrays.toString(args)+")");
		return false;
	}

	public PlayerMetadata getMetadata(Player p) {
		PlayerMetadata res = umeta.get(p);
		if (res == null) {
			res = new PlayerMetadata(p);
			umeta.put(p, res);
		}
		return res;
	}

	public Collection<ChannelMetadata> getChannels() {
		return cmeta.values();
	}

	public void ircJoin(IrcUser u, ChannelMetadata cm, boolean notify) {
		cm.ircuser.add(u);
		if (notify) {
			for (PlayerMetadata pm : cm.players) {
				pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + u.getNick() + " joined " + cm.ircRelay + "]");
			}
		}
	}

	public void playerQuit(IrcUser ircUser) {
		ArrayList<PlayerMetadata> sendQuit = new ArrayList<PlayerMetadata>();
		for (ChannelMetadata cm : getChannels()) {
			if (!cm.ircuser.contains(ircUser))
				continue;
			for (PlayerMetadata pm : cm.players) {
				if (sendQuit.contains(pm))
					continue;
				sendQuit.add(pm);
				pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + ircUser.getNick() + " has quit IRC]");
			}
		}
	}

	public void forceJoin(PlayerMetadata playerMetadata, ChannelMetadata channelMetadata, boolean force) {
		playerMetadata.getChannels().add(channelMetadata.name);
		channelMetadata.players.add(playerMetadata);
		playerMetadata.getPlayer().sendMessage(ChatColor.YELLOW + "[You were forced to join " + channelMetadata.getName() + "]");
	}

	public void forcePart(PlayerMetadata playerMetadata, ChannelMetadata channelMetadata) {
		playerMetadata.getChannels().remove(channelMetadata.name);
		channelMetadata.players.remove(playerMetadata);
		if (playerMetadata.getCurrentChannel().equals(channelMetadata.name)) {
			int max = -1;
			String nch = "";
			for(String ch:playerMetadata.getChannels()) {
				if (cmeta.get(ch).priority > max) {
					max = cmeta.get(ch).priority;
					nch = ch;
				}
			}
			playerMetadata.setCurrentChannel("");
			if (nch == "") { // can check against instance, because both are internal :)
				playerMetadata.getPlayer().sendMessage(ChatColor.RED + "[You were forced to leave " + channelMetadata.name + " you are currently in no channel]");
			} else {
				playerMetadata.getPlayer().sendMessage(ChatColor.YELLOW + "[You were forced to leave " + channelMetadata.name + ", your current channel is now " + nch + "]");
			}
		} else {
			playerMetadata.getPlayer().sendMessage(ChatColor.YELLOW + "[You were forced to leave " + channelMetadata.name);
		}
		for (PlayerMetadata pm : channelMetadata.players) {
			pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + playerMetadata.getPlayer().getDisplayName() + ChatColor.YELLOW + " was forced to leave " + channelMetadata.name + "]");
		}
	}

	public void sendIrcMessage(String srcNick, ChannelMetadata channel, String message) {
		for (PlayerMetadata pm : channel.players) {
			pm.getPlayer().sendMessage("[" + channel.getPublicName() + "] " + srcNick + ": " + message);
		}

	}

	public void sendIrcMessage(String srcNick, PlayerMetadata playerMetadata, String message) {
		playerMetadata.getPlayer().sendMessage("(MSG) " + srcNick + ": " + message);
	}

	public void sendIrcPart(IrcUser ircUser, ChannelMetadata channelMetadata) {
		channelMetadata.ircuser.remove(ircUser);
		for (PlayerMetadata pm : channelMetadata.players) {
			pm.getPlayer().sendMessage(ChatColor.YELLOW + "[" + ircUser.getNick() + " has left " + channelMetadata.ircRelay + "]");
		}
	}

	public ChannelMetadata getChannel(String s) {
		return cmeta.get(s);
	}

	public void restartIrcd() {
		ircd.exit();
		ircd = new Ircd(getConfiguration().getNode("ircd"), this);
		ircd.start();
	}

	public void removePlayer(Player player) {
		System.out.println(last);
		System.out.println(player);
		System.out.println(last.equals(player));
		System.out.println(last.hashCode() == player.hashCode());
		System.out.println(last == player);
		PlayerMetadata pm = umeta.remove(player);
		System.out.println(pm);
		for(String s : pm.getChannels()) {
			try {
				cmeta.get(s).players.remove(pm);
			} catch (Exception e) {}
		}
	}

}
