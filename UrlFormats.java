package algorithms;

/**
 * This class imitates SoundCloud's url formats scheme, in order for WebDriver to get correct urls.
 * @author Dennis Qiu
 */
public class UrlFormats {
	
	/**
	 * Converts a name to SoundCloud's URL format.
	 * @param formats name to be converted to it's URL format
	 * @return converted name's URL format.
	 */
	public static String formats(String formats) {
		formats = formats.toLowerCase();
		if (formats.contains("~")) 
			formats = formats.replace("~", "");
		if (formats.contains("¡")) 
			formats = formats.replace("¡", "");
		if (formats.contains("®")) 
			formats = formats.replace("®", " r");
		if (formats.indexOf("!") > 0 && formats.indexOf("!") < formats.length()-1) 
			formats = formats.replaceFirst("!", "i");
		if (formats.lastIndexOf("!") == formats.length()-1) 
			formats = formats.replaceFirst("!", "");
		if (formats.contains("-("))
			formats = formats.replace("(", "");
		if (formats.contains("-)"))
			formats = formats.replace(")", "");
		if (formats.indexOf("(") > 0 && formats.indexOf("(") < formats.length()-1) 
			formats = formats.replace("(", "-");
		if (formats.lastIndexOf(")") == formats.length()-1) 
			formats = formats.replace(")", "");
		if (formats.contains("*"))
			formats = formats.replace("*", "");
		if (formats.indexOf(".") > 0 && formats.indexOf(".") < formats.length()-1) 
			formats = formats.replace(".", "-");
		if (formats.lastIndexOf(".") == formats.length()-1) 
			formats = formats.replace(".", "");
		if (formats.indexOf("/") > 0 && formats.indexOf("/") < formats.length()-1) 
			formats = formats.replace("/", "-");
		if (formats.indexOf("\\") > 0 && formats.indexOf("\\") < formats.length()-1) 
			formats = formats.replace("\\", "");
		if (formats.contains("["))
			formats = formats.replace("[", "-");
		if (formats.lastIndexOf("]") == formats.length()-1) 
			formats = formats.replaceFirst("]", "");
		if (formats.indexOf("@") > 0 && formats.indexOf("@") < formats.length()-1) 
			formats = formats.replace("@", "");
		if (formats.indexOf(":") > 0 && formats.indexOf(":") < formats.length()-1) 
			formats = formats.replace(":", "-");
		if (formats.contains(" "))
			formats = formats.replace(" ", "-");
		while (formats.contains("--"))
			formats = formats.replace("--", "-");
		if (formats.lastIndexOf("-") == formats.length()-1) 
			formats = formats.replaceFirst("-", "");
		return formats;
	}
}
