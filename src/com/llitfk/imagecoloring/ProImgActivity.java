package com.llitfk.imagecoloring;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ProImgActivity extends Activity implements
		OnSeekBarChangeListener, OnTouchListener, OnClickListener {

	public static final String PHOTO_PATH = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
			+ String.valueOf(System.currentTimeMillis()) + ".jpg";

	Mat mat;
	private Bitmap bitmap;

	private SeekBar sbDrawCon;
	Button bDrawCon, bDone, bPen, bEraser;
	private ImageView ivOriginal;
	private Bitmap bmpResult;
	private Mat segMat;
	private Mat changeMat;

	private boolean isUsingPen = true;

	private static boolean isNotImageSeg = true;
	private static final int CAMERA_PIC_REQUEST = 1;
	Point stPoint = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.process_photo);

		initialViews();
		setOnlisteners();

		Intent intent = this.getIntent();
		String path = intent.getStringExtra(MainColoringBoard.INTENT_PIC_PATH);
		bitmap = BitmapFactory.decodeFile(path);
		bitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
		// bitmap = BitmapFactory.decodeResource(getResources(),
		// R.drawable.obama);
		ivOriginal.setImageBitmap(bitmap);
	}

	private void initialViews() {
		sbDrawCon = (SeekBar) findViewById(R.id.seekBar1);
		sbDrawCon.setMax(200);
		sbDrawCon.setProgress(50);

		ivOriginal = (ImageView) findViewById(R.id.imageView1);
		ivOriginal.setDrawingCacheEnabled(true);

		bDrawCon = (Button) findViewById(R.id.bDrawCon);
		bDone = (Button) findViewById(R.id.bDone);
		bEraser = (Button) findViewById(R.id.bEraser);
		bPen = (Button) findViewById(R.id.bPen);
	}

	private void setOnlisteners() {
		sbDrawCon.setOnSeekBarChangeListener(this);
		ivOriginal.setOnTouchListener(this);

		bDone.setOnClickListener(this);
		bDrawCon.setOnClickListener(this);
		bPen.setOnClickListener(this);
		bEraser.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bPen:
			if (!bPen.isSelected()) {
				setAllSelected(false);
				bPen.setSelected(true);
				isUsingPen = true;
			}
			break;
		case R.id.bEraser:
			if (!bEraser.isSelected()) {
				setAllSelected(false);
				bEraser.setSelected(true);
				isUsingPen = false;
			}
			break;
		case R.id.bDone:
			if (!bDone.isSelected()) {
				setAllSelected(false);
				bDone.setSelected(true);
				backToMainBoard();
			}
			break;
		case R.id.bDrawCon:
			if (!bDrawCon.isSelected()) {
				setAllSelected(false);
				bDrawCon.setSelected(true);
			}
			if (isNotImageSeg) {
				segMat = segImage();
				isNotImageSeg = false;
			}

			Mat getMat = findContours(segMat);
			updateResult(getMat);

			break;
		}
	}

	private void setAllSelected(boolean b) {
		bDone.setSelected(b);
		bDrawCon.setSelected(b);
		bEraser.setSelected(b);
		bPen.setSelected(b);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float eventX = event.getX();
		float eventY = event.getY();

		if (isNotImageSeg) {
			Toast.makeText(getBaseContext(), "Processing Image",
					Toast.LENGTH_LONG).show();
			segMat = segImage();
			isNotImageSeg = false;
			Mat getMat = findContours(segMat);
			updateResult(getMat);
		}
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			Point startPoint = new Point(eventX, eventY);
			changeMat = changeMat(changeMat, startPoint);
			break;
		case MotionEvent.ACTION_MOVE:
			Point movePoint = new Point(eventX, eventY);

			changeMat = changeMat(changeMat, movePoint);
			break;
		case MotionEvent.ACTION_UP:
			Point endPoint = new Point(eventX, eventY);

			changeMat = changeMat(changeMat, endPoint);
			stPoint = null;
			break;
		}
		updateResult(changeMat);
		return true;
	}

	private Mat changeMat(Mat originalMat, Point point) {
		if (isUsingPen) {
			if (stPoint == null) {
				Core.circle(originalMat, point, 2, Scalar.all(0), Core.FILLED);
			} else {
				Core.line(originalMat, stPoint, point, Scalar.all(0), 2, Core.LINE_4, 0);
			}
			stPoint = point;
		} else {
			Core.circle(originalMat, point, 10, Scalar.all(255), Core.FILLED);
		}
		return originalMat;
	}

	private Mat preProcessing() {
		Utils.bitmapToMat(bitmap, mat);
		Mat reMat = mat.clone();
		return reMat;
	}

	private void backToMainBoard() {

		saveTofile();
		Intent intent = new Intent(ProImgActivity.this, MainColoringBoard.class);
		intent.putExtra(MainColoringBoard.INTENT_PIC_PATH, PHOTO_PATH);
		startActivity(intent);
		ivOriginal.destroyDrawingCache();
	}

	private void saveTofile() {
		Bitmap bitmap = ivOriginal.getDrawingCache();

		File file = new File(PHOTO_PATH);
		try {
			file.createNewFile();
			FileOutputStream ostream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, ostream);
			ostream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Mat segImage() {
		final Mat segMat = preProcessing();

		final ProgressDialog dialog = ProgressDialog.show(this, "Segmentation",
				"Please Waiting...", true);
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					TermCriteria termCriteria = new TermCriteria(
							TermCriteria.MAX_ITER + TermCriteria.EPS, 5, 1);
					if (segMat.channels() != 3) {
						Imgproc.cvtColor(segMat, segMat,
								Imgproc.COLOR_RGBA2RGB, 3);
					}
					Imgproc.pyrMeanShiftFiltering(segMat, segMat, 20, 20, 1,
							termCriteria);
					
//					Thread.sleep(5000);
					dialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		return segMat;

	}

	private Mat findContours(Mat preMat) {
		Mat originalMat = preMat.clone();
		Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_RGB2GRAY);

		Imgproc.Canny(originalMat, originalMat, sbDrawCon.getProgress(),
				sbDrawCon.getProgress() * 2, 3, false);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Mat hierarchy = new Mat(originalMat.cols(), originalMat.rows(),
				CvType.CV_8UC1, new Scalar(3));

		int mode = Imgproc.RETR_TREE;
		int method = Imgproc.CHAIN_APPROX_SIMPLE;
		Imgproc.findContours(originalMat, contours, hierarchy, mode, method);

		Mat imgeBackground = new Mat(originalMat.size(), CvType.CV_8U,
				new Scalar(255, 255, 255));

		for (int i = 0; i < contours.size(); i++) {
			Scalar color = new Scalar(0, 0, 0);
			Imgproc.drawContours(imgeBackground, contours, i, color, Core.LINE_4);
		}

		changeMat = imgeBackground;
		return imgeBackground;
	}

	private void updateResult(Mat matToBmp) {
		if (bmpResult == null) {
			bmpResult = Bitmap.createBitmap(matToBmp.cols(), matToBmp.rows(),
					Bitmap.Config.RGB_565);
		}

		Utils.matToBitmap(matToBmp, bmpResult);

		ivOriginal.setImageBitmap(bmpResult);
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
			handleCameraPhoto(data);
		}
	}

	private void handleCameraPhoto(Intent data) {
		Intent photoIntent = new Intent(this, ProImgActivity.class);
		photoIntent.putExtra(MainColoringBoard.INTENT_PIC_PATH, PHOTO_PATH);
		startActivity(photoIntent);
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				mat = new Mat();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch (seekBar.getId()) {
		case R.id.seekBar1:
			progress = progress + 1;
			break;
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (isNotImageSeg) {
			segMat = segImage();
			isNotImageSeg = false;
		}
		Mat trackingMat = findContours(segMat);
		updateResult(trackingMat);
	}

}
