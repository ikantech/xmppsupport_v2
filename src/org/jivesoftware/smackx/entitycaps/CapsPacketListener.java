/**
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.entitycaps;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.entitycaps.packet.CapsExtension;

class CapsPacketListener implements PacketListener {

	private EntityCapsManager manager;

	protected CapsPacketListener(EntityCapsManager manager) {
		this.manager = manager;
	}

	public void processPacket(Packet packet) {
		CapsExtension ext = (CapsExtension) packet.getExtension(
				CapsExtension.NODE_NAME, CapsExtension.XMLNS);

		String nodeVer = ext.getNode() + "#" + ext.getVersion();
		String user = packet.getFrom();

		manager.addUserCapsNode(user, nodeVer);
	}
}