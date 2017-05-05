package org.komparator.security.handler;

import java.security.SecureRandom;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;
import java.security.*;
import java.security.cert.Certificate;
import javax.crypto.SecretKey;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;

public class HandlerManager {

	SortedMap<Long, Set<String>> recent_nonces = new TreeMap<>();
	Map<String, Certificate> localCertificates = new HashMap<>();
	SecureRandom randomizer = new SecureRandom();
	PublicKey publicKey;
	PrivateKey privateKey;
	PublicKey cipherPublicKey;
	PrivateKey cipherPrivateKey;
	SecretKey secretKey;
	CAClient certificateAuthority;

	private static class SingletonHolder {
		private static final HandlerManager instance = new HandlerManager();
	}

	public static HandlerManager getInstance() {
		return SingletonHolder.instance;
	}

}
