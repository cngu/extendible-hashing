/**
 * .Bucket.java
 *
 * @author Chris Nguyen
 */

/**
 * Represents a bucket pointed at by an index in the Directory in Extendible Hashing.
 */
public class Bucket
{
	//          +-------------------------+
	// bucket = |   HEADER   |   BUFFER   |
	//          +-------------------------+
	// 
	// HEADER contains the length of the words in the BUFFER, and grows left to right.
	// BUFFER contains the words, and grows right to left, but the words are NOT reversed.
	// Ex: [ 3,4,4, <empty cells here>, S,u,n,J,a,v,a,U,o,f,C ] 
	private char[] bucket;
	
	private int depth;
	private int bitPattern;
	
	private int remainingSize;
	private int numWords;	// also known as the header length
	private int startOfBuffer;
	
	private Bucket nextBucket;
	
	private StringHasher h;
	
	private static int ID;
	public int id;
	
	/**
	 * Constructs a new bucket.
	 * 
	 * @param capacity the maximum size of the bucket.
	 */
	public Bucket(int capacity)
	{
		this(capacity, 0);
	}
	
	/**
	 * Constructs a new bucket.
	 * 
	 * @param capacity the maximum size of the bucket.
	 * @param newDepth the initial depth of the bucket.
	 */
	public Bucket(int capacity, int newDepth)
	{
		this.bucket = new char[capacity];
		this.depth = newDepth;
		this.bitPattern = -1;
		this.remainingSize = capacity;
		this.numWords = 0;
		this.startOfBuffer = capacity;
		this.nextBucket = null;
		this.h = StringHasher.getInstance();
		
		this.id = Bucket.ID++;
	}
	
	/**
	 * Creates an empty Bucket with the state of the input Bucket.
	 * 
	 * @param b bucket to copy state, but not the bucket header/buffer (contents) itself.
	 */
	public Bucket(Bucket b)
	{
		this.bucket = new char[b.getCapacity()];
		this.depth = b.getDepth();
		this.bitPattern = b.getBitPattern();
		this.remainingSize = this.bucket.length;
		this.numWords = 0;
		this.startOfBuffer = this.bucket.length;
		this.nextBucket = null;
		this.h = StringHasher.getInstance();
		
		this.id = Bucket.ID++;
	}
	
	/**
	 * Gets the capacity of the bucket.
	 * 
	 * @return the capacity of the bucket.
	 */
	public int getCapacity()
	{
		return this.bucket.length;
	}
	
	/**
	 * Gets the depth of the bucket.
	 * 
	 * @return the local depth of the bucket.
	 */
	public int getDepth()
	{
		return this.depth;
	}
	
	/**
	 * Gets the bit pattern of the bucket.
	 *
	 * @return the bit pattern of the bucket.
	 */
	public int getBitPattern()
	{
		return this.bitPattern;
	}
	
	/**
	 * Increments the depth of the bucket. Appends 0.
	 */
	public void incDepth0()
	{
		this.depth++;
		
		if (this.bitPattern == -1)
			this.bitPattern = 0;
		else
			this.bitPattern = BitUtility.append0(this.bitPattern);
	}
	
	/**
	 * Increments the depth of the bucket. Appends 1.
	 */
	public void incDepth1()
	{
		this.depth++;
		
		if (this.bitPattern == -1)
			this.bitPattern = 1;
		else
			this.bitPattern = BitUtility.append1(this.bitPattern);
	}
	
	/**
	 * Gets the remaining size of the bucket.
	 * 
	 * @return the remaining size of the bucket.
	 */
	public int getRemainingSize()
	{
		return this.remainingSize;
	}
	
	/**
	 * Create a new bucket and link to it.
	 */
	public void chainBucket()
	{
		Bucket prev = this;
		Bucket curr = this.nextBucket;	// might have to make a getNextBucket()
		
		while (curr != null) {
			prev = curr;
			curr = curr.nextBucket;
		}
		prev.nextBucket = new Bucket(prev);
	}
	
	/**
	 * Inserts a value into the bucket.
	 * 
	 * @param value The value to insert.
	 * @return true if insert succeeded, false if there is not enough room to insert.
	 */
	public boolean insert(String value)
	{
		// Return false if there is not enough room to store every character plus the length
		if (this.remainingSize < value.length() + 1) {
			// If the next bucket is not null, the directory must have reached it's max size and 
			// called chainBucket(). In this case, insert value in the next bucket.
			if (this.nextBucket != null) {
				if (as1.DEBUG) 
					System.out.println("Bucket: " + this.id + " is full! " +
						"Inserting into chained bucket: " + this.nextBucket.id);
				return this.nextBucket.insert(value);
			}
			else {
				if (as1.DEBUG)
					System.out.println("Bucket " + this.id + " is full!");
				return false;
			}
		}
		
		// Subtract the number of bytes used to store 'value', and one more to store the length
		this.remainingSize -= (value.length() + 1);
		
		// Find the proper position in header to insert value's length by a sequential search.
		int offset = 0;
		int valueLength = value.length();
		int i = 0;
		for (; i < this.numWords; i++) {
			if (this.bucket[i] < valueLength) {
				offset += this.bucket[i];
			}
			else if (this.bucket[i] == valueLength) {
				int indexOfWord = this.bucket.length - offset - this.bucket[i];
				String wordInBucket = String.copyValueOf(this.bucket, indexOfWord, valueLength);
				
				// if value to insert is less than the current word in the bucket
				if (value.compareTo(wordInBucket) < 0) {
					break;
				}
				else {
					offset += this.bucket[i];
				}
			}
			else {
				break;
			}
		}
		
		// Make room to insert length in header and word in buffer.
		if (i != this.numWords) {
			shiftRight(i, this.numWords-1, 1);
		}
		shiftLeft(this.startOfBuffer, this.bucket.length - offset - 1, valueLength);
		
		// Since we are inserting from the end of the array, offset must include the length of value
		offset += valueLength;
		this.startOfBuffer -= valueLength;
		
		// Insert the length into the header
		this.bucket[i] = (char) valueLength;
		
		// Insert the word itself into the buffer.
		writeStringToBucket(value, this.bucket.length - offset);
		this.numWords++;
		
		return true;
	}
	
	/**
	 * Searches for the given key in the bucket.
	 * 
	 * @param key The key to search for.
	 * @return Index of key if found. -1 otherwise.
	 */
	public int search(String key)
	{
		// Find ANY (there may be multiple) index of key.length() in the header.
		int midIndexOfKeyInHeader = binarySearchHeader(key.length());
		int curr = midIndexOfKeyInHeader;
		
		// If the word length is not found, try searching the next chained bucket.
		if (curr < 0) {
			if (this.nextBucket != null)
				return this.nextBucket.search(key);
			else
				return -1;
		}
		
		// Find offset to the word itself in the buffer.
		int offset = 0;
		for (int i = 0; i <= midIndexOfKeyInHeader; i++) {
			offset += this.bucket[i];
		}
		
		// Check if key is found
		String word = String.copyValueOf(this.bucket, this.bucket.length-offset, this.bucket[curr]);
		if (key.compareTo(word) == 0)
			return curr;
		
		// Scan left to find key
		curr = midIndexOfKeyInHeader-1;
		while (curr >= 0 && this.bucket[curr] == key.length()) {
			word = String.copyValueOf(this.bucket, this.bucket.length-offset, this.bucket[curr]);
			if (key.compareTo(word) == 0)
				return curr;
			offset -= this.bucket[curr];
			curr--;
		}
		
		// Scan right to find key
		curr = midIndexOfKeyInHeader+1;
		while (curr < this.numWords && this.bucket[curr] == key.length()) {
			word = String.copyValueOf(this.bucket, this.bucket.length-offset, this.bucket[curr]);
			if (key.compareTo(word) == 0)
				return curr;
			offset += this.bucket[curr];
			curr++;
		}
		
		// Could not find word
		if (this.nextBucket != null)
			return this.nextBucket.search(key);
		else
			return -1;
	}
	
	/**
	 * Searches for the given key in the bucket, and counts the number of probes.
	 * 
	 * @param key The key to search for.
	 * @return The number of probes to find key
	 */
	public int countProbes(String key)
	{
		int numProbes = 0;
		
		// Find ANY (there may be multiple) index of key.length() in the header.
		int midIndexOfKeyInHeader = binarySearchHeader(key.length());
		int curr = midIndexOfKeyInHeader;
		
		// If the word length is not found, try searching the next chained bucket.
		if (curr < 0) {
			if (this.nextBucket != null) {
				numProbes++;
				return numProbes + this.nextBucket.countProbes(key);
			}
			else {
				return -1;
			}
		}
		
		// Find offset to the word itself in the buffer.
		int offsetToMidIndexOfKey = 0, currOffset = 0;
		for (int i = 0; i <= midIndexOfKeyInHeader; i++) {
			offsetToMidIndexOfKey += this.bucket[i];
		}
		
		// Check if key is found
		String word = String.copyValueOf(this.bucket, this.bucket.length-offsetToMidIndexOfKey, this.bucket[curr]);
		numProbes++;
		if (key.compareTo(word) == 0)
			return numProbes;
		
		// Scan left to find key
		currOffset = offsetToMidIndexOfKey;
		curr = midIndexOfKeyInHeader-1;
		while (curr >= 0 && this.bucket[curr] == key.length()) {
			currOffset -= this.bucket[curr];
			word = String.copyValueOf(this.bucket, this.bucket.length-currOffset, this.bucket[curr]);
			numProbes++;
			if (key.compareTo(word) == 0)
				return numProbes;
			
			curr--;
		}
		
		// Scan right to find key
		currOffset = offsetToMidIndexOfKey;
		curr = midIndexOfKeyInHeader+1;
		while (curr < this.numWords && this.bucket[curr] == key.length()) {
			currOffset += this.bucket[curr];
			word = String.copyValueOf(this.bucket, this.bucket.length-currOffset, this.bucket[curr]);
			numProbes++;
			if (key.compareTo(word) == 0)
				return numProbes;
			
			curr++;
		}
		
		// Could not find word
		if (this.nextBucket != null) {
			numProbes++;
			return numProbes + this.nextBucket.countProbes(key);
		}
		else {
			return -1;
		}
	}
	
	
	private int binarySearchHeader(int length)
	{
		int l = 0, r = this.numWords-1, m = mid(l, r);
		while (l <= r) {
			if (length < this.bucket[m]) {
				r = m-1;
				m = mid(l, r);
			}
			else if (length > this.bucket[m]) {
				l = m+1;
				m = mid(l, r);
			}
			else {
				return m;
			}
		}
		
		// At this point, r < l. Did not find length in header.
		return -1;
	}
	
	private void erase(int start, int end)
	{
		for (int i = start; i <= end; i++)
			this.bucket[i] = ' ';
		this.remainingSize += end-start+1;
	}
	
	public void shiftRight(int start, int end, int numberOfShifts)
	{
		for (int i = end; i >= start; i--) {
			bucket[i+numberOfShifts] = bucket[i];
		}
		for (int i = start; i < start+numberOfShifts; i++) {
			bucket[i] = ' ';
		}
	}
	
	public void shiftLeft(int start, int end, int numberOfShifts)
	{
		for (int i = start; i <= end; i++) {
			bucket[i-numberOfShifts] = bucket[i];
		}
		for (int i = end; i > end-numberOfShifts; i--) {
			bucket[i] = ' ';
		}
	}
	
	public void writeStringToBucket(String value, int start)
	{
		for (int i = 0; i < value.length(); i++) {
			bucket[start+i] = value.charAt(i);
		}
	}
	
	/**
	 * Filters all values in the bucket that does not start with bitPattern into b.
	 * 
	 * @param b Bucket to move values that do not fit the bitPattern filter
	 * @param bitPattern Filter
	 */
	public void filter(Bucket b, int bitPattern)
	{
		int offset = 0;
		for (int i = 0; i < this.numWords; i++) {
			offset += this.bucket[i];
			String word = String.copyValueOf(this.bucket, this.bucket.length-offset, this.bucket[i]); 
			
			if (BitUtility.getLeftMostBits(h.hash(word), this.depth) != bitPattern) {
				b.insert(word);
				
				erase(i, i);				
				shiftLeft(i+1, this.numWords-1, 1);
				i--;
				this.numWords--;
				
				erase(this.bucket.length-offset, this.bucket.length-offset+word.length()-1);
				shiftRight(startOfBuffer, this.bucket.length-offset-1, word.length());
				offset -= word.length();
				startOfBuffer += word.length();
			}
		}
	}
	
	/**
	 * Calculates the midpoint between two integers.
	 * 
	 * @param left One endpoint.
	 * @param right Another endpoint.
	 * @return The midpoint between left and right.
	 */
	private int mid(int left, int right)
	{
		if (left <= right)
			return (right-left)/2 + left;
		else
			return (left-right)/2 + right;
	}
	
	public void printBucket()
	{
		// Print bucket id, depth, and bit pattern
		String bitStr;
		if (this.bitPattern == -1 || this.depth == 0)
			bitStr = "_";
		else
			bitStr = String.format("%"+this.depth+"s", Integer.toBinaryString(this.bitPattern));
		bitStr = bitStr.replace(' ', '0');
		
		System.out.printf("|B: %d D: %d Bit: %s|", this.id, this.depth, bitStr);
		
		// Print bucket contents
		String header = "", buffer = "";
		int offset = 0;
		for (int i = 0; i < this.numWords; i++) {
			header += (int) this.bucket[i];
			offset += this.bucket[i];
			String word = String.copyValueOf(this.bucket, this.bucket.length-offset, this.bucket[i]);
			buffer = word + buffer;;
		}
		System.out.print(header + "..." + this.remainingSize + " free bytes..." + buffer);
		
		// Print next bucket (if any)
		if (this.nextBucket != null) {
			System.out.print(" -> (next bucket: " + this.nextBucket.id + ")" + "\n                           -> ");
			this.nextBucket.printBucket();
		}
	}
}
