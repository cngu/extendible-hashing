/**
 * .Directory.java
 *
 * @author Chris Nguyen
 */

/**
 * Represents the Directory table in the Extendible Hashing method.
 */
public class Directory
{
	public static int MAX_DEPTH = 30;
	
	private Bucket[] directory;
	private int depth;			/* max depth is 30 since depth=31 overflows an int data type */
	
	private StringHasher h;
	
	/**
	 * Constructs a Directory for Extendible Hashing with a starting length of 1 and depth of 0.
	 */
	public Directory(int bucketSizeInBytes)
	{
		this.directory = new Bucket[] {new Bucket(bucketSizeInBytes)};
		this.depth = 0;
		this.h = StringHasher.getInstance();
	}
	
	public int getDepth()
	{
		return this.depth;
	}
	
	public int getLength()
	{
		return this.directory.length;
	}
	
	/**
	 * Inserts a value into a Directory entry's bucket. 
	 * 
	 * @param value Value to insert.
	 */
	public void insert(String value)
	{
		int pseudokey = this.h.hash(value);
		int key = BitUtility.getLeftMostBits(pseudokey, this.depth);
		Bucket b = this.directory[key];
		
		if (as1.DEBUG)
			System.out.println("Inserting " + value + " to directory["+key+"] (Bucket: "+b.id+")");
		
		boolean inserted = b.insert(value);
		
		while (! inserted) {			
			if (this.depth > b.getDepth()) {
				if (as1.DEBUG)
					System.out.println("Bucket " + b.id + " is full!");
				
				Bucket b2 = new Bucket(b);
				b2.incDepth1();
				b.incDepth0();
				
				b.filter(b2, b.getBitPattern());
				
				// TODO: This could be optimized a bit if they're adjacent.
				// Could just loop left and right while directory[i] == b, starting at i = key.
				for (int i = 0; i < this.directory.length; i++) {
					if (directory[i] == b) {
						int dirIdxTruncatedToBucketDepth = i >>> (this.depth - b2.getDepth());
						if (dirIdxTruncatedToBucketDepth != b.getBitPattern())
							directory[i] = b2;
					}
				}
			}
			else if (this.depth == b.getDepth()) {
				expand(key);
			}
			else {
				ErrorLogger.logIssue("Directory.insertAt(String)", "Global depth < Local Depth");
				System.exit(-1);
			}
			
			key = BitUtility.getLeftMostBits(pseudokey, this.depth);
			b = this.directory[key];
			inserted = b.insert(value);
		}
	}
	
	/**
	 * Searches for value and counts the number of probes along the way.
	 * 
	 * @param value The string to search for
	 * @return the number of probes to find value
	 */
	public int countProbes(String value)
	{
		int pseudokey = this.h.hash(value);
		int key = BitUtility.getLeftMostBits(pseudokey, this.depth);
		Bucket b = this.directory[key];
		return b.countProbes(value);
	}
	
	/**
	 * Doubles the size of the directory, increments depth, and updates the references to buckets.
	 * 
	 * @param fullBucketIndex The index of the directory entry referencing a full bucket.
	 */
	private void expand(int fullBucketIndex)
	{		
		if (this.depth == Directory.MAX_DEPTH) {
			if (as1.DEBUG)
				System.out.println("Directory has reached max depth!. Creating chained bucket now.");
			
			this.directory[fullBucketIndex].chainBucket();
			return;
			/*
			ErrorLogger.logError("Directory.expand(int)", 
					"Directory has reached maximum size.\nThis is most likely because too many " + 
					"words are being hashed to the same bitpattern/pseudokey. " + 
					"Try a more distributed hash function.");
			System.exit(-1);
			*/
		}
		
		if (as1.DEBUG)
			System.out.println("Expanding Directory: ");
		
		Bucket[] newDirectory = new Bucket[this.directory.length * 2];
		
		for (int i = 0; i < this.directory.length; i++) {
			if (i != fullBucketIndex) {
				newDirectory[i*2] = this.directory[i];
				newDirectory[i*2 + 1] = this.directory[i];
			}
			else {
				Bucket b2 = new Bucket(this.directory[fullBucketIndex]);
				b2.incDepth1();
				this.directory[fullBucketIndex].incDepth0();
				this.directory[fullBucketIndex].filter(b2, fullBucketIndex*2);
				
				newDirectory[fullBucketIndex*2] = this.directory[fullBucketIndex];
				newDirectory[fullBucketIndex*2 + 1] = b2;
			}
		}
		
		this.directory = newDirectory;
		this.depth++;
		
		if (as1.DEBUG)
			print();
	}
	
	public void print()
	{
		System.out.println("Directory Depth: " + this.depth);
		int len = (int) Math.pow(2, this.depth);
		for (int i = 0; i < len; i++) {
			String binaryI = String.format("%30s", Integer.toBinaryString(i)).replace(' ', '0');
			System.out.print(binaryI);
			directory[i].printBucket();
			System.out.println();
		}
		System.out.println();
	}
}
