package online.sniper.utils;

public class ByteUtils {

	/**
	 * 2进制转16进制字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String strHex = Integer.toHexString(bytes[i]);
			if (strHex.length() > 3) {
				sb.append(strHex.substring(6));
			} else {
				if (strHex.length() < 2) {
					sb.append("0" + strHex);
				} else {
					sb.append(strHex);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 16进制字符串转2进制
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * 字符转为byte
	 * 
	 * @param c
	 * @return
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * int to byte
	 * 
	 * @param i
	 * @return
	 */
	public static byte[] intToBytes(int i) {
		byte[] bt = new byte[4];
		bt[0] = (byte) (0xff & i);
		bt[1] = (byte) ((0xff00 & i) >> 8);
		bt[2] = (byte) ((0xff0000 & i) >> 16);
		bt[3] = (byte) ((0xff000000 & i) >> 24);
		return bt;
	}

	/**
	 * byte to int
	 * 
	 * @param bytes
	 * @return
	 */
	public static int bytesToInt(byte[] bytes) {
		int num = bytes[0] & 0xFF;
		num |= ((bytes[1] << 8) & 0xFF00);
		num |= ((bytes[2] << 16) & 0xFF0000);
		num |= ((bytes[3] << 24) & 0xFF000000);
		return num;
	}

	/**
	 * 合并两个byte数组
	 * 
	 * @param aBytes
	 *            合并在前
	 * @param bBytes
	 *            合并在后
	 * @return
	 */
	public static byte[] getMergeBytes(byte[] aBytes, byte[] bBytes) {
		int aCount = aBytes.length;
		int bCount = bBytes.length;
		byte[] b = new byte[aCount + bCount];
		for (int i = 0; i < aCount; i++) {
			b[i] = aBytes[i];
		}
		for (int i = 0; i < bCount; i++) {
			b[aCount + i] = bBytes[i];
		}
		return b;
	}

	/**
	 * 5个byte合并
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @param e
	 * @return
	 */
	public static byte[] getMergeBytesFive(byte[] a, byte[] b, byte[] c, byte[] d, byte[] e) {
		int ia = a.length;
		int ib = b.length;
		int ic = c.length;
		int id = d.length;
		int ie = e.length;

		int i = 20 - ib;
		if (i > 0) {
			byte[] ibb = new byte[i];
			byte[] ib20 = new byte[20];
			for (int f = 0; f < ibb.length; f++) {
				ibb[f] = 0x00;
			}
			System.arraycopy(b, 0, ib20, 0, ib);
			System.arraycopy(ibb, 0, ib20, ib, i);
			b = ib20;
			ib = 20;
		}

		int ii = 20 - ic;
		if (ii > 0) {
			byte[] ibb = new byte[ii];
			byte[] ib20 = new byte[20];
			for (int f = 0; f < ibb.length; f++) {
				ibb[f] = 0x00;
			}
			System.arraycopy(c, 0, ib20, 0, ic);
			System.arraycopy(ibb, 0, ib20, ic, ii);
			c = ib20;
			ic = 20;
		}

		byte[] bs = new byte[ia + ib + ic + id + ie];
		System.arraycopy(a, 0, bs, 0, ia);
		System.arraycopy(b, 0, bs, ia, ib);
		System.arraycopy(c, 0, bs, ia + ib, ic);
		System.arraycopy(d, 0, bs, ia + ib + ic, id);
		System.arraycopy(e, 0, bs, ia + ib + ic + id, ie);
		return bs;
	}
}
