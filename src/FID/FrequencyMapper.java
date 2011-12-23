package FID;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;

import Jama.Matrix;

/**
 * @author viswanathgs
 */

public class FrequencyMapper {
	Map<Integer, Vector<Integer> > frequencyTable;
	
	// Maximum number of system calls in Linux kernel. Hard-coded.
	public static final int MAX_SYSTEM_CALLS = 400;
	
	public FrequencyMapper(String processLog) throws IOException {
		BufferedReader processLogFin = new BufferedReader(new FileReader(processLog));
		frequencyTable = new HashMap<Integer, Vector<Integer> >();
		
		// Create the table of count of each system call for each process
		String line;
		StringTokenizer lineTokens;
		while ((line = processLogFin.readLine()) != null) {
			lineTokens = new StringTokenizer(line);
			
			// Each line should be <PID> <System Call ID>
			if (lineTokens.countTokens() != 2) {
				throw new IOException("Unknown data format. Expected format: <PID> <System call ID>");
			}
			
			int pID = Integer.parseInt(lineTokens.nextToken());
			int systemCallID = Integer.parseInt(lineTokens.nextToken());
			
			// System call ID should be in the range [1..maxSystemCalls]
			if (systemCallID < 1 || systemCallID > MAX_SYSTEM_CALLS) {
				throw new IOException("Invalid system call ID");
			}
			
			if (!frequencyTable.containsKey(pID)) {
				Vector<Integer> frequencyList = new Vector<Integer>();
				for (int i = 0; i < MAX_SYSTEM_CALLS; i++) {
					frequencyList.add(0);
				}
				
				frequencyTable.put(pID, frequencyList);
			}
			
			Vector<Integer> systemCallFrequency = frequencyTable.get(pID);
			systemCallFrequency.set(systemCallID, systemCallFrequency.get(systemCallID) + 1);
		}
	}
	
	public Matrix getX() {
		int n = MAX_SYSTEM_CALLS;
		int m = frequencyTable.size();
		Matrix X = new Matrix(n, m);
		
		// Create matrix X from frequencyTable
		Iterator<Entry<Integer, Vector<Integer> > > iter = frequencyTable.entrySet().iterator();
		int j = 0;
		while (iter.hasNext()) {
			Map.Entry<Integer, Vector<Integer> > pairs = (Entry<Integer, Vector<Integer>>) iter.next();
			Vector<Integer> systemCallFrequency = pairs.getValue();
			
			double totalFrequency = 0.0;
			for (int i = 0; i < n; i++) {
				totalFrequency += systemCallFrequency.get(i);
			}
			for (int i = 0; i < n; i++) {
				X.set(i, j, (double) systemCallFrequency.get(i) / totalFrequency); 
			}
			
			j++;
		}
		
		return X;
	}
	
	Vector<Integer> getPIDList() {
		Vector<Integer> pidList = new Vector<Integer>();
		
		Iterator<Entry<Integer, Vector<Integer> > > iter = frequencyTable.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Vector<Integer> > pairs = (Entry<Integer, Vector<Integer>>) iter.next();
			pidList.add(pairs.getKey());
		}
		
		return pidList;
	}
}
