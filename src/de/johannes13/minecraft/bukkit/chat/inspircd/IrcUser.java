package de.johannes13.minecraft.bukkit.chat.inspircd;

import java.util.Set;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

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
	public void sendMessage(String message) {
		// TODO Auto-generated method stub

	}

    public boolean isPermissionSet(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPermissionSet(Permission prmsn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPermission(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPermission(Permission prmsn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAttachment(PermissionAttachment pa) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void recalculatePermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setOp(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
