package com.example.testapp;

import java.net.InetAddress;

/** All public data members, similar to a C-struct */
public class ClientDevice {
	public int id;
	public long hash;
	public String name;
	public InetAddress ip;
	public int port;

	long unsignedParseLongHex(String s) {
		long l = 0;
		long hi = Long.parseLong(s.substring(0, 8), 16), lo = Long.parseLong(
				s.substring(8), 16);
		l = (hi << 32) | lo;
		return l;
	}

	ClientDevice(String desc) {
		id = 0;
		port = 0;

		// desc format: <name> (hash in 64-bit hex) connected
		String delims = "[ ]+";
		String tokens[] = desc.split(delims);

		name = tokens[0];
		String hashString = tokens[1].substring(1, tokens[1].length() - 1);

		// Long.parseLong **sucks**. It is expecting a positive number, so it
		// doesn't work if the first character on a 16-hex is 8-f
		// I'm doing the split into two 32-bit longs and bitwise xor.
		hash = unsignedParseLongHex(hashString);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ClientDevice)
			return ((ClientDevice) other).hash == this.hash;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return (int) (hash & 0xffffffff);
	}
}
