import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * .as1.java
 *
 * @author Chris Nguyen
 */

/**
 * CPSC335 Assignment 1
 * 
 * Performs the procedure outlined in the assignment specifications under the "Program" heading:
 * http://pages.cpsc.ucalgary.ca/~marina/335/asmt1_335_2013.doc
 */
public class as1 
{	
	// TODO: Set this flag to true to print out debug information after each insertion/search.
	public static boolean DEBUG = false;
	
	/**
	 * Validates arg0, the file of keys.
	 * @param arg0 File name with keys to hash
	 * @return true if the file can be opened and read, false otherwise.
	 */
	public static boolean checkArg0(String arg0)
	{
		if (arg0 != null) {
			File f = new File(arg0);
			if (f.exists() && f.canRead())
				return true;
		}
		return false;
	}
	
	/**
	 * Validates arg1, the number of keys to hash. The number of keys must be at least one.
	 * @param arg1 Number of keys to hash
	 * @return true if the number of keys is at least one, false otherwise.
	 */
	public static boolean checkArg1(String arg1)
	{
		if (arg1 != null) {
			try {
				if (Integer.parseInt(arg1) >= 1)
					return true;
			}
			catch (NumberFormatException nfe) {
				return false;
			}
		}
		return false;
	}
	
	/** Validates arg2, the size of the bucket in bytes. The size must be large enough to contain
	 * the longest key in the file, plus 1 to store it's length.
	 * @param arg2 Size of the bucket in bytes.
	 * @param longestKey The length of the longest key.
	 * @return true if arg2 >= longestKey+1, false otherwise
	 */
	public static boolean checkArg2(String arg2, int longestKey)
	{
		if (arg2 != null) {
			try {
				if (Integer.parseInt(arg2) >= longestKey+1)
					return true;
			}
			catch (NumberFormatException nfe) {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Prints usage command to the terminal. Exits the program.
	 */
	public static void usage()
	{
		System.out.println("Usage:");
		System.out.println("\tas1 <file> <number of keys to hash> <bucket size (bytes)>");
		System.out.println("file - ../input/<name of key file here>");
		System.out.println("number of keys to hash - must be at least 1");
		System.out.println("bucket size - must be large enough to hold the longest key in file");
		System.out.println("\nAborting program.");
		return;
	}
	
	/**
	 * Reads lines lines from fileName, and inserts them into keyList.
	 * 
	 * @param keyList ArrayList to insert read keys into.
	 * @param fileName File to read keys from.
	 * @param lines Number of lines to read.
	 * @return The length of the longest key.
	 */
	private static int readFile(ArrayList<String> keyList, String fileName, int lines)
	{
		if (!checkArg0(fileName))
			return -1;
		
		try {
			KeyReader reader = new KeyReader(fileName);
			return reader.readLines(keyList, lines);
		}
		catch (FileNotFoundException fnfe) {
			ErrorLogger.logException("as1.readFile", "Invalid file path: " + fileName, fnfe);
			return -1;
		}
		catch (IOException ioe) {
			ErrorLogger.logException("as1.readFile", "Corrupted file: " + fileName, ioe);
			return -1;
		}
	}
	
	public static void main(String[] args)
	{	
		// Validate the file path and the number of keys to hash (args 0 and 1 respectively)
		if (args.length != 3 || !checkArg0(args[0]) || !checkArg1(args[1])) {
			usage();
			System.exit(0);
		}
		String keyFile = args[0];
		int numberOfKeysToHash = Integer.parseInt(args[1]);
		
		// Read all keys
		ArrayList<String> keys = new ArrayList<String>();
		int longestKey = readFile(keys, keyFile, numberOfKeysToHash);
		if (longestKey <= 0) {
			usage();
			System.exit(-1);
		}
		
		// Ensure the specified bucket size is large enough to contain the longest key
		if (!checkArg2(args[2], longestKey)) {
			usage();
			System.exit(0);
		}
		
		// Create a directory
		int bucketSize = Integer.parseInt(args[2]);
		Directory d = new Directory(bucketSize);
		
		// Insert all keys
		try {
			StringHasher h = StringHasher.getInstance();
			
			for (String k : keys) {				
				d.insert(k);
				
				if (as1.DEBUG) {
					// Print the hash value of k
					int hashValue = h.hash(k.getBytes()).intValue();
					String hashInBinary = String.format("%32s", 
							Integer.toBinaryString(hashValue)).replace(' ', '0');
					System.out.println(String.format("Hashed %s to:\t%s", k, hashInBinary));
					
					// Print directory after inserting
					System.out.println("Directory inserting " + k + ":");
					d.print();
					System.out.println();
				}
			}
		}
		catch (OutOfMemoryError oome) {
			ErrorLogger.logError("as1.main(String[])", 
					"Not enough memory to expand directory to length " + d.getLength() + 
						" (depth = " + d.getDepth() + "). " + 
							"Cannot expand directory further. Aborting program.", oome);
			System.exit(-1);
		}
		
		// Print the results:
		//	1-Final directory
		//	2-Probe count
		System.out.println("\n========================= RESULTS =========================");
		
		System.out.println("Final directory after inserting all "+keys.size()+" keys:\n");
		d.print();
		
		System.out.println();
		
		// Search for keys and count probes
		int probes = 0, totalProbes = 0;
		for (String k : keys) {
			probes = d.countProbes(k);
			totalProbes += probes;
			
			//if (as1.DEBUG)
				System.out.println(probes + " probes to find " + k);
		}
		
		System.out.println("Total number of probes to search for all " + keys.size() + " keys:\t" + totalProbes);
		System.out.println("Average number of probes per key:\t" + ((double)totalProbes)/keys.size());
		
	}
}
