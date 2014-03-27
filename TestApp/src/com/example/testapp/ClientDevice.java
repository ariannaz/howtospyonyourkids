package com.example.testapp;

import java.net.InetAddress;

/** All public data members, similar to a C-struct */
public class ClientDevice {
	public int id;
	public long hash;
	public String name;
	public InetAddress ip;
	public int port;

	ClientDevice(String desc) {
		id = 0;
		port = 0;

		// desc format: <name> (hash in 64-bit hex) connected
		String delims = "[ ]+";
		String tokens[] = desc.split(delims);

		name = tokens[0];
		String hashString = tokens[1].substring(1, tokens[1].length() - 1);
		hash = Long.parseLong(hashString, 16);
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
