package FID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import Jama.Matrix;

/**
 * @author viswanathgs
 *
 */

public class FastIntrusionDetector {
	
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		Utils.printSystemCallIDs("straces.dat", "detect.dat");
		
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		if (args[0].equals("train")) {
			if (args.length < 4) {
				printUsage();
				System.exit(1);
			}
			
			FrequencyMapper frequencyMapper = new FrequencyMapper(args[1]);
			Matrix X = frequencyMapper.getX();
			
			FIDTrainer fidTrainer;
			if (args.length == 4) {
				fidTrainer = new FIDTrainer(X, Integer.parseInt(args[3]));
			}
			else {
				fidTrainer = new FIDTrainer(X, Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			}
			fidTrainer.factorizeNMF();
			
			Matrix W = fidTrainer.getW();
			
			BufferedWriter fout = new BufferedWriter(new FileWriter(args[2]));
			
			int n = W.getRowDimension();
			int r = W.getColumnDimension();
			fout.write(n + " " + r + "\n");
			
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < r; j++) {
					fout.write(W.get(i, j) + " ");
				}
				fout.write("\n");
				fout.flush();
			}
		}
		else if (args[0].equals("detect")) {
			if (args.length != 5) {
				printUsage();
				System.exit(1);
			}
			
			BufferedReader fin = new BufferedReader(new FileReader(args[1]));
			String line;
			StringTokenizer lineTokenizer;
			
			line = fin.readLine();
			lineTokenizer = new StringTokenizer(line);
			int n = Integer.parseInt(lineTokenizer.nextToken());
			int r = Integer.parseInt(lineTokenizer.nextToken());
			
			Matrix W = new Matrix(n, r);
			for (int i = 0; i < n; i++) {
				line = fin.readLine();
				lineTokenizer = new StringTokenizer(line);
				for (int j = 0; j < r; j++) {
					W.set(i, j, Double.parseDouble(lineTokenizer.nextToken()));
				}
			}
		
			FIDDetector fidDetector = new FIDDetector(W, Integer.parseInt(args[3]));
			
			FrequencyMapper frequencyMapper = new FrequencyMapper(args[2]);
			Matrix X = frequencyMapper.getX();
			Vector<Integer> pidList = frequencyMapper.getPIDList();
			
			System.out.println("PID\tAnomaly Index\tIntrusion\n");
			double[] frequency = new double[X.getRowDimension()];
			for (int j = 0; j < X.getColumnDimension(); j++) {
				for (int i = 0; i < X.getRowDimension(); i++) {
					frequency[i] = X.get(i, j);
				}
				
				double anomalyIndex = fidDetector.getAnomalyIndex(frequency);
				System.out.print(pidList.get(j) + "\t" + Math.abs(1.0 - anomalyIndex) + "\t");
				if (Math.abs(1.0 - anomalyIndex) < Double.parseDouble(args[4])) {
					System.out.println("Yes");
				}
				else {
					System.out.println("No");
				}
			}
			
		}
		else {
			printUsage();
			System.exit(1);
		} 
	}

	static void printUsage() {
		System.out.println("Usage: ");
		System.out.println("java -jar <JAR File> train <Input_File> <Output_File> <r> [<Max_Iterations>]");
		System.out.println("java -jar <JAR File> detect <Train_File> <Input_File> <Max_Iterations> <Anomaly_Index>");
	}
}
