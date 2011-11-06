package FID;

import Jama.Matrix;

/*
 * TODO
 * matrix class with threads (matrix mult optimize, iteration optimize)
 * 
 */

public class FIDTrainer {
	Matrix X;
	Matrix W;
	Matrix H;
	int n;
	int m;
	int r;
	int maxIterations;
	double tolerance;
	
	public FIDTrainer(Matrix X, int r) {
		this.X = X;
		this.r = r;
		n = X.getRowDimension();
		m = X.getColumnDimension();
		
		W = new Matrix(n, r);
		H = new Matrix(r, m);
		
		// Initialize W and H with random values
		System.out.println("Initial W");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < r; j++) {
				W.set(i, j, Math.random());
				System.out.print(W.get(i, j) + " ");
			}
			System.out.print("\n\n");
		}
		System.out.println("Initial H");
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < m; j++) {
				H.set(i, j, Math.random());
				System.out.print(H.get(i, j) + " ");
			}
			System.out.print("\n\n");
		}
		
		maxIterations = 100;
		tolerance = 1.0;
	}
	
	public FIDTrainer(Matrix X, int r, int maxIterations) {
		this(X, r);
		this.maxIterations = maxIterations;
	}
	
	public FIDTrainer(Matrix X, int r, int maxIterations, double tolerance) {
		this(X, r, maxIterations);
		this.tolerance = tolerance;
	}
	
	void factorizeNMF() throws IllegalArgumentException {
		int iterations = 0;
		
		while (iterations < maxIterations) {
			// Calculate W(transpose) * W;
			Matrix WTW = new Matrix(r, r);
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < r; j++) {
					double sum = 0.0;
					for (int k = 0; k < n; k++) {
						sum += W.get(k, i) * W.get(k, j);
					}
					WTW.set(i, j, sum);
				}
			}

			// Update matrix H
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < m; j++) {
					double numerator = 0.0;
					for (int k = 0; k < n; k++) {
						numerator += W.get(k, i) * X.get(k, j);
					}
					
					double denominator = 0.0;
					for (int k = 0; k < r; k++) {
						denominator += WTW.get(i, k) * H.get(k, j);
					}
					
					if (denominator != 0.0) {
						H.set(i, j, H.get(i, j) * numerator / denominator);
					}
					else {
						H.set(i, j, 0.0);
					}
				}
			}		
			
			//Calculate H * H(transpose)
			Matrix HHT = new Matrix(r, r);
			for (int i = 0; i < r; i++) {
				for (int j = 0; j < r; j++) {
					double sum = 0.0;
					for (int k = 0; k < m; k++) {
						sum += H.get(i, k) * H.get(j, k);
					}
					HHT.set(i, j, sum);
				}
			}
	
			// Update matrix W
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < r; j++) {
					double numerator = 0.0;
					for (int k = 0; k < m; k++) {
						numerator += X.get(i, k) * H.get(j, k);
					}
					
					double denominator = 0.0;
					for (int k = 0; k < r; k++) {
						denominator += W.get(i, k) * HHT.get(k, j);
					}
					
					if (denominator != 0.0) {
						W.set(i, j, W.get(i, j) * numerator / denominator);
					}
					else {
						W.set(i, j, 0.0);
					}
				}
			}
			
			// Check for convergence
			System.out.println("dist = " + euclideanDistanceSquare(X, W.times(H)));
			if (euclideanDistanceSquare(X, W.times(H)) < tolerance) {
				break;
			}
			iterations++;
		}
	}
	
	double euclideanDistanceSquare(Matrix A, Matrix B) throws IllegalArgumentException {
		double distance = 0.0;
		int p = A.getRowDimension();
		int q = A.getColumnDimension();
		
		if (p != B.getRowDimension() || q != B.getColumnDimension()) {
			throw new IllegalArgumentException("Matrix dimensions must agree");
		}
		
		for (int i = 0; i < p; i++) {
			for (int j = 0; j < q; j++) {
				distance += Math.pow((A.get(i, j) - B.get(i, j)), 2.0);
			}
		}
		
		return distance;
	}
	
	int getr() {
		return r;
	}
	
	int getn() {
		return n;
	}
	
	int getm() {
		return m;
	}
	
	Matrix getW() {
		return W;
	}
}
