package FID;

import Jama.Matrix;

/**
 * @author viswanathgs
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
	
	public static double EPS = 1e-9;
	
	public FIDTrainer(Matrix X, int r) {
		this.X = X;
		this.r = r;
		n = X.getRowDimension();
		m = X.getColumnDimension();
		
		W = new Matrix(n, r);
		H = new Matrix(r, m);
		
		// Initialize W and H with random values
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < r; j++) {
				W.set(i, j, Math.random());
			}
		}
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < m; j++) {
				H.set(i, j, Math.random());
			}
		}
		
		maxIterations = 100;
		tolerance = 0.01;
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
		
//		Matrix H0 = H.copy();
//		Matrix W0 = W.copy();
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
						H.set(i, j, X.get(i, j));
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
			if (euclideanDistanceSquare(X, W.times(H)) < tolerance) {
				break;
			}
			iterations++;
		}
	}
	
	void updateMatrixH() {
		// Compute X / (W*H) and return the result in _TempSizeX
        Matrix WH = W.times(H);
        Matrix X_WH = new Matrix(n, m);
        Matrix ones = new Matrix(n, m);
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < m; j++) {
        		X_WH.set(i, j, (X.get(i, j) + EPS) / (WH.get(i, j) + EPS));
        		ones.set(i, j, 1.0);
        	}
        } 
        // _TempSizeWTran <- W^T
        Matrix WT = W.transpose();
        // _TempSizeH <- W^T * (X / (W * H))        
        Matrix num = WT.times(X_WH);
        // _TempSizeH2 <- W^T * _OnesSizeX
        Matrix den = WT.times(ones);
        
        // _TempSizeH <- _TempSizeH / _TempSizeH2
        Matrix prod = new Matrix(r, m);
        for (int i = 0; i < r; i++) {
        	for (int j = 0; j < m; j++) {
        		prod.set(i, j, (num.get(i, j) + EPS) / (den.get(i, j) + EPS));
        	}
        }
        // _H <- _H * _TempSizeH
        for (int i = 0; i < r; i++) {
        	for (int j = 0; j < m; j++) {
        		H.set(i, j, H.get(i, j) * prod.get(i, j));
        	}
        }        
	}
	
	void updateMatrixW() {
		// Compute X / (W*H) and return the result in _TempSizeX
        Matrix WH = W.times(H);
        Matrix X_WH = new Matrix(n, m);
        Matrix ones = new Matrix(n, m);
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < m; j++) {
        		X_WH.set(i, j, (X.get(i, j) + EPS) / (WH.get(i, j) + EPS));
        		ones.set(i, j, 1.0);
        	}
        }
        // _TempSizeHTran <- H^T
        Matrix HT = H.transpose();
        // _TempSizeW <- _TempSizeX * H^T
        Matrix num = X_WH.times(HT);
        // _TempSizeW2 <- _OnesSizeX * H^T
        Matrix den = ones.times(HT);
        // _TempSizeW <- _TempSizeW / _TempSizeW2
        Matrix prod = new Matrix(n, r);
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < r; j++) {
        		prod.set(i, j, (num.get(i, j) + EPS) / (den.get(i, j) + EPS));
        	}
        }
        // _W <- _W * _TempSizeW
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < r; j++) {
        		W.set(i, j, W.get(i, j) * prod.get(i, j));
        	}
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
