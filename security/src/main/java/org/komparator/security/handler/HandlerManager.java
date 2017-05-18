package org.komparator.security.handler;

import java.security.SecureRandom;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.*;
import java.security.cert.Certificate;
import javax.crypto.SecretKey;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;

public class HandlerManager {

	SortedMap<Long, Set<String>> recentNonces = new ConcurrentSkipListMap<>();
	Map<String, Certificate> localCertificates = new ConcurrentHashMap<>();
	SecureRandom randomizer = new SecureRandom();
	PrivateKey privateKey;
	PublicKey publicKey;
	SecretKey secretKey;
	CAClient certificateAuthority;

	private static class SingletonHolder {
		private static final HandlerManager instance = new HandlerManager();
	}

	public static HandlerManager getInstance() {
		return SingletonHolder.instance;
	}

	public void resetSecretKey() {
		secretKey = null;
	}

}
