import java.math.BigInteger;

/**
 * .StringHasher.java
 *
 * @author Chris Nguyen
 */

/**
 * Hashes strings to 32-bit integer values.
 */
public class StringHasher implements Hasher<String> 
{
	private static final BigInteger INIT32 = new BigInteger("811c9dc5", 16);
	private static final BigInteger PRIME32 = new BigInteger("01000193", 16);
	private static final BigInteger MOD32 = new BigInteger("2").pow(32);
	  
	private static StringHasher instance = null;
	
	/**
	 * Called exactly once to construct a single instance of StringHasher.
	 */
	private StringHasher() {}
	
	/**
	 * Returns the singleton object.
	 * 
	 * @return Singleton instance of StringHasher
	 */
	public static StringHasher getInstance()
	{
		if (instance == null) {
			instance = new StringHasher();
		}
		
		return instance;
	}
	
	/**
	 * Implements a hash function that hashes strings to positive signed 32-bit integers.
	 * 
	 * @param key The key to hash.
	 */
	public int hash(String key)
	{
		return hash(key.getBytes()).intValue();
	}
	
	/**
	 * FNV-1a hashing algorithm taken from:
	 * 	https://github.com/jakedouglas/fnv-java/blob/master/src/main/java/com/bitlove/FNV.java
	 * 
	 * @param data bytes to hash
	 * @return the hash of the input bytes
	 */
	public BigInteger hash(byte[] data) {
	    BigInteger hash = INIT32;

	    for (byte b : data) {
	      hash = hash.xor(BigInteger.valueOf((int) b & 0xff));
	      hash = hash.multiply(PRIME32).mod(MOD32);
	    }

	    return hash;
	  }
}
