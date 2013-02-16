/**
 * .BitUtility.java
 *
 * @author Chris Nguyen
 */

/**
 * Performs bitwise operations.
 */
public class BitUtility 
{
	/**
	 * Finds the n left most bits of a positive signed 32-bit integer.
	 * 
	 * @param value Value to get left most bits from.
	 * @param n Number of left most bits to get.
	 * @return The n leftmost bits of value as an integer.
	 */
	public static int getLeftMostBits(int value, int n)
	{
		if (n == 0) {
			return 0;
		}
		else {
			int rightShifts = 32-n;
			return value >>> rightShifts;
		}
	}
	
	/**
	 * Finds the n right most bits of a positive signed 32-bit integer.
	 * 
	 * @param value Value to get right most bits from.
	 * @param n Number of right most bits to get.
	 * @return The n rightmost bits of value as an integer.
	 */
	public static int getRightMostBits(int value, int n)
	{
		if (n == 0) {
			return 0;
		}
		else {
			// http://stackoverflow.com/questions/2798191/extracting-rightmost-n-bits-of-an-integer
			return value & ((1<<n)-1);
		}
	}
	
	/**
	 * Appends 0 to bit pattern.
	 * 
	 * @param value Bit pattern to append 0 to.
	 * @return Input value with 0 appended.
	 */
	public static int append0(int value)
	{
		return value << 1;
	}
	
	/**
	 * Appends 1 to bit pattern.
	 * 
	 * @param value Bit pattern to append 1 to.
	 * @return Input value with 1 appended.
	 */
	public static int append1(int value)
	{
		return (value << 1) + 1;
	}
	
	public static boolean endsWith0(int value)
	{
		if ((value & 0x00000001) == 0) {
			return true;
		}
		return false;
	}
	
	public static boolean endsWith1(int value)
	{
		return !BitUtility.endsWith0(value);
	}
}
