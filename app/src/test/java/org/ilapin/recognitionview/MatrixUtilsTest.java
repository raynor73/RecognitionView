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
package org.ilapin.recognitionview;

import junit.framework.Assert;

import org.ilapin.matrix.MatrixUtils;
import org.junit.Test;

public class MatrixUtilsTest {
	@Test
	public void testSum() {
		final double[][] a = {
				{1, 2, 3},
				{4, 5, 6},
				{7, 8, 9},
		};
		final double[][] b = {
				{10, 20, 30},
				{40, 50, 60},
				{70, 80, 90},
		};
		final double[][] expectedResult = {
				{11, 22, 33},
				{44, 55, 66},
				{77, 88, 99},
		};

		final double[][] actualResult = MatrixUtils.matrixSum(a, b);
		for (int i = 0; i < expectedResult.length; i++) {
			for (int j = 0; j < expectedResult[0].length; j++) {
				Assert.assertEquals(expectedResult[i][j], actualResult[i][j], 0.00001);
			}
		}
	}

	@Test
	public void testMultiply() {
		final double[][] a = {
				{1, 2, 3, 4},
				{5, 6, 7, 8},
				{9, 10, 11, 12},
		};
		final double[][] b = {
				{10, 20, 30},
				{40, 50, 60},
				{70, 80, 90},
				{100, 110, 120},
		};
		final double[][] expectedResult = {
				{700, 800, 900},
				{1580, 1840, 2100},
				{2460, 2880, 3300},
		};
		final double[][] actualResult = MatrixUtils.matrixMultiply(a, b);
		Assert.assertEquals(expectedResult.length, actualResult.length);
		Assert.assertEquals(expectedResult[0].length, actualResult[0].length);
		for (int i = 0; i < expectedResult.length; i++) {
			for (int j = 0; j < expectedResult[0].length; j++) {
				Assert.assertEquals(expectedResult[i][j], actualResult[i][j], 0.00001);
			}
		}
	}

	@Test
	public void testTranspose() {
		final double[][] a = {
				{1, 2, 3, 4},
				{5, 6, 7, 8},
				{9, 10, 11, 12},
		};
		final double[][] expectedResult = {
				{1, 5, 9},
				{2, 6, 10},
				{3, 7, 11},
				{4, 8, 12},
		};
		final double[][] actualResult = MatrixUtils.matrixTranspose(a);
		Assert.assertEquals(expectedResult.length, actualResult.length);
		Assert.assertEquals(expectedResult[0].length, actualResult[0].length);
		for (int i = 0; i < expectedResult.length; i++) {
			for (int j = 0; j < expectedResult[0].length; j++) {
				Assert.assertEquals(expectedResult[i][j], actualResult[i][j], 0.00001);
			}
		}
	}

	@Test
	public void testRowToColumnVectorTranspose() {
		final double[] a = {1, 2, 3, 4};
		final double[][] expectedResult = {
				{1},
				{2},
				{3},
				{4},
		};
		final double[][] actualResult = MatrixUtils.matrixTranspose(a);
		Assert.assertEquals(expectedResult.length, actualResult.length);
		Assert.assertEquals(expectedResult[0].length, actualResult[0].length);
		for (int i = 0; i < expectedResult.length; i++) {
			for (int j = 0; j < expectedResult[0].length; j++) {
				Assert.assertEquals(expectedResult[i][j], actualResult[i][j], 0.00001);
			}
		}
	}

	@Test
	public void testMatrixToVectorArrayConversion() {
		final double[][] a = {
				{1, 2, 3, 4}
		};
		final double[][] b = {
				{1},
				{2},
				{3},
				{4},
		};
		final double[] expectedResult = {1, 2, 3, 4};

		final double[] actualResult0 = MatrixUtils.matrixToVectorArray(a);
		Assert.assertEquals(expectedResult.length, actualResult0.length);
		for (int i = 0; i < expectedResult.length; i++) {
			Assert.assertEquals(expectedResult[i], actualResult0[i], 0.00001);
		}

		final double[] actualResult1 = MatrixUtils.matrixToVectorArray(b);
		Assert.assertEquals(expectedResult.length, actualResult1.length);
		for (int i = 0; i < expectedResult.length; i++) {
			Assert.assertEquals(expectedResult[i], actualResult1[i], 0.00001);
		}
	}
}
