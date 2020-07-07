import static org.junit.Assert.*;

import java.io.*;

import org.junit.Test;

/**
 * @author clairewalker
 *
 */
public class CharCounterTest {

	/**
	 * Tests that size of an empty frequency table is 0
	 */
	@Test
	public void testNewEmpty() {
		CharCounter c = new CharCounter();
		assertEquals(c.getTable().size(), 0);
	}

	/**
	 * Tests that count of bytes read in for input stream "teststring" is 10
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCountAllCount() throws IOException {
		CharCounter c = new CharCounter();
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));

		assertEquals(c.countAll(ins), 10);
	}

	/**
	 * Tests that after one character added to frequency table, table size is 1
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddNew() throws IOException {
		CharCounter c = new CharCounter();
		c.add(116);
		assertEquals(c.getTable().size(), 1);
	}

	/**
	 * Tests that when the same character is added multiple times to the frequency
	 * table, the size of the table remains the same
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddExisting() throws IOException {
		CharCounter c = new CharCounter();
		c.add(116);
		c.add(116);
		assertEquals(c.getTable().size(), 1);
	}

	/**
	 * Tests that expected counts for t, s, and g from input stream "teststring" are
	 * as expected
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetCount() throws IOException {
		CharCounter c = new CharCounter();
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		c.countAll(ins);
		Integer t = ((int) 't');
		Integer s = ((int) 's');
		Integer g = ((int) 'g');
		assertEquals(c.getCount(t), 3);
		assertEquals(c.getCount(s), 2);
		assertEquals(c.getCount(g), 1);
	}

	/**
	 * Tests that count for a character not in frequency table is 0
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetCountNotInTable() throws IOException {
		CharCounter c = new CharCounter();
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		c.countAll(ins);
		assertEquals(c.getCount(104), 0);
	}

	/**
	 * Tests that after clearing the frequency table, its size is 0
	 * 
	 * @throws IOException
	 */
	@Test
	public void testClear() throws IOException {
		CharCounter c = new CharCounter();
		InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		c.countAll(ins);
		c.clear();
		assertEquals(c.getTable().size(), 0);
	}

}
