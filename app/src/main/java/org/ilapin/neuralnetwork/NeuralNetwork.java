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
package org.ilapin.neuralnetwork;

import org.ilapin.matrix.MatrixUtils;

import java.util.Arrays;

public class NeuralNetwork {

	private final double[][] mInputWeights;
	private final double[][] mInputBiases; // column vector
	private final double[][] mLayerWeights;
	private final double[][] mLayerBiases; // column vector
	private final double[][] mXOffset; // column vector
	private final double[][] mGain; // column vector
	private final int[] mKeepInputsIndexes;

	private double mYMin;

	public NeuralNetwork(final int inputsNumber,
						 final int inputNeuronsNumber,
						 final int outputsNumber, final int[] keepInputsIndexes) {
		if (inputsNumber <= 0) {
			throw new IllegalArgumentException("Inputs number is less than or equal to zero");
		}
		if (inputNeuronsNumber <= 0) {
			throw new IllegalArgumentException("Input layer's neurons number is less than or equal to zero");
		}
		if (outputsNumber <= 0) {
			throw new IllegalArgumentException("Outputs number is less than or equal to zero");
		}

		mInputWeights = new double[inputNeuronsNumber][inputsNumber];
		mInputBiases = new double[inputNeuronsNumber][1];
		mLayerWeights = new double[outputsNumber][inputNeuronsNumber];
		mLayerBiases = new double[outputsNumber][1];
		mXOffset = new double[inputsNumber][1];
		mGain = new double[inputsNumber][1];
		mKeepInputsIndexes = Arrays.copyOf(keepInputsIndexes, keepInputsIndexes.length);
	}

	public double[] calculateOutputs(final double[] inputs) {
		final double[][] filteredInputs = new double[mKeepInputsIndexes.length][1];
		for (int i = 0; i < filteredInputs.length; i++) {
			filteredInputs[i][0] = inputs[mKeepInputsIndexes[i]];
		}

		final double[][] normalizedInputs = minMaxApply(filteredInputs, mGain, mXOffset, mYMin);
		final double[][] a1 = sigmoidApply(
				MatrixUtils.matrixSum(mInputBiases, MatrixUtils.matrixMultiply(mInputWeights, normalizedInputs))
		);
		final double[][] a2 = softMaxApply(
				MatrixUtils.matrixSum(mLayerBiases, MatrixUtils.matrixMultiply(mLayerWeights, a1))
		);

		return MatrixUtils.matrixToVectorArray(a2);
	}

	public void setYMin(final double yMin) {
		mYMin = yMin;
	}

	public void setInputsWeights(final double[][] inputWeights) {
		if (mInputWeights.length != inputWeights.length || mInputWeights[0].length != inputWeights[0].length) {
			final String msg = String.format(
					"Input weights do not fit neural network. Expected rows: %d; columns: %d. Actual rows: %d; columns: %d",
					mInputWeights.length, mInputWeights[0].length,
					inputWeights.length, inputWeights[0].length
			);
			throw new IllegalArgumentException(msg);
		}

		for (int i = 0; i < mInputWeights.length; i++) {
			System.arraycopy(inputWeights[i], 0, mInputWeights[i], 0, mInputWeights[i].length);
		}
	}

	public void setLayerWeights(final double[][] layerWeights) {
		if (mLayerWeights.length != layerWeights.length || mLayerWeights[0].length != layerWeights[0].length) {
			throw new IllegalArgumentException("Layer weights do not fit neural network");
		}

		for (int i = 0; i < mLayerWeights.length; i++) {
			System.arraycopy(layerWeights[i], 0, mLayerWeights[i], 0, mLayerWeights[i].length);
		}
	}

	public void setXOffset(final double[][] xOffset) {  // column vector
		if (mXOffset.length != xOffset.length || mXOffset[0].length != xOffset[0].length) {
			throw new IllegalArgumentException("X offsets do not fit neural network");
		}

		copyColumnVector(xOffset, mXOffset);
	}

	public void setGain(final double[][] gain) {
		if (mGain.length != gain.length || mGain[0].length != gain[0].length) {
			throw new IllegalArgumentException("Gains do not fit neural network");
		}

		copyColumnVector(gain, mGain);
	}

	public void setInputBiases(final double[][] biases) { // column vector
		if (mInputBiases.length != biases.length || mInputBiases[0].length != biases[0].length) {
			throw new IllegalArgumentException("Input biases not fit neural network");
		}

		copyColumnVector(biases, mInputBiases);
	}

	public void setLayerBiases(final double[][] biases) {
		if (mLayerBiases.length != biases.length || mLayerBiases[0].length != biases[0].length) {
			throw new IllegalArgumentException("Layer biases not fit neural network");
		}

		copyColumnVector(biases, mLayerBiases);
	}

	private double[][] minMaxApply(final double[][] x, // column vector
								   final double[][] gain, // column vector
								   final double[][] xOffset, // column vector
								   final double yMin) {
		final double[][] y = new double[x.length][1];

		for (int i = 0; i < y.length; i++) {
			y[i][0] = (x[i][0] - xOffset[i][0]) * gain[i][0] + yMin;
		}

		return y;
	}

	private double[][] sigmoidApply(final double[][] x) { // column vector
		final double[][] y = new double[x.length][1];

		for (int i = 0; i < y.length; i++) {
			y[i][0] = sigmoid(x[i][0]);
		}

		return y;
	}

	private double[][] softMaxApply(final double x[][]) { // column vector
		final double[][] y = new double[x.length][1];
		final double[][] numerator = new double[x.length][1];

		final double maxValue = findMax(x);
		for (int i = 0; i < y.length; i++) {
			y[i][0] = x[i][0] - maxValue;
			numerator[i][0] = Math.exp(y[i][0]);
		}

		double denominator = calculateSum(numerator);
		if (denominator == 0) {
			denominator = 1;
		}

		for (int i = 0; i < y.length; i++) {
			y[i][0] = numerator[i][0] / denominator;
		}

		return y;
	}

	private double sigmoid(final double x) {
		return 2.0 / (1 + Math.exp(-2 * x)) - 1;
	}

	private double findMax(final double[][] x) { // column vector
		double max = Double.MIN_VALUE;

		for (final double[] currentX : x) {
			if (currentX[0] > max) {
				max = currentX[0];
			}
		}

		return max;
	}

	private double calculateSum(final double[][] x) { // column vector
		double sum = 0;

		for (final double[] currentX : x) {
			sum += currentX[0];
		}

		return sum;
	}

	private void copyColumnVector(final double[][] src, final double[][] dst) { // column vector
		for (int i = 0; i < dst.length; i++) {
			dst[i][0] = src[i][0];
		}
	}
}
