import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author clairewalker
 *
 */
public class CharCounter implements ICharCounter, IHuffConstants {
	private Map<Integer, Integer> Table;

	/**
	 * CharCounter constructor initializes empty frequency table
	 */
	public CharCounter() {
		this.Table = new HashMap<Integer, Integer>();
		this.clear();
	}

	@Override
	public int getCount(int ch) {
		// if character is not in the frequency table, return 0
		if (!this.Table.containsKey(ch)) {
			return 0;
		}
		// else, return the frequency for the character as mapped in frequency table
		Integer val = this.Table.get(ch);
		return val;
	}

	@Override
	public int countAll(InputStream stream) throws IOException {
		int byte_count = 0;
		// read in bytes until you hit the end of the file
		try {
			int byte_read = stream.read();
			// for each byte read in, add it to the frequency map

			while (byte_read != -1) {
				// add() adds 1 to the count for existing characters, or adds a new entry
				this.add(byte_read);
				// increase byte count
				byte_count++;
				byte_read = stream.read();
			}
		} catch (IOException e) {
			System.out.print("ioexception");
		}
		// return count of bytes read in
		return byte_count;
	}

	@Override
	public void add(int i) {
		// if the integer is a key in the count table, increase its count by 1
		if (this.Table.containsKey(i)) {
			this.set(i, this.Table.get(i) + 1);
			// otherwise, add a new entry with the integer and a count of 1
		} else {
			this.Table.put(i, 1);
		}
	}

	@Override
	public void set(int i, int value) {
		this.Table.replace(i, value);

	}

	@Override
	public void clear() {
		Table.clear();
	}

	@Override
	public Map<Integer, Integer> getTable() {
		return this.Table;
	}

}
