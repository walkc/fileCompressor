import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author clairewalker
 *
 */
public class Huff implements ITreeMaker, IHuffEncoder, IHuffHeader, IHuffModel {
	private CharCounter c = new CharCounter();
	private Map<Integer, String> codesMap;
	private HuffTree hufftree;
	private int headerSizeVal;
	private HuffTree reconstructedTree;
	private int compressedFileSize;

	@Override
	public HuffTree makeHuffTree(InputStream stream) throws IOException {
		// get the charcounter map of characters
		c.countAll(stream);
		Map<Integer, Integer> map = c.getTable();

		// create priority queue
		PriorityQueue<HuffTree> pq = new PriorityQueue<HuffTree>();

		// create leaf nodes for each of the elements and add them to PQ
		for (Map.Entry<Integer, Integer> el : map.entrySet()) {
			HuffTree hln = new HuffTree(el.getKey(), el.getValue());
			pq.add(hln);
		}

		// add the pseudo eof
		HuffTree pseudo = new HuffTree(256, 1);
		pq.add(pseudo);

		// build the tree
		HuffTree ht = buildTree(pq);

		// return the tree
		this.hufftree = ht;
		return ht;
	}

	/*
	 * Private helper method for makeHuffTree Gets the first two HuffTrees from the
	 * priority queue, builds a new hufftree with those trees as left child and
	 * right child and with a weight of their combined weights. returns the new
	 * HuffTree
	 */
	private HuffTree buildTree(PriorityQueue<HuffTree> Hheap) {

		HuffTree tmp1, tmp2, tmp3 = null;

		while (Hheap.size() > 1) { // While two items left
			tmp1 = Hheap.poll();
			tmp2 = Hheap.poll();
			tmp3 = new HuffTree(tmp1.root(), tmp2.root(), tmp1.weight() + tmp2.weight());
			Hheap.add(tmp3); // Return new tree to heap
		}
		return tmp3; // Return the tree
	}

	@Override
	public Map<Integer, String> makeTable() {
		// create a new map to hold the chars and their encodings
		Map<Integer, String> cMap = new HashMap<Integer, String>();

		// call helper function createCodes() to build the codes from the huffman tree
		return createCodes(this.hufftree.root(), "", cMap);

	}

	/**
	 * Recursive function to build codes for each character in the huffman tree and
	 * return a Map<Integer, String> of these codes to makeTable()
	 */
	private Map<Integer, String> createCodes(IHuffBaseNode ht, String s, Map<Integer, String> cMap) {
		// if the node is null, return the codes map
		if (ht == null) {
			this.codesMap = cMap;
			return cMap;
		}
		// if the node is a leaf, add the char and the built string code to the map
		if (ht.isLeaf()) {
			HuffLeafNode hf = ((HuffLeafNode) ht);
			cMap.put((hf.element()), s);
			this.codesMap = cMap;
			return cMap;
		}
		// else, node is an internal node
		// recurse on the left child and add a "0" to the string code
		// recurse on the right child and add a "1" to the string code
		else {
			HuffInternalNode hi = ((HuffInternalNode) ht);
			createCodes(hi.left(), s + "0", cMap);
			createCodes(hi.right(), s + "1", cMap);
		}

		// set this code map to the codesMap variable and return it
		this.codesMap = cMap;
		return cMap;

	}

	@Override
	public String getCode(int i) {

		// check if it's in the map. If not, return null
		if (!this.codesMap.containsKey(i)) {
			return null;
		}

		// else, return the mapping for the character from our codesMap
		return this.codesMap.get(i);

	}

	@Override
	public Map<Integer, Integer> showCounts() {
		return c.getTable();

	}

	@Override
	public int headerSize() {
		return this.headerSizeVal;
	}

	@Override
	public int writeHeader(BitOutputStream out) {
		int count = 0;
		// write the magic number, and increase the bit count by the size of the magic
		// header
		out.write(BITS_PER_INT, MAGIC_NUMBER);
		count += BITS_PER_INT;

		// write the compressed tree by calling the compressHuffTree helper method
		// increase the bit count by the size of the compressed hufftree
		count += (compressHuffTree(this.hufftree.root(), out, 0));

		// set the count of bits to the headerSizeVal variable and return it
		this.headerSizeVal = count;
		return count;
	}

	/**
	 * Helper method to compress our hufftree and return the size, called by
	 * writeHeader Returns size of the compressed hufftree
	 */
	private int compressHuffTree(IHuffBaseNode ht, BitOutputStream out, int count) {
		// if the node is null, return the count
		if (ht == null) {
			return count;
		}
		// if node is a leaf, write "1" and then 9 bits for the character's ascii code
		if (ht.isLeaf()) {
			HuffLeafNode hf = ((HuffLeafNode) ht);
			out.write(1, 1);
			out.write(9, hf.element());
			// return the size of 10, as 10 bits have been written to file
			return 10;
		}
		// else, if node is internal, write "0" to file
		out.write(1, 0);
		// return 1 (for the 0 bit written) plus the recursive sum of the left child
		// node and right child node
		return 1 + compressHuffTree(((HuffInternalNode) ht).left(), out, count)
				+ compressHuffTree(((HuffInternalNode) ht).right(), out, count);
	}

	/*
	 * Helper method to get the size of compressed tree without writing it To be
	 * called when determining if compressed file is larger than original file
	 * Called by checkIfCompressedVersionOfFileIsSmaller(). Returns size of
	 * compressed tree
	 */
	private int compressHuffTreeWithoutWriting(IHuffBaseNode ht, int count) {
		// if the node is null, return the count
		if (ht == null) {
			return count;
		}
		// if node is a leaf, return 10
		if (ht.isLeaf()) {
			return 10;
		}
		// return 1 (for the 0 bit written) plus the recursive sum of the left child
		// node and right child node
		return 1 + compressHuffTreeWithoutWriting(((HuffInternalNode) ht).left(), count)
				+ compressHuffTreeWithoutWriting(((HuffInternalNode) ht).right(), count);
	}

	/**
	 * Helper method to check if a file should be compressed. If the file's
	 * compressed size is smaller than it's original size, return true. Else returns
	 * false. Called by write()
	 */
	private boolean checkIfCompressedVersionOfFileIsSmaller(String inFile) {

		File f = new File(inFile);
		// get the size of the original file
		long lengthOfFile = f.length();
		// set bit counters. runningCountOfBits will represent the total bits required
		// to write the compressed file
		int runningCountOfBits = 0;
		int countForSingleLetter = 0;

		// calculate the size of the compressed tree
		// for each letter, multiple its count with the length of its compression code
		for (Map.Entry<Integer, String> e : this.codesMap.entrySet()) {
			// if the code is the pseudo_EOF, simply add the length of its compression code
			if (e.getKey() == PSEUDO_EOF) {
				countForSingleLetter = e.getValue().length();

			} else {
				countForSingleLetter = e.getValue().length() * c.getCount(e.getKey());
			}
			// add the bits for the single character to the total count of bits for the
			// compressed file
			runningCountOfBits += countForSingleLetter;
		}

		// Add header size (size of compressed tree + magic number size)
		runningCountOfBits += BITS_PER_INT + compressHuffTreeWithoutWriting(this.hufftree.root(), 0);

		compressedFileSize = runningCountOfBits;
		// length of file returns # bytes, so need to compare bits to bits
		if (compressedFileSize < lengthOfFile * 8) {
			return true;
		}
		return false;
	}

	@Override
	public int write(String inFile, String outFile, boolean force) {
		String code;
		int bit_count;
		BitInputStream in = new BitInputStream(inFile);
		BitInputStream in1 = new BitInputStream(inFile);

		// build the hufftree from the input stream
		try {
			makeHuffTree(in);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// make the frequency table
		makeTable();

		if (force == false) {
			// check if compressed version is smaller
			boolean check = checkIfCompressedVersionOfFileIsSmaller(inFile);
			// if the compressed file is not smaller, then close the input stream and return
			// the size required for the compressed file
			if (!check) {
				in.close();
				in1.close();
				return compressedFileSize;
			}
		}

		// create a bitInputStream from the outFile
		BitOutputStream out = new BitOutputStream(outFile);

		// write the header
		bit_count = writeHeader(out);

		// write the rest of the file, looking up its code in the hufftree using getCode
		int byte_read;
		int char_index;
		int bit;
		try {
			// read through the inputstream a byte at a time
			byte_read = in1.read(BITS_PER_WORD);
			while (byte_read != -1) {
				// for each byte (char) read, get its compression code
				code = getCode(byte_read);
				// write the code out bit by bit
				char_index = 0;
				while (char_index < code.length()) {
					bit = code.charAt(char_index);
					out.write(1, bit);
					// increase the bit count for each bit written
					bit_count++;
					char_index++;
				}
				byte_read = in1.read(BITS_PER_WORD);
			}

			// write the pseudo EOF
			// get the pseudo EOF code
			code = getCode(PSEUDO_EOF);
			char_index = 0;
			// write that code to the file bit by bit
			while (char_index < code.length()) {
				bit = code.charAt(char_index);
				out.write(1, bit);
				bit_count++;
				char_index++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// close all the files and return the total count of bits written
		in.close();
		in1.close();
		out.close();
		return bit_count;
	}

	@Override
	public HuffTree readHeader(BitInputStream in) throws IOException {
		// read in the magic number
		int magicNumberCheck = in.read(BITS_PER_INT);

		// check that the magic number is right
		if (magicNumberCheck != MAGIC_NUMBER) {
			throw new IOException();
		}

		// read in the tree and reconstruct it. make each 0 an internal node, and 1 a
		// leaf node with element
		reconstructedTree = buildTreeFromBitsRead(in);

		// check number of bits read in is correct. what are we supposed to be comparing
		// this against for a file we know nothing about?
		return reconstructedTree;
	}

	/**
	 * Helper method to build a hufftree from a compressed file Called by
	 * readHeader() function Reads in a compressed hufftree from a BitInputStream
	 * and uncompresses the tree Returns the uncompressed huffTree
	 */
	private HuffTree buildTreeFromBitsRead(BitInputStream in) throws IOException {
		HuffTree ht = null;
		int bit_read;
		int char_read;

		// read from the input stream one bit at a time
		while (true) {
			bit_read = in.read(1);

			// if the bit read is -1, then there is no pseudo EOF. Throw an IOEXCEPTION
			if (bit_read == -1) {
				throw new IOException("unexpected end of input file");
			}

			// if the bit is a zero, create an internal node and recurse to get its left
			// child node and right child node
			if ((bit_read & 1) == 0) {
				ht = new HuffTree(buildTreeFromBitsRead(in).root(), buildTreeFromBitsRead(in).root(), 0);
				break;

			}
			// if the bit is a one, create a leaf node and return
			// break out of the loop if its the EOF
			else {
				char_read = in.read(9);
				if (char_read == PSEUDO_EOF) {
					ht = new HuffTree(PSEUDO_EOF, 0);
					break;
				} else {
					ht = new HuffTree(char_read, 0);
					return ht;
				}

			}
		}

		// return the uncompressed huffman tree
		return ht;
	}

	@Override
	public int uncompress(String inFile, String outFile) {
		// create input and output streams
		BitInputStream in = new BitInputStream(inFile);
		BitOutputStream out = new BitOutputStream(outFile);
		String code = "";
		int bitCount = 0;

		try {
			// read in the header
			readHeader(in);
			// set the hufftree to the uncompressed tree
			this.hufftree = reconstructedTree;
			// make frequency table based on this hufftree
			makeTable();

			// read in a bit from the compressed file
			int bit_read = in.read(1);

			// read from the compressed file
			while (true) {
				// if we reach the end of the file, there is no pseudo eof. throw an exception
				if (bit_read == -1) {
					throw new IOException();
				}

				// else, start to construct the compressed code bit by bit
				code = code + bit_read;
				// if code is in map, look up character in the codesMap and write it
				// if code is pseudo eof, close files and return the count of bits
				if (this.codesMap.containsValue(code)) {
					for (Map.Entry<Integer, String> el : this.codesMap.entrySet()) {
						if (el.getValue().equals(code)) {
							if (el.getKey().equals(PSEUDO_EOF)) {
								in.close();
								out.close();

								return bitCount;
							} else {
								out.write(BITS_PER_INT, el.getKey());
								bitCount += 8;

								code = "";
								break;
							}
						}
					}
				}
				// else, keep reading until get a valid compression code
				bit_read = in.read(1);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		in.close();
		out.close();
		return bitCount;
	}

}
