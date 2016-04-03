/*******************************************************************************
 * Copyright 2016 Igor Lapin
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ilapin.matrix;

public class MatrixUtils {

	public static double[][] matrixSum(final double[][] a, final double[][] b) {
		final double[][] c = new double[a.length][a[0].length];

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				c[i][j] = a[i][j] + b[i][j];
			}
		}

		return c;
	}

	public static double[][] matrixMultiply(final double[][] a, final double[][] b) {
		final double[][] c = new double[a.length][b[0].length];

		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c[0].length; j++) {
				double sum = 0;
				for (int k = 0; k < a[i].length; k++) {
					sum += a[i][k] * b[k][j];
				}
				c[i][j] = sum;
			}
		}

		return c;
	}

	public static double[][] matrixTranspose(final double[][] a) {
		final double[][] b = new double[a[0].length][a.length];

		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				b[i][j] = a[j][i];
			}
		}

		return b;
	}

	public static double[][] matrixTranspose(final double[] a) {
		final double[][] columnVector = new double[a.length][1];

		for (int i = 0; i < a.length; i++) {
			columnVector[i][0] = a[i];
		}

		return columnVector;
	}

	public static double[] matrixToVectorArray(final double[][] a) {
		final double[] vector;

		if (a.length == 1) {
			vector = new double[a[0].length];
			System.arraycopy(a[0], 0, vector, 0, vector.length);
		} else if (a[0].length == 1) {
			vector = new double[a.length];
			for (int i = 0; i < a.length; i++) {
				vector[i] = a[i][0];
			}
		} else {
			throw new IllegalArgumentException("Matrix is neither row nor column vector");
		}

		return vector;
	}
}
