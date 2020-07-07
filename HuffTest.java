import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

/**
 * @author clairewalker
 *
 */
public class HuffTest {

	/**
	 * Tests that HuffTree created from inputstream "teststring" has correct root
	 * weight of 11
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMakeHuffTree() throws IOException {
		Huff h = new Huff();
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		HuffTree ht = h.makeHuffTree(ins);
		assertEquals(ht.root().weight(), 11);
	}

	/**
	 * Tests that frequency table created from inputstream "teststring" has correct
	 * size of 8
	 * 
	 * @throws IOException
	 */
	@Test
	public void makeTableSize() throws IOException {
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		Huff h = new Huff();
		h.makeHuffTree(ins);
		Map<Integer, String> m;
		m = h.makeTable();
		assertEquals(m.size(), 8);
	}

	/**
	 * Tests that getCode returns the expected string compression codes for
	 * inputstream "teststring"
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetCodes() throws IOException {
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		Huff h = new Huff();
		h.makeHuffTree(ins);
		Map<Integer, String> m;
		m = h.makeTable();
		assertEquals(m.get(105), "011");
		assertEquals(h.getCode(105), "011");
		assertEquals(h.getCode(103), "010");
		assertEquals(h.getCode(116), "10");
		assertEquals(h.getCode(115), "110");
		assertEquals(h.getCode(10), null);
	}

	/**
	 * Tests that showCounts returns correct frequency counts for select letters
	 * from inputstream "teststring"
	 * 
	 * @throws IOException
	 */
	@Test
	public void testShowCounts() throws IOException {
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		Huff h = new Huff();
		h.makeHuffTree(ins);
		Map<Integer, Integer> m;
		m = h.showCounts();
		assertFalse(m == null);
		int i = (int) m.get(105);
		assertEquals(i, 1);
		int g = (int) m.get(103);
		assertEquals(g, 1);
		int t = (int) m.get(116);
		assertEquals(t, 3);
		int s = (int) m.get(115);
		assertEquals(s, 2);
	}

	/**
	 * Tests that writeheader size for inputstream "teststring" is expected size of
	 * magic number plus compressed tree size
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCompressHuffTree() throws IOException {
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		Huff h = new Huff();
		h.makeHuffTree(ins);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertEquals(32 + 15 + 72, h.writeHeader(new BitOutputStream(out)));
		out.close();
	}

	/**
	 * Tests that write returns the correct size of file written for compressed
	 * version of input stream "teststring" Size if size of magic number, plus size
	 * of compressed tree, plus size for all compressed characters written Then
	 * tests that when this file is compressed, it returns the same size of the
	 * original file, 8*(number of chars in file)
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUncompressTrue() throws IOException {
		Huff h = new Huff();
		assertEquals(
				h.write("/Users/clairewalker/eclipse-workspace/594 file compression6/src/intest",
						"/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest", true),
				72 + 15 + 32 + 32);
		assertEquals(10 * 8, h.uncompress("/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest",
				"/Users/clairewalker/eclipse-workspace/594 file compression6/src/uncomp"));
	}

	/**
	 * Tests that when write() is passed false for forcing compression, if the
	 * compressed file is larger than the original file, correctly returns size of
	 * compressed file and does not create output file
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUncompressFalse() throws IOException {
		Huff h = new Huff();
		assertEquals(
				h.write("/Users/clairewalker/eclipse-workspace/594 file compression6/src/intest",
						"/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest2", false),
				72 + 15 + 32 + 32);
	}
	
	@Test
	public void testU() throws IOException {
//		Huff h = new Huff();
//				h.write("/Users/clairewalker/eclipse-workspace/594 file compression6/src/intest",
//						"/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest5", true);
//	h.uncompress("/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest3", "/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest3u");
	}
	
	@Test
	public void tes() throws IOException {
		Huff h = new Huff();
//				h.write("/Users/clairewalker/eclipse-workspace/594 file compression6/src/intest",
//						"/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest3", true);
	h.uncompress("/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest5", "/Users/clairewalker/eclipse-workspace/594 file compression6/src/outtest5u");
	}
	
}
