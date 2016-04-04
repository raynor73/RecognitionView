package org.ilapin.recognitionview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final RecognitionView recognitionView = (RecognitionView) findViewById(R.id.view_recognizer);
		final ImageView debugImageView = (ImageView) findViewById(R.id.view_debug);
		//noinspection ConstantConditions
		recognitionView.setListener(new RecognitionView.Listener() {
			@Override
			public void onStateChanged(final RecognitionView.State state) {

			}

			@Override
			public void onRecognitionResult(final String result) {
				Log.d("!@#", result);
			}

			@Override
			public void onDebugBitmap(final Bitmap bitmap) {
				//noinspection ConstantConditions
				debugImageView.setImageBitmap(bitmap);
			}
		});
	}
}
