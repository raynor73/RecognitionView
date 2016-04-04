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
package org.ilapin.recognitionview;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import org.ilapin.matrix.MatrixLoader;
import org.ilapin.matrix.MatrixUtils;
import org.ilapin.neuralnetwork.NeuralNetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecognitionView extends View {

	private final static float STROKE_WIDTH = 2; //dp
	private final static int END_DRAWING_TIMEOUT = 500; //millis
	private final static int RECOGNIZED_IMAGE_ROWS = 28;
	private final static int RECOGNIZED_IMAGE_COLUMNS = 28;

	private static NeuralNetwork sNeuralNetwork;

	private State mState = State.IDLE;

	private Listener mListener;

	private final Path mPath = new Path();
	private Paint mPaint = new Paint();

	private final List<List<PointF>> mSegments = new ArrayList<>();

	private final Runnable mEndOfDrawingRoutine = new Runnable() {

		@Override
		public void run() {
			changeState(State.RECOGNIZING);
			invalidate();

			new RecognitionTask().execute();
		}
	};

	private Bitmap buildBitmapForRecognition() {
		final Paint paint = new Paint();
		paint.setColor(0xff000000);
		paint.setStrokeWidth(0);
		paint.setStyle(Paint.Style.STROKE);
		final Bitmap bitmap = Bitmap.createBitmap(RECOGNIZED_IMAGE_COLUMNS, RECOGNIZED_IMAGE_ROWS,
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);

		final Matrix matrix = new Matrix();
		matrix.reset();
		matrix.setScale((float) RECOGNIZED_IMAGE_COLUMNS / getWidth(), (float) RECOGNIZED_IMAGE_ROWS / getHeight());

		drawPath(canvas, paint, matrix);

		return bitmap;
	}

	public RecognitionView(final Context context) {
		this(context, null, 0);
	}

	public RecognitionView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RecognitionView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init();
	}

	private void init() {
		mPaint.setColor(0xff000000);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STROKE_WIDTH, getResources().getDisplayMetrics())
		);
	}

	public void setPaint(final Paint paint) {
		mPaint = paint;
	}

	public void setListener(final Listener listener) {
		mListener = listener;
	}

	public void heavyInit() {
		final AssetManager assetManager = getContext().getAssets();
		final double[][] inputLayerWeights, layerWeights, inputBiases, layerBiases, gain, keep, xOffset;
		try {
			inputLayerWeights = MatrixLoader.load(assetManager.open("input_layer_weights"));
			layerWeights = MatrixLoader.load(assetManager.open("layer_weights"));
			inputBiases = MatrixLoader.load(assetManager.open("input_biases"));
			layerBiases = MatrixLoader.load(assetManager.open("layer_biases"));
			gain = MatrixLoader.load(assetManager.open("gain"));
			keep = MatrixLoader.load(assetManager.open("keep"));
			xOffset = MatrixLoader.load(assetManager.open("xoffset"));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		final double[] keepInputsIndexesDoubleArray = MatrixUtils.matrixToVectorArray(keep);
		final int[] keepInputsIndexes = new int[keepInputsIndexesDoubleArray.length];
		for (int i = 0; i < keepInputsIndexes.length; i++) {
			keepInputsIndexes[i] = (int) keepInputsIndexesDoubleArray[i];
		}
		sNeuralNetwork = new NeuralNetwork(
				keep[0].length,
				inputLayerWeights.length,
				layerWeights.length,
				keepInputsIndexes
		);
		sNeuralNetwork.setInputsWeights(inputLayerWeights);
		sNeuralNetwork.setInputBiases(inputBiases);
		sNeuralNetwork.setLayerWeights(layerWeights);
		sNeuralNetwork.setLayerBiases(layerBiases);
		sNeuralNetwork.setXOffset(xOffset);
		sNeuralNetwork.setGain(gain);
		sNeuralNetwork.setYMin(-1);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		switch (mState) {
			case IDLE:
				changeState(State.DRAWING);

			case DRAWING:
				processDrawingEvent(event);
				return true;

			case RECOGNIZING:
			default:
				return false;
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		switch (mState) {
			case DRAWING:
				drawPath(canvas, mPaint);
				break;

			case RECOGNIZING:
				drawPath(canvas, mPaint);
				applyDimming(canvas);
				break;
		}
	}

	protected void changeState(final State newState) {
		mState = newState;
		if (mListener != null) {
			mListener.onStateChanged(mState);
		}
	}

	protected void applyDimming(final Canvas canvas) {
		canvas.drawARGB(0x80, 0xff, 0xff, 0xff);
	}

	protected void drawPath(final Canvas canvas, final Paint paint) {
		drawPath(canvas, paint, new Matrix());
	}

	protected void drawPath(final Canvas canvas, final Paint paint, final Matrix matrix) {
		mPath.reset();

		for (final List<PointF> segmentPoints : mSegments) {
			for (int i = 0; i < segmentPoints.size(); i++) {
				final PointF point = segmentPoints.get(i);
				if (i > 0) {
					mPath.lineTo(point.x, point.y);
				} else {
					mPath.moveTo(point.x, point.y);
				}
			}
		}

		mPath.transform(matrix);

		canvas.drawPath(mPath, paint);
	}

	private void processDrawingEvent(final MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				continueDrawing();
				createAndSaveSegment(event);
				break;

			case MotionEvent.ACTION_MOVE:
				continueDrawing();
				if (!mSegments.isEmpty()) {
					final List<PointF> currentSegment = mSegments.get(mSegments.size() - 1);
					currentSegment.add(new PointF(event.getX(), event.getY()));
				} else {
					createAndSaveSegment(event);
				}
				break;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				awaitForEndOfDrawing();
		}
		invalidate();
	}

	private void continueDrawing() {
		removeCallbacks(mEndOfDrawingRoutine);
	}

	private void awaitForEndOfDrawing() {
		postDelayed(mEndOfDrawingRoutine, END_DRAWING_TIMEOUT);
	}

	private void createAndSaveSegment(final MotionEvent event) {
		final ArrayList<PointF> segment = new ArrayList<>();
		segment.add(new PointF(event.getX(), event.getY()));
		mSegments.add(segment);
	}

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private class RecognitionTask extends AsyncTask<Void, Void, String> {

		@Override
		@SuppressWarnings("ResourceType")
		protected String doInBackground(final Void... params) {
			final Bitmap bitmap = buildBitmapForRecognition();

			if (mListener != null) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mListener.onDebugBitmap(bitmap);
					}
				});
			}

			if (sNeuralNetwork == null) {
				heavyInit();
			}

			final double[] inputs = new double[RECOGNIZED_IMAGE_ROWS * RECOGNIZED_IMAGE_COLUMNS];
			for (int j = 0; j < RECOGNIZED_IMAGE_COLUMNS; j++) {
				for (int i = 0; i < RECOGNIZED_IMAGE_ROWS; i++) {
					inputs[i * RECOGNIZED_IMAGE_COLUMNS + j] = 0xff - (bitmap.getPixel(i, j) & 0xff);
				}
			}

			final double[] outputs = sNeuralNetwork.calculateOutputs(inputs);

			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < outputs.length; i++) {
				if (i > 0) {
					sb.append("|");
				}
				sb.append(String.format("%d: %f", i, outputs[i]));
			}

			return sb.toString();
		}

		@Override
		protected void onPostExecute(final String result) {
			changeState(State.IDLE);
			mSegments.clear();
			if (mListener != null) {
				mListener.onRecognitionResult(result);
			}
			invalidate();
		}
	}

	public interface Listener {

		void onStateChanged(final State state);

		void onRecognitionResult(final String result);

		void onDebugBitmap(final Bitmap bitmap);
	}

	public enum State {
		IDLE, DRAWING, RECOGNIZING
	}
}
