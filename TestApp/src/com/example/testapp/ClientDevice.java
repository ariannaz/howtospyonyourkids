package com.example.testapp;

import java.net.InetAddress;

/** All public data members, similar to a C-struct */
public class ClientDevice {
	public int id;
	public long hash;
	public String name;
	public InetAddress ip;
	public int port;

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
