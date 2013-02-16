/**
 * .ErrorLogger.java
 *
 * @author Chris Nguyen
 */


/**
 * Writes error messages to System.err.
 */
public class ErrorLogger 
{
	/**
	 * Writes formatted issue message to System.err.
	 * 
	 * @param source Source of exception. FOrmat as: "ClassName.MethodName(parameter, types, here)"
	 * @param message Developer's personalized message on possible cause of error.
	 */
	public static void logIssue(String source, String message)
	{
		System.err.println("\n**********");
		System.err.println("ISSUE: " + source);
		System.err.println(message);
		System.err.println("**********\n");
	}
	
	/**
	 * Writes exception details to System.err.
	 * 
	 * @param source Source of exception. Format as: "ClassName.MethodName(parameter, types, here)"
	 * @param message Developer's personalized message on possible cause of exception.
	 * @param e Exception that was thrown.
	 */
	public static void logException(String source, String message, Exception e)
	{
		System.err.println("\n**********");
		System.err.println("EXCEPTION: " + source);
		System.err.println(message);
		System.err.println(e.getMessage());
		e.printStackTrace();
		System.err.println("**********\n");
	}
	
	/**
	 * Writes error details to System.err.
	 * 
	 * @param source Source of error. Format as: "ClassName.MethodName(parameter, types, here)"
	 * @param message Developer's personalized message on possible cause of error.
	 * @param e Error that was thrown.
	 */
	public static void logError(String source, String message, Error e)
	{
		System.err.println("\n**********");
		System.err.println("ERROR: " + source);
		System.err.println(message);
		System.err.println(e.getMessage());
		e.printStackTrace();
		System.err.println("**********\n");
	}
}
