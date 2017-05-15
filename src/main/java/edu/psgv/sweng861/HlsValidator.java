//Author: Victor Azurin
//Purpose: To validate a URL in the HLS context
//Version number: v0.1

package edu.psgv.sweng861;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.*;
import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// main class - contains all business logic
public class HlsValidator {

	private static final Logger logger = LogManager.getLogger();
	protected enum PlaylistType {SINGLE, MASTER, NONE};
	
	public static void main(String[] args) {
		logger.info(">>main()");
		ArrayList<String> InputURLList = new ArrayList<String>();
		ArrayList<Playlist> MasterListPlaylist = new ArrayList<Playlist>();
		System.out.println("HLS Validator: Version v0.3");

		//getUserInput will ask users single lines in case a batch file was not input
		if (args.length != 0){
			InputURLList = getUserInputBatch(args[0]);
		} else {
			InputURLList = getUserInputLine();	
		}
			
		//System.out.println(InputURLList);
		logger.info("This are the URLs input by the user {}", InputURLList);
		
		//Process list of user URLs
		MasterListPlaylist = ProcessURLList(InputURLList);
		
		//call validator
		InvokeValidator(MasterListPlaylist);
		
		logger.info("<<main()");
		System.out.println("Program Terminated");
	}
	
	//getUserInput for batch file
	protected static ArrayList<String> getUserInputBatch(String string) {
        //if batch mode, then ask for path to file
		String batchFile = string;
		Scanner s = null;
    	ArrayList<String> list = new ArrayList<String>();

    	System.out.println("input batch file: " + batchFile );
  		logger.info("input batch file: {}", batchFile);
		try {
			s = new Scanner(new File(batchFile));
		} catch (FileNotFoundException e) {
			logger.debug("File not found - batch mode: {}", batchFile);
			System.err.println("FileNotFoundException error. Please enter a valid Batch file. Exiting program...");
		}
 
    	while (s.hasNext()){
    	    list.add(s.next());
    	}
    	s.close();
	
        // returning list with contents of batch file
		return list;
	}

	//get user input in case ther is no batch file
	protected static ArrayList<String> getUserInputLine() {
    	System.out.println("Please enter URL one at a time");
  		String UserInputURL = null;
		Scanner scanner = new Scanner(System.in);
  		boolean LastInput = false;
  		ArrayList<String> listLineByLine = new ArrayList<String>();
		do {
			System.out.println("Please enter next URL, or type \"end\" to stop:");
			UserInputURL = scanner.nextLine();
			logger.info("Line-by-line input URLs: {}", UserInputURL);
			//checking if User wants to exit program
			if (UserInputURL.equals("end")){
				LastInput = true;
			} else {
				listLineByLine.add(UserInputURL);
			}
		} while (!LastInput);
		return listLineByLine;
	}

//		// Don't close the scanner because doing so also closes System.in.  Do NOT uncomment the line below.
//		//scanner.close();		
	
	//method iterates over list of Playlist objects and calls for its visit method
	private static void InvokeValidator(ArrayList<Playlist> masterListPlaylist) {
		logger.info(">>invokeValidator()");
		
		System.out.println("\n==============================================================");
		System.out.println("Following report shows Objects created and corresponding errors:");
		System.out.println("==============================================================\n");
	    //iterate through all items
	    for(int i = 0; i < masterListPlaylist.size(); i++) {
	    	masterListPlaylist.get(i).accept(new PlaylistElementVisitor());
	    	masterListPlaylist.get(i).accept(new PlaylistFirstLineVisitor());
	    	masterListPlaylist.get(i).accept(new PlaylistDurationVisitor());
	    	masterListPlaylist.get(i).accept(new PlaylistURIVisitor());
	    	masterListPlaylist.get(i).accept(new PlaylistDuplicateTagVisitor());
	    	System.out.println("=================================================");
	    }
		
	    logger.info("<<invokeValidator()");
	}

	//Process list of user URLs
	protected static ArrayList<Playlist> ProcessURLList(ArrayList<String> inputURLList) {
		logger.info(">>ProcessURLList()");
		ArrayList<Playlist> ListPlaylist = new ArrayList<Playlist>();
		String URLString = null;
		PlaylistType result = null;
		//iterate over list of URL - detect if single of master - create objects accordingly
		for (int i = 0; i < inputURLList.size(); i++) {
			URLString = inputURLList.get(i);
			System.out.println("=================================================");
			logger.info("Info on URL: {}", URLString);
			//store URL as object only if it is valid (reachable)
			if (validateURL(URLString)) {
				//check if is single or master playlist
				result = checkSingleorMaster(URLString);
				//create objects accordingly
				if (result == PlaylistType.SINGLE) {
					System.out.print("Creating a single playlist object: " + URLString + "\n");
					logger.debug("Creating single playlisty object: {}", URLString);
					ListPlaylist.add( new SinglePlaylist(URLString) );
				} else if (result == PlaylistType.MASTER) {
					System.out.printf("Creating a master playlist object: " + URLString + "\n");
					logger.debug("Creating master playlisty object: {}", URLString);
					ListPlaylist.add( new MasterPlaylist(URLString) );
				} else if (result == PlaylistType.NONE) {
					System.out.printf("URL header un-identified. Cannot categorize as either single or master playlist: " + URLString + "\n");
				} else {
					logger.fatal("Cannot categorize URL as either single or master playlist: {}", URLString);
				}
			};
		}
		
		logger.info("<<ProcessURLList()");
		return ListPlaylist;
	}

	
	// validateURL returns false if any of the following cases:
	// - MalformedURLException
	// - IOException
	// - REsponse code is not OK --> It is not 200
	// - Content type is not correct
	protected static Boolean validateURL(String UserURL) {
		logger.info(">>validateURL()");
		int ResponseCode = 0;
		String ContentType = null;
		boolean validInput1 = false;
		
		System.out.println("Playlist File name: " + UserURL);
        		
		try { 
			URL objURL = new URL(UserURL); // strURL is the string form of URL
			HttpURLConnection con = (HttpURLConnection) objURL.openConnection();
			// HTTP GET response code
			ResponseCode = con.getResponseCode();
			logger.info("Response Code is: {}", ResponseCode);
			System.out.println("Response Code is: " + ResponseCode);
			// HTTP GET content type
			ContentType = con.getContentType();
			logger.info("Content Type is: {}", ContentType);
			System.out.println("Content Type is: " + ContentType);
		} 
		catch (MalformedURLException e) { 
			logger.error("Bad UserURL is: {}", UserURL);
			System.err.println("MalformedURLException error. Please enter a valid URL.");
			return validInput1;
		} 
		catch (IOException e) { 
			logger.error("IO exception error");
			System.err.println("IO exception error. Problems connecting to URL.");
			return validInput1;
		} 

		//Checking results from HTTP GET
		if (ResponseCode != 200 ) {
			logger.error("URL not available - reponse code not 200.");
			System.err.println("URL not available - Please enter a valid and available URL.");
		} else if ((!ContentType.equals("application/vnd.apple.mpegurl"))&&(!ContentType.equals("application/x-mpegURL"))) {
			logger.error("Content Type is not valid.");
			System.err.println("Content Type is not valid - Please enter a valid URL.");
		} else {
			validInput1 = true;
		}
		
		//Will not store incorrect URL as object - discarding
		if (!validInput1){
			logger.error("URL entry not valid - Will not store incorrect URL as object. URL: {}", UserURL);
			System.err.println("URL entry not valid - Will not store incorrect URL as object. URL:" + UserURL);
		}
		logger.info("<<validateURL()");
		return validInput1;
	}
	
	//decide what kind of object is it. In case it cannot be recognized, then assume it is single playlist
	protected static PlaylistType checkSingleorMaster (String UserURL){
		logger.info(">>checkSingleOrMaster()");
		
		PlaylistType result = null;
		String inputLine = null;
		
		try {
			//open URL
			URL objURL = new URL(UserURL);
			URLConnection urlcon = objURL.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
	      
	        //check if first line is "#EXTM3U" (single) or "" (master)
	        String firstLine = in.readLine();
	        if (firstLine.equals("#EXTM3U")) {
	        	while ((inputLine = in.readLine()) != null) {
	        		if (inputLine.matches("(.*)#EXT-X-STREAM-INF(.*)")) {
	    	        	logger.info("Master playlist detected - Found record: \"#EXT-X-STREAM-INF\" ");
	    				System.out.println("Master playlist detected - Found record is: \"#EXT-X-STREAM-INF\" for URL:" + UserURL);
	    				return PlaylistType.MASTER;
	    			}
	        	}
		        
	        	logger.info("Single playlist detected - First record is \"#EXTM3U\" for URL {}", UserURL);
				System.out.println("Single playlist detected - First record is " + firstLine + " for URL:" + UserURL);
				result = PlaylistType.SINGLE;
	        }   else if (firstLine.matches(".*EXT.*")) {
	        	logger.info("Neither Single or Master playlist detected - First record is {}", firstLine );
				System.out.println("Neither Single or Master playlist detected - First record is " + firstLine + " for URL: " + UserURL);
				System.out.println("Will assume it to be a Single playlist");
				result = PlaylistType.SINGLE;
	        } 	else {
	        	logger.error("Neither Single or Master playlist detected - First record is {}", firstLine );
				System.err.println("Neither Single or Master playlist detected - First record is " + firstLine + " for URL: " + UserURL);
				result = PlaylistType.NONE;
	        }

	        in.close();
		}
			catch (MalformedURLException e) { 
			logger.error("printContentValidURL - Bad UserURL is: {}", UserURL);
			System.err.println("MalformedURLException error. Please enter a valid URL.");
		} 
			catch (IOException e) { 
			logger.error("printContentValidURL - IO exception error");
			System.err.println("IO exception error. Problems connecting to URL.");
		}
		
		logger.info("<<checkSingleOrMaster()");
		
		return result;
		
	}
	

}
