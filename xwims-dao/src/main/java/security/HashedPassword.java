package security;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class HashedPassword {
	private final byte[] hash;
	private final byte[] salt;
	private final int iterations;

	public HashedPassword(byte[] hash, byte[] salt) {
		this.iterations = 1000;
		this.salt = salt;
		this.hash = hash;
	}

	public HashedPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		this(password, generateSalt().getBytes());
	}

	private HashedPassword(String password, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.iterations = 1000;		
		char[] chars = password.toCharArray();

		this.salt = salt;
		PBEKeySpec keySpec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		hash = skf.generateSecret(keySpec).getEncoded();
	}

	public HashedPassword(String hash, String salt) {
		this.iterations = 1000;
		this.salt = toByteArray(salt);
		this.hash = toByteArray(hash);
	}

	public String getHash() throws NoSuchAlgorithmException {
		return toHex(hash);
	}

	public String getSalt() throws NoSuchAlgorithmException {
		return toHex(salt);
	}

	public int getIterations() {
		return iterations;
	}

	private String toHex(byte[] byteArray) throws NoSuchAlgorithmException
	{
		BigInteger i = new BigInteger(1, byteArray);
        String hexa = i.toString(16);
        int paddingSize = (byteArray.length * 2) - hexa.length();
		if(paddingSize > 0)
		{
			return String.format("%0"  +paddingSize + "d", 0) + hexa;
		}else{
			return hexa;
		}
	}

	public boolean comparePasswords(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
		HashedPassword pass = new HashedPassword(password, salt);
		return equals(pass);
	}

	private static String generateSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt.toString();
	}

	private static byte[] toByteArray(String str) {
		int strLength = str.length();
		byte[] data = new byte[strLength / 2];
		for (int i = 0; i < strLength - 1; i += 2) {
			data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i+1), 16));
		}
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(hash);
		result = prime * result + iterations;
		result = prime * result + Arrays.hashCode(salt);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashedPassword other = (HashedPassword) obj;
		if (!Arrays.equals(hash, other.hash))
			return false;
		if (iterations != other.iterations)
			return false;
		if (!Arrays.equals(salt, other.salt))
			return false;
		return true;
	}
}
