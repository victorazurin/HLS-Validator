package edu.psgv.sweng861;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// These classes hold the visitor classes, which will be used for checking Playlist objects
public interface PlaylistVisitor {
	  public void visit(SinglePlaylist SinglePlaylist);
	  public void visit(MasterPlaylist MasterPlaylist);
	  public void visit(VariantPlaylist VariantPlaylist);
}

// This visitor only print the URL name
class PlaylistElementVisitor implements PlaylistVisitor {

	@Override
	public void visit(SinglePlaylist SinglePlaylist) {
		System.out.println("Single Playlist URL is : " + SinglePlaylist.getURLName());
		
	}

	@Override
	public void visit(MasterPlaylist MasterPlaylist) {
		System.out.println("Master Playlist URL is : " + MasterPlaylist.getURLName());
		
	}
	
	@Override
	public void visit(VariantPlaylist VariantPlaylist) {
		//System.out.println("Variants : " + VariantPlaylist.getURLName());
		
	}
		
}

// check that first line is correct
class PlaylistFirstLineVisitor implements PlaylistVisitor {
	protected static final Logger logger = LogManager.getLogger();

	@Override
	public void visit(SinglePlaylist SinglePlaylist) {
		logger.info(">>visit() PlaylistFirstLineVisitor - single");
		//System.out.println("Single Playlist Firt Line check:");
		ArrayList<String> textFile = SinglePlaylist.getContents();
		if (!textFile.get(0).equals("#EXTM3U")) {
			System.out.println("Line 1 - MAJOR: First line is not #EXTINF as expected. Found " + textFile.get(0) + " instead");
		}
		logger.info("<<visit() PlaylistFirstLineVisitor - single");
	}

	@Override
	public void visit(MasterPlaylist MasterPlaylist) {
		logger.info(">>visit() PlaylistFirstLineVisitor - master");
		//System.out.println("Master Playlist Firt Line check");
		ArrayList<String> textFile = MasterPlaylist.getContents();
		if (!textFile.get(0).equals("#EXTM3U")) {
			System.out.println("Line 1 - MAJOR: First line is not #EXTINF as expected. Found " + textFile.get(0) + " instead");
		}
		logger.info("<<visit() PlaylistFirstLineVisitor - master");
	}
	
	@Override
	public void visit(VariantPlaylist VariantPlaylist) {
		logger.info(">>visit() PlaylistFirstLineVisitor - variant");
		//System.out.println("Variant Playlist Firt Line check");
		ArrayList<String> textFile = VariantPlaylist.getContents();
		if (!textFile.get(0).equals("#EXTM3U")) {
			System.out.println("\tError in Variant " + VariantPlaylist.getURLName() + ":\n\t" + "Line 1 - MAJOR: First line is not #EXTINF as expected. Found " + textFile.get(0) + " instead");
		}
		logger.info("<<visit() PlaylistFirstLineVisitor - variant");
	}
		
}

// check that duration is not exceeded by the segments
class PlaylistDurationVisitor implements PlaylistVisitor {
	protected static final Logger logger = LogManager.getLogger();

	// checking all the lines
	@Override
	public void visit(SinglePlaylist SinglePlaylist) {
		logger.info(">>visit() PlaylistDurationVisitor - single");
		ArrayList<String> textFile = SinglePlaylist.getContents();
		String duration = null;
		String durationSegment = null;
		//getting playlist duration
		for (String textLines : textFile) {
			if (textLines.matches(".*#EXT-X-TARGETDURATION.*")){
				duration = textLines.substring(textLines.lastIndexOf(":")+1, textLines.length());
			};
		}
		//System.out.println("duration is" + duration);

		//flagging if any segment exceeds duration
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXTINF.*")){
				durationSegment = textFile.get(i).substring(textFile.get(i).lastIndexOf(":")+1, textFile.get(i).lastIndexOf(","));
				//System.out.println("durationSegment is " + durationSegment);
				Double numberduration = Double.parseDouble(duration.trim());
				Double numberdurationSeg = Double.parseDouble(durationSegment.trim());
				int lineNumber = i+1;
				if (numberdurationSeg > numberduration){
					System.out.println("Line " + lineNumber + " - MEDIUM: Segment duration " + durationSegment + " is greater than Playlist max duration " + duration);
				}
			};
		}
		logger.info("<<visit() PlaylistDurationVisitor - single");
	}

	// no need to check in master playlist as it does not hold targetduration info
	@Override
	public void visit(MasterPlaylist MasterPlaylist) {
		logger.info(">>visit() PlaylistDurationVisitor - master");
		logger.info("<<visit() PlaylistDurationVisitor - master");
	}
	
	// checking all the lines
	@Override
	public void visit(VariantPlaylist VariantPlaylist) {
		logger.info(">>visit() PlaylistDurationVisitor - variant");
		ArrayList<String> textFile = VariantPlaylist.getContents();
		String duration = null;
		String durationSegment = null;
		for (String textLines : textFile) {
			if (textLines.matches(".*#EXT-X-TARGETDURATION.*")){
				duration = textLines.substring(textLines.lastIndexOf(":")+1, textLines.length());
			};
		}
		//System.out.println("duration is " + duration);
		//flagging if any segment exceeds duration
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXTINF.*")){
				durationSegment = textFile.get(i).substring(textFile.get(i).lastIndexOf(":")+1, textFile.get(i).lastIndexOf(","));
				//System.out.println("durationSegment is " + durationSegment);
				Double numberduration = Double.parseDouble(duration.trim());
				Double numberdurationSeg = Double.parseDouble(durationSegment.trim());
				int lineNumber = i+1;
				if (numberdurationSeg > numberduration){
					System.out.println("\tError in Variant " + VariantPlaylist.getURLName() + ":\n\t" + "Line " + lineNumber + " - MEDIUM: Segment duration " + durationSegment + " is greater than Playlist max duration " + duration);
				}
			};
		}
		logger.info("<<visit() PlaylistDurationVisitor - variant");
	}
		
}

// check correct sintax of the #EXTINF and #EXT-X-STREAM-INF keywords according to HLS draft
class PlaylistURIVisitor implements PlaylistVisitor {
	protected static final Logger logger = LogManager.getLogger();

	@Override
	public void visit(SinglePlaylist SinglePlaylist) {
		logger.info(">>visit() PlaylistURIVisitor - single");
		ArrayList<String> textFile = SinglePlaylist.getContents();
		String secondLine = null;
		
		//getting #EXTINF line + next line
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXTINF.*")){
				int lineNumber = i+1;
				secondLine = textFile.get(i+1).trim();
				if (!secondLine.matches(".*ts")){
					System.out.println("Line " + lineNumber + " - MAJOR: #EXTINF tag is not followed by media URI(s) .ts as expected. Instead, it is followed by " + secondLine);
				}
			}
		}
		logger.info("<<visit() PlaylistURIVisitor - single");

	}

	@Override
	public void visit(MasterPlaylist MasterPlaylist) {
		logger.info(">>visit() PlaylistURIVisitor - master");
		ArrayList<String> textFile = MasterPlaylist.getContents();
		String secondLine = null;
		
		//getting #EXT-X-STREAM-INF line + next line
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXT-X-STREAM-INF.*")){
				secondLine = textFile.get(i+1).trim();
				int lineNumber = i+1;
				if (!secondLine.matches(".*m3u8")){
					System.out.println("Line " + lineNumber + " - MAJOR: #EXT-X-STREAM-INF tag is not followed by variant URI(s) .m3u8 as expected. Instead, it is followed by " + secondLine);
				}
			}
		}
		logger.info("<<visit() PlaylistURIVisitor - master");
	}
	
	@Override
	public void visit(VariantPlaylist VariantPlaylist) {
		logger.info(">>visit() PlaylistURIVisitor - variant");
		ArrayList<String> textFile = VariantPlaylist.getContents();
		String secondLine = null;
		
		//getting #EXTINF line + next line
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXTINF.*")){
				secondLine = textFile.get(i+1).trim();
				if (!secondLine.matches(".*ts")){
					System.out.println("\tError in Variant " + VariantPlaylist.getURLName() + ":\n\t" + "Line " + i + " - MAJOR: #EXTINF tag is not followed by media URI(s) .ts as expected. Instead, it is followed by " + secondLine);
				}
			}
		}
		logger.info("<<visit() PlaylistURIVisitor - variant");
	}
		
}

//check #EXT-X-VERSION is not duplicated
class PlaylistDuplicateTagVisitor implements PlaylistVisitor {
	protected static final Logger logger = LogManager.getLogger();

	@Override
	public void visit(SinglePlaylist SinglePlaylist) {
		logger.info(">>visit() PlaylistDuplicateTagVisitor - single");
		ArrayList<String> textFile = SinglePlaylist.getContents();
		int counter = 0;
		ArrayList<Integer> linenumbers = new ArrayList<Integer>();
		
		//getting #EXT-X-VERSION and count number of appearances
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXT-X-VERSION.*")){
				counter++;
				linenumbers.add(i+1);
			}
		}
		if (counter > 1){
			System.out.println("Lines " + linenumbers + " - MEDIUM: #EXT-X-VERSION tag must not be duplicated");
		}

		logger.info("<<visit() PlaylistDuplicateTagVisitor - single");

	}

	@Override
	public void visit(MasterPlaylist MasterPlaylist) {
		logger.info(">>visit() PlaylistDuplicateTagVisitor - master");
		ArrayList<String> textFile = MasterPlaylist.getContents();
		int counter = 0;
		ArrayList<Integer> linenumbers = new ArrayList<Integer>();
		
		//getting #EXT-X-VERSION and count number of appearances
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXT-X-VERSION.*")){
				counter++;
				linenumbers.add(i+1);
			}
		}
		if (counter > 1){
			System.out.println("Lines " + linenumbers + " - MEDIUM: #EXT-X-VERSION tag must not be duplicated");
		}
		logger.info("<<visit() PlaylistDuplicateTagVisitor - master");
	}
	
	@Override
	public void visit(VariantPlaylist VariantPlaylist) {
		logger.info(">>visit() PlaylistDuplicateTagVisitor - variant");
		ArrayList<String> textFile = VariantPlaylist.getContents();
		int counter = 0;
		ArrayList<Integer> linenumbers = new ArrayList<Integer>();

		//getting #EXT-X-VERSION and count number of appearances
		for (int i = 0; i < textFile.size(); i++) {
			if (textFile.get(i).matches(".*#EXT-X-VERSION.*")){
				counter++;
				linenumbers.add(i+1);
			}
		}
		if (counter > 1){
			System.out.println("\tError in Variant " + VariantPlaylist.getURLName() + ":\n\t" + "Lines " + linenumbers + " - MEDIUM: #EXT-X-VERSION tag must not be duplicated");
		}
		logger.info("<<visit() PlaylistDuplicateTagVisitor - variant");
	}
		
}