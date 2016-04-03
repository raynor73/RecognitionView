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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RecognitionView extends View {

	private final static float STROKE_WIDTH = 2; //dp
	private final static int END_DRAWING_TIMEOUT = 500; //millis

//	private

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
				drawPath(canvas);
				break;

			case RECOGNIZING:
				drawPath(canvas);
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

	protected void drawPath(final Canvas canvas) {
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

		canvas.drawPath(mPath, mPaint);
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

	private class RecognitionTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(final Void... params) {
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
			return "42";
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
	}

	public enum State {
		IDLE, DRAWING, RECOGNIZING
	}
}
