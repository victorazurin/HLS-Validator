package edu.psgv.sweng861;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.psgv.sweng861.HlsValidator.PlaylistType;


// This class holds the types of playlists: single, master and variant
public abstract class Playlist {
	protected static final Logger logger = LogManager.getLogger();
	
	public abstract String getURLName();
	public abstract ArrayList<String> getContents();
	public abstract void accept(PlaylistVisitor visitor);
	
	protected ArrayList<String> slurpURL(String URL) {
		logger.info(">>slurpURL()");
		String inputLine = null;
		ArrayList<String> file = new ArrayList<String>(); 
		
		try {
			//open URL
			URL objURL = new URL(URL);
			URLConnection urlcon = objURL.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
		      
			while ((inputLine = in.readLine()) != null) 
				//logger.info("inputLine is {}", inputLine);
	        	file.add(inputLine);
	        in.close();
		}
			catch (MalformedURLException e) { 
			System.err.println("MalformedURLException error. Please enter a valid URL.");
		} 
			catch (IOException e) { 
			System.err.println("IO exception error. Problems connecting to URL.");
		}
		
		logger.info("<<slurpURL()");
		return file;
	};
	
}

// single playlist object
class SinglePlaylist extends Playlist {
	private String URLname;
	private ArrayList<String> Contents;

	public SinglePlaylist(String URLname) {
		this.URLname = URLname;
		this.Contents = slurpURL(URLname);
	}

	@Override
	public String getURLName() {
        return this.URLname;
    }
	
	@Override
	public ArrayList<String> getContents() {
        return this.Contents;
    }

	@Override
	public void accept(PlaylistVisitor visitor) {
		visitor.visit(this);
	}

}

//master playlist will also create variant objects. It will also invoke visitor for all its variants
class MasterPlaylist extends Playlist {
	private String URLname;
	private ArrayList<String> Contents;
	private ArrayList<VariantPlaylist> variants = new ArrayList<VariantPlaylist>();

	public MasterPlaylist(String URLname) {
		this.URLname = URLname;
		this.Contents = slurpURL(URLname);

		//creating variants
		logger.info("creating variants from master playlist: {}", URLname);
		try {
			String inputLine = null;
			URL objURL = new URL(URLname);
			URLConnection urlcon = objURL.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));
	        //check for .m3u8 format files
	     	while ((inputLine = in.readLine()) != null) {
	    		if (inputLine.matches("(.*).m3u8(.*)")) {
					//create variant objects;
	    			//logger.info("variant name: {}", URLname.substring(0, URLname.lastIndexOf("/")) + "/" + inputLine);
					variants.add( new VariantPlaylist(URLname.substring(0, URLname.lastIndexOf("/")) + "/" + inputLine));
				}
	    	}
	        in.close();
		}
			catch (MalformedURLException e) { 
			System.err.println("MalformedURLException error. Something wrong with variant" + URLname);
		} 
			catch (IOException e) { 
			System.err.println("IO exception error. Something wrong with variant" + URLname);
		}
	}
	
	@Override
	public String getURLName() {
        return this.URLname;
    }
	
	@Override
	public ArrayList<String> getContents() {
        return this.Contents;
    }

	@Override
	public void accept(PlaylistVisitor visitor) {
		visitor.visit(this);
		
		//visit all variants as well
		//System.out.println("Contents: ");
		for (int i = 0; i < variants.size(); i++) {
			variants.get(i).accept(visitor);
		}
	}
}

//variant playlist object
class VariantPlaylist extends Playlist {
	private String URLname;
	private ArrayList<String> Contents;

	public VariantPlaylist(String URLname) {
		this.URLname = URLname;
		this.Contents = slurpURL(URLname);
	}
	
	@Override
	public String getURLName() {
        return this.URLname;
    }
	
	@Override
	public ArrayList<String> getContents() {
        return this.Contents;
    }

	@Override
	public void accept(PlaylistVisitor visitor) {
		visitor.visit(this);		
	}
}