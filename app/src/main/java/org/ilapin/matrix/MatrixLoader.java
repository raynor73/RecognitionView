/*******************************************************************************
 * Copyright 2016 Igor Lapin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ilapin.matrix;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MatrixLoader {

	public static double[][] load(final InputStream inputStream) throws IOException {
		final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));

		final int rows = dataInputStream.readInt();
		final int columns = dataInputStream.readInt();
		final double[][] matrix = new double[rows][columns];

		for (int j = 0; j < columns; j++) {
			for (int i = 0; i < rows; i++) {
				matrix[i][j] = dataInputStream.readDouble();
			}
		}

		return matrix;
	}
}
