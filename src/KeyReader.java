import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


/**
 * .KeyReader.java
 *
 * @author Chris Nguyen
 */

/**
 * Responsible for reading and storing a file of keys (Strings).
 * Each key must be separated by a newline (i.e. a list of keys)
 */
public class KeyReader 
{
	private BufferedReader reader;
	
	/**
	 * Constructs a KeyReader that reads keys from fileName.
	 * 
	 * @param fileName File to read keys from.
	 */
	public KeyReader(String fileName) throws FileNotFoundException
	{
		openFile(fileName);
	}
	
	/**
	 * Opens a file containing keys.
	 * 
	 * @param fileName File containing keys to open.
	 * @throws FileNotFoundException when the fileName file cannot be found or opened.
	 */
	private void openFile(String fileName) throws FileNotFoundException
	{
		try 
		{
			this.reader = new BufferedReader(new FileReader(fileName));
		} 
		catch (FileNotFoundException fnfe) 
		{
			ErrorLogger.logException("KeyReader.openFile(String)", 
									 fileName + " not found or cannot be opened.", 
									 fnfe);
			throw fnfe;
		}
	}
	
	/**
	 * Reads a single line from the key file.
	 * 
	 * @return A single key.
	 * @throws IOException when a line cannot be read from file.
	 */
	private String readLine() throws IOException
	{
		try 
		{
			return this.reader.readLine();
		} 
		catch (IOException ioe) 
		{
			ErrorLogger.logException("KeyReader.readLine(void)",
									 "Cannot read line from key file",
									 ioe);
			throw ioe;
		}
	}
	
	/**
	 * Reads at most the specified number of lines. If the file contains less lines than the 
	 * parameter passed in, then the entire file is read.
	 * 
	 * @param keyList an ArrayList to read the lines into.
	 * @param lines number of lines to read.
	 * @return The length of the longest line read.
	 * @throws IOException when a line cannot be read from file.
	 */
	public int readLines(ArrayList<String> keyList, int lines) throws IOException
	{
		int longestKey = 0;
		String line;
		
		while (lines > 0 && (line = readLine()) != null) {
			if (line.length() > longestKey)
				longestKey = line.length();
			keyList.add(line);
			lines--;
		}
		
		return longestKey;
	}
	
	/**
	 * Reads in every line of the file passed to the constructor.
	 * 
	 * @return An ArrayList of keys.
	 * @throws IOException when a line cannot be read from file.
	 */
	public ArrayList<String> readAllLines() throws IOException
	{
		ArrayList<String> result = new ArrayList<String>();
		String line;
		
		while ((line = readLine()) != null) {
			result.add(line);
		}
		
		return result;
	}
}
