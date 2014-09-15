package com.ikantech.xmppsupport.util;

import java.io.File;
import java.util.Properties;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.os.Build;
import android.util.Log;

import com.ikantech.support.util.YiLog;

public class XmppConnectionUtils {
	private static final String XMPP_HOST_CFG_NAME = "xmpp-host";
	private static final String XMPP_PORT_CFG_NAME = "xmpp-port";
	private static final String XMPP_SERVER_NAME_CFG_NAME = "xmpp-server-name";

	private static String mXmppHost = "";
	private static int mXmppPort = 5222;
	private static String mXmppServerName = "";

	private XMPPConnection mConnection = null;

	private static XmppConnectionUtils mInstance = null;

	public static XmppConnectionUtils getInstance() {
		if (mInstance == null) {
			mInstance = new XmppConnectionUtils();
		}
		return mInstance;
	}

	private XmppConnectionUtils() {
//		Connection
//				.addConnectionCreationListener(new ConnectionCreationListener() {
//					public void connectionCreated(Connection connection) {
//						connection
//								.addConnectionListener(new ReconnectionManager(
//										connection));
//					}
//				});
	}

	private void openConnection() {
		try {
			if (mXmppHost.length() < 1) {
				Properties properties = PropertiesUtils.getInstance()
						.getProperties();
				if (properties != null) {
					mXmppHost = properties.getProperty(XMPP_HOST_CFG_NAME, "");
					mXmppPort = Integer.parseInt(properties.getProperty(
							XMPP_PORT_CFG_NAME, "5222"));
					mXmppServerName = properties.getProperty(
							XMPP_SERVER_NAME_CFG_NAME, "");
				}
			}

			if (mXmppServerName.length() < 1) {
				mXmppServerName = mXmppHost;
			}
			configure(ProviderManager.getInstance());

			ConnectionConfiguration connConfig = new ConnectionConfiguration(
					mXmppHost, mXmppPort, mXmppServerName);
//			connConfig.setReconnectionAllowed(true);
			//启用压缩
//			connConfig.setCompressionEnabled(true);
			connConfig
					.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
			connConfig.setSASLAuthenticationEnabled(true);

			try {
				// ICE_CREAM_SANDWICH
				if (Build.VERSION.SDK_INT >= 14) {
					connConfig.setTruststoreType("AndroidCAStore");
					connConfig.setTruststorePassword(null);
					connConfig.setTruststorePath(null);
				} else {
					connConfig.setTruststoreType("bks");
					String path = System
							.getProperty("javax.net.ssl.trustStore");
					if (path == null) {
						path = System.getProperty("java.home") + File.separator
								+ "etc" + File.separator + "security"
								+ File.separator + "cacerts.bks";
					}
					connConfig.setTruststorePassword("changeit");
					connConfig.setTruststorePath(path);
				}
			} catch (Exception e) {
				// TODO: handle exception
				YiLog.getInstance().e(e, "set truststore failed");
			}

			mConnection = new XMPPConnection(connConfig);
			mConnection.connect();

		} catch (XMPPException xe) {
			YiLog.getInstance().e(xe, "connect failed");
		}
	}

	private void configure(ProviderManager pm) {
		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
				new OpenIQProvider());
		pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
				new CloseIQProvider());
		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
				new DataPacketProvider());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
	}

	public XMPPConnection getConnection() throws Exception {
		if (mConnection == null) {
			openConnection();
		}
		if (!mConnection.isConnected()) {
			try {
				mConnection.connect();
			} catch (Exception e) {
				throw e;
			}
		}
		return mConnection;
	}

	public XMPPConnection getRawConnection() {
		return mConnection;
	}

	public void closeConnection() {
		if (mConnection != null) {
			mConnection.disconnect();
			mConnection = null;
		}
	}

	public static String getXmppHost() {
		return mXmppHost;
	}

	public static int getXmppPort() {
		return mXmppPort;
	}

	public static void setXmppPort(int xmppPort) {
		mXmppPort = xmppPort;
	}

	public static String getXmppServerName() {
		return mXmppServerName;
	}

	public static void setXmppServerName(String xmppServerName) {
		mXmppServerName = xmppServerName;
	}

	public static void setXmppHost(String xmppHost) {
		mXmppHost = xmppHost;
	}
}
