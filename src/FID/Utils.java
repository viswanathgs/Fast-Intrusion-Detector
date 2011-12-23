package FID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author viswanathgs
 * 
 * Helper class. Utility functions for experiments.
 */

public class Utils {
	public static void parseSystemCalls() throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader("syscalls.dat"));
		BufferedWriter fout = new BufferedWriter(new FileWriter("out.dat"));
		
		String line;
		StringTokenizer lineTokenizer;
		StringTokenizer lineTokenizer2;
		while ((line = fin.readLine()) != null) {
			lineTokenizer = new StringTokenizer(line);
			lineTokenizer2 = new StringTokenizer(lineTokenizer.nextToken(), "(");
			fout.write(lineTokenizer2.nextToken() + "\n");
			fout.flush();
		}
	}
	
	public static void printSystemCallIDs(String fileName, String outFile) throws IOException {
		BufferedReader finSysCalls = new BufferedReader(new FileReader("syscalls.dat"));
		Map<String, Integer> sysCallMap = new HashMap<String, Integer>();
		String line;
		int z = 1;
		while ((line = finSysCalls.readLine()) != null) {
			sysCallMap.put(line, z);
			z++;
		}
		
		BufferedReader fin = new BufferedReader(new FileReader(fileName));
		BufferedWriter fout = new BufferedWriter(new FileWriter(outFile));
		while ((line = fin.readLine()) != null) {
			StringTokenizer tokenizer = new StringTokenizer(line, " ");
			int pID = Integer.parseInt(tokenizer.nextToken());
			BufferedReader finStrace = new BufferedReader(new FileReader(tokenizer.nextToken()));
			
			String line2;
			while ((line2 = finStrace.readLine()) != null) {
				StringTokenizer tokenizer2 = new StringTokenizer(line2, "(");
				String sysCall = tokenizer2.nextToken();
				if (sysCallMap.containsKey(sysCall) == false) {
					continue;
				}
				fout.write(pID + " " + sysCallMap.get(sysCall) + "\n");
			}
			fout.flush();
		}
	}
}
