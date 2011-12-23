package FID;

import Jama.Matrix;

/**
 * @author viswanathgs
 */

public class FIDDetector {
	Matrix W;
	Matrix WTW;
	int n;
	int r;
	int maxIterations;
	
	public FIDDetector(Matrix W) {
		this.W = W;
		n = W.getRowDimension();
		r = W.getColumnDimension();
		if (n != FrequencyMapper.MAX_SYSTEM_CALLS) {
			throw new IllegalArgumentException("Dimensions do not agree");
		}
		
		// Calculate W(transpose) * W;
		WTW = new Matrix(r, r);
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < r; j++) {
				double sum = 0.0;
				for (int k = 0; k < n; k++) {
					sum += W.get(k, i) * W.get(k, j);
				}
				WTW.set(i, j, sum);
			}
		}
		
		maxIterations = 100;
	}
	
	public FIDDetector(Matrix W, int maxIterations) {
		this(W);
		this.maxIterations = maxIterations;
	}
	
	double getAnomalyIndex(double[] X) throws IllegalArgumentException {
		// X should be a column matrix representing system call frequencies
		if (X.length != FrequencyMapper.MAX_SYSTEM_CALLS) {
			throw new IllegalArgumentException("Dimensions do not agree");
		}
		
		// Compute W(transpose) * X
		double[] WTX = new double[r];
		for (int i = 0; i < r; i++) {
			WTX[i] = 0.0;
			for (int j = 0; j < n; j++) {
				WTX[i] += W.get(j, i) * X[j];
			}
		}
		
		// Initialize random values for H
		double[] H = new double[r];
		for (int i = 0; i < r; i++) {
			H[i] = Math.random();
		}
		
		int iterations = 0;
		while (iterations < maxIterations) {
			for (int i = 0; i < r; i++) {
				double denominator = 0.0;
				for (int j = 0; j < r; j++) {
					denominator += WTW.get(i, j) * H[j];
				}
				
				H[i] = H[i] * WTX[i] / denominator;
			}
			
			iterations++;
		}
		
		double d = 0.0;
		for (int i = 0; i < r; i++) {
			d += H[i];
		}
		return Math.abs(d - 1.0);
	}
}
