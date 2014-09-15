/*
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
package org.jivesoftware.smackx.packet;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Represents a <b>message delivery receipt</b> entry as specified by <a
 * href="http://xmpp.org/extensions/xep-0184.html">Message Delivery
 * Receipts</a>.
 * 
 * @author Georg Lukas
 */
public class DeliveryReceipt implements PacketExtension {
	public static final String NAMESPACE = "urn:xmpp:receipts";

	private String id; // / original ID of the delivered message

	public DeliveryReceipt(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getElementName() {
		return "received";
	}

	public String getNamespace() {
		return NAMESPACE;
	}

	public String toXML() {
		return "<received xmlns='" + NAMESPACE + "' id='" + id + "'/>";
	}
}
