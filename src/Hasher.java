/**
 * .Hasher.java
 *
 * @author Chris Nguyen
 */

/**
 * Implemented by classes that are responsible for hashing a key of any type to an integer.
 */
public interface Hasher<K> 
{
	/**
	 * Hashes a generic key to a positive signed 32-bit integer.
	 * 
	 * @param key Generic key of any type to hash.
	 * @return A 32-bit integer.
	 */
	public int hash(K key);
}
