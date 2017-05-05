package org.komparator.security.handler;

import java.security.SecureRandom;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class HandlerManager {

	SortedMap<Long, Set<String>> recent_nonces = new TreeMap<>();
	SecureRandom randomizer = new SecureRandom();

	private static class SingletonHolder {
		private static final HandlerManager instance = new HandlerManager();
	}

	public static HandlerManager getInstance() {
		return SingletonHolder.instance;
	}

}
