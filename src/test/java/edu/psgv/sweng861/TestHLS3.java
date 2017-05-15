package edu.psgv.sweng861;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.psgv.sweng861.HlsValidator.PlaylistType;
import mockit.Mocked;
import mockit.NonStrictExpectations;

public class TestHLS3 {

@Mocked Scanner mockScanner;

// order of difficulty
//validateURL - input: string - returns bool
@Test
public void analyzeValidateURL() {
	
assertEquals(true, HlsValidator.validateURL("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8"));
assertEquals(false, HlsValidator.validateURL("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u"));
}

//getUserInput Line-by-Line mode - input none - returns arraylist<strings>
@Test
public void analizeGetUserInputLine() {
	new NonStrictExpectations() {{
		mockScanner.nextLine(); returns("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8","end");
	}};
	ArrayList<String> val = HlsValidator.getUserInputLine();
	assertEquals("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8", val.get(0).toString());
}

//Check Single of Master - input: string - returns PlaylistType enum
@Test
public void analizeSingleOrMaster() {
	assertEquals(PlaylistType.SINGLE, HlsValidator.checkSingleorMaster("http://gv8748.gv.psu.edu:8084/sweng861/simple-01/playlist.m3u8"));
	assertEquals(PlaylistType.MASTER, HlsValidator.checkSingleorMaster("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8"));
	assertEquals(PlaylistType.SINGLE, HlsValidator.checkSingleorMaster("http://gv8748.gv.psu.edu:8084/sweng861/simple-01-first-record-error/playlist.m3u8"));
}

// processURLList - input Arraylist<string> - returns Arraylist<playlist>
@Test
public void analizeProcessURLList() {
	// setup - input 
	ArrayList<String> inputArray = new ArrayList<String>();
	inputArray.add("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8");
	inputArray.add("http://gv8748.gv.psu.edu:8084/sweng861/simple-01/playlist.m3u8");
	
	ArrayList<Playlist> result = HlsValidator.ProcessURLList(inputArray);
	// result from first object's URL
	Playlist resultObjectOne = result.get(0);
	String stringresultOne = resultObjectOne.getURLName();
	
	// result from second object's URL
	Playlist resultObjectTwo = result.get(1);
	String stringresultTwo = resultObjectTwo.getURLName();
	
	// check
	assertEquals("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8", stringresultOne);
	assertEquals("http://gv8748.gv.psu.edu:8084/sweng861/simple-01/playlist.m3u8", stringresultTwo);
}

//@Test
//public void printTest() {
//// setup
//	final SinglePlaylist testSingleObject = new SinglePlaylist("http://gv8748.gv.psu.edu:8084/sweng861/simple-01/playlist.m3u8");
//	final String expectedOutput = "Single Playlist URL is : http://gv8748.gv.psu.edu:8084/sweng861/simple-01/playlist.m3u8\n";
//	final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
//	System.setOut(new PrintStream(myOut));;
//		// test
//	testSingleObject.accept(new PlaylistElementVisitor());
//	// check results
//	final String printResult = myOut.toString();
//	assertEquals(expectedOutput, printResult);
//}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}




}
