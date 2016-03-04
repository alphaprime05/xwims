package com.esipe.dbimport;

import java.nio.charset.Charset;
import java.util.HashMap;

public class CharsetHandler {
	private final HashMap<String, Charset> charsetMap;

	public CharsetHandler() {
		charsetMap = new HashMap<>();
	}

	public void putCharset(String language, String charsetName) {
		Charset charset;
		try {
			charset = Charset.forName(charsetName);
		} catch(Exception e) {
			System.err.println("ERROR: Can't find charset with name " + charsetName);
			return;
		}
		charsetMap.put(language, charset);
	}
	
	public Charset getCharset(String language) {
		return charsetMap.get(language);
	}
}
