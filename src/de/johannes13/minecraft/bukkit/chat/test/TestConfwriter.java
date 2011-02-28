package de.johannes13.minecraft.bukkit.chat.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bukkit.util.config.Configuration;

import de.johannes13.minecraft.bukkit.chat.plugin.ChannelMetadata;

public class TestConfwriter {
	public static void main(String[] args) {
		File f = new File("./config.yml");
		Configuration conf = new Configuration(f);
		conf.load();

		
		ArrayList<Object> channels = new ArrayList<Object>();
		Hashtable<String, Object> ch1 = new Hashtable<String, Object>();
		ch1.put("name", "c");
		ch1.put("irc", "#chat");
		ch1.put("public-name", "Chat");
		ch1.put("hidden", false);
		ch1.put("priority", 5);
		
		channels.add(ch1);
		ch1 = new Hashtable<String, Object>();
		ch1.put("name", "g");
		ch1.put("irc", "#guest");
		ch1.put("public-name", "Guest");
		ch1.put("hidden", false);
		ch1.put("priority", 1);
		channels.add(ch1);
		conf.setProperty("channels", channels);
		
		Hashtable<String, Object> ircd = new Hashtable<String, Object>();
		ircd.put("host", "localhost");
		ircd.put("port", 7000);
		ircd.put("serverid", "404");
		ircd.put("password", "test");
		ircd.put("serverhost", "minecraftserver.local");
		ircd.put("servername", "Minecraft Server");
		ircd.put("propagate-ip", true);
		ircd.put("userhost", "<host>");
		ircd.put("userident", "<nick>");
		
		conf.setProperty("ircd", ircd);
		
		conf.save();
	}
}
