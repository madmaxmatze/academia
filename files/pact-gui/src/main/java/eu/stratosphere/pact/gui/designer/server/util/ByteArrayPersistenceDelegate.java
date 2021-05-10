package eu.stratosphere.pact.gui.designer.server.util;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;

import org.apache.commons.codec.binary.Base64;

/**
 * Helper class to serialize byte array into one String and not into one xml
 * node for each byte
 * 
 * http://java.flyingmac.com/2010/06/making-xmlencoder-do-something-
 * sensible-with-binary-data/
 */
public class ByteArrayPersistenceDelegate extends PersistenceDelegate {
	@Override
	protected Expression instantiate(Object oldInstance, Encoder out) {
		byte[] e = (byte[]) oldInstance;
		return new Expression(e, ByteArrayPersistenceDelegate.class, "decode",
				new Object[] { ByteArrayPersistenceDelegate.encode(e) });
	}

	public static byte[] decode(String encoded) {
		return Base64.decodeBase64(encoded);
	}

	public static String encode(byte[] data) {
		return Base64.encodeBase64String(data);
	}
}