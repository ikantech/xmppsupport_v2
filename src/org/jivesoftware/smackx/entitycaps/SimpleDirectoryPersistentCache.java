/**
 *  Copyright 2011 Florian Schmaus
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jivesoftware.smackx.entitycaps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Simple implementation of an EntityCapsPersistentCache that uses a directory
 * to store the Caps information for every known node. Every node is represented
 * by an file.
 * 
 * @author Florian Schmaus
 * 
 */
public class SimpleDirectoryPersistentCache implements
		EntityCapsPersistentCache {

	private File cacheDir;
	private StringEncoder stringEncoder;

	/**
	 * Creates a new SimpleDirectoryPersistentCache Object. Make sure that the
	 * cacheDir exists and that it's an directory.
	 * 
	 * @param cacheDir
	 */
	public SimpleDirectoryPersistentCache(File cacheDir) {
		this(cacheDir, new Base64Encoder());
	}

	public SimpleDirectoryPersistentCache(File cacheDir,
			StringEncoder stringEncoder) {
		if (!cacheDir.exists())
			throw new IllegalStateException("Cache directory \"" + cacheDir
					+ "\" does not exist");
		if (!cacheDir.isDirectory())
			throw new IllegalStateException("Cache directory \"" + cacheDir
					+ "\" is not a directory");

		this.cacheDir = cacheDir;
		this.stringEncoder = stringEncoder;
	}

	@Override
	public void addDiscoverInfoByNodePersistent(String node, DiscoverInfo info) {
		String filename = stringEncoder.encode(node);
		File nodeFile = new File(cacheDir, filename);

		try {
			if (nodeFile.createNewFile())
				writeInfoToFile(nodeFile, info);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void replay() throws IOException {
		File[] files = cacheDir.listFiles();
		for (File f : files) {
			String node = stringEncoder.decode(f.getName());
			DiscoverInfo info = restoreInfoFromFile(f);
			if (info == null)
				continue;

			EntityCapsManager.addDiscoverInfoByNode(node, info);
		}
	}

	public void emptyCache() {
		File[] files = cacheDir.listFiles();
		for (File f : files) {
			f.delete();
		}
	}

	/**
	 * Writes the DiscoverInfo packet to an file
	 * 
	 * @param file
	 * @param info
	 * @throws IOException
	 */
	private static void writeInfoToFile(File file, DiscoverInfo info)
			throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		try {
			dos.writeUTF(info.toXML());
		} finally {
			dos.close();
		}
	}

	/**
	 * Tries to restore an DiscoverInfo packet from a file.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static DiscoverInfo restoreInfoFromFile(File file)
			throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		String fileContent = null;
		try {
			fileContent = dis.readUTF();
		} finally {
			dis.close();
		}
		if (fileContent == null)
			return null;

		Reader reader = new StringReader(fileContent);
		XmlPullParser parser;
		try {
			parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			parser.setInput(reader);
		} catch (XmlPullParserException xppe) {
			xppe.printStackTrace();
			return null;
		}

		DiscoverInfo iqPacket;
		IQProvider provider = (IQProvider) ProviderManager
				.getInstance()
				.getIQProvider("query", "http://jabber.org/protocol/disco#info");

		// Skip the first <iq id=....> tag
		try {
			parser.next();
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
			return null;
		}

		// Point parser to the query tag
		try {
			parser.next();
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			iqPacket = (DiscoverInfo) provider.parseIQ(parser);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return iqPacket;
	}
}
