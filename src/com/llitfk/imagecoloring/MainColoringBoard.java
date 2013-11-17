package com.llitfk.imagecoloring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainColoringBoard extends Activity implements OnClickListener,
		OnTouchListener {

	private static final String TAG = "ImageColoringDemo::Activity";

	public static final String INTENT_PIC_ID = "from intent";

	Button bGallery, bSave, bLoading, bEraser, bFreeStyle, bBack, bCamera;
	Button bGreen, bBlue, bYellow, bOrange, bRed, bDarkblue, bPink, bLightpink;

	ImageView ivColoringPic;

	public static final String CAMERA_INTENT = "FromCamera";

	public static final String PIC_PATH = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
			+ String.valueOf(System.currentTimeMillis()) + ".jpg";

	private final static int CAMERA_PIC_REQUEST = 1337;
	private final static int GALERRY_PIC_REQUEST = 1336;

	private static final String INTENT_START_NEW = "com.llitfk.imagecoloring.GRIDVIEWACTIVITY";
	public static final String INTENT_PIC_PATH = "DrawActivity";
	// public static final String PIC_DIR = Environment
	// .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
	// .getPath();

	private static boolean randomPainting = true;
	Mat mat;
	Bitmap bitmap;
	Mat preMat;
	Mat matResult;
	private Bitmap bmpResult;
	List<Point> points = new ArrayList<Point>();
	List<Scalar> scalars = new ArrayList<Scalar>();

	Scalar drawScalar = getRandomScalar(3);

	private static boolean isBmpToMat = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_coloring_board);

		initViews();
		setListener();
		bFreeStyle.setPadding(0, 45, 0, 0);
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();

		// loadColoringImage((Integer) bundle.get(INTENT_PIC_ID));
		int i = bundle.getInt(INTENT_PIC_ID, 0);
		String test = bundle.getString(INTENT_PIC_PATH);
		if (i != 0) {
			bitmap = BitmapFactory.decodeResource(getResources(), i);
			ivColoringPic.setImageBitmap(bitmap);
		}

		if (test != null) {
			bitmap = BitmapFactory.decodeFile(test);
			ivColoringPic.setImageBitmap(bitmap);
		}

		resetParameter();

	}

	private void resetParameter() {
		points.clear();
		scalars.clear();
		randomPainting = true;
		isBmpToMat = false;
		if (bmpResult != null) {
			bmpResult.recycle();
			bmpResult = null;
		}
	}

	private void initViews() {
		// image button
		bBlue = (Button) findViewById(R.id.bBlue);
		bDarkblue = (Button) findViewById(R.id.bDarkblue);
		bGreen = (Button) findViewById(R.id.bGreen);
		bLightpink = (Button) findViewById(R.id.bLightpink);
		bPink = (Button) findViewById(R.id.bPink);
		bOrange = (Button) findViewById(R.id.bOrange);
		bRed = (Button) findViewById(R.id.bRed);
		bYellow = (Button) findViewById(R.id.bYellow);
		ivColoringPic = (ImageView) findViewById(R.id.ivColoringPic);

		bSave = (Button) findViewById(R.id.bSave);
		bEraser = (Button) findViewById(R.id.bEraser);
		bFreeStyle = (Button) findViewById(R.id.bFreestyle);
		bBack = (Button) findViewById(R.id.bUndo);
		bLoading = (Button) findViewById(R.id.bLoading);
		// camera
		bGallery = (Button) findViewById(R.id.bTakePic);
		bCamera = (Button) findViewById(R.id.bTakePic2);
	}

	private void setListener() {
		bGreen.setOnClickListener(this);
		bBlue.setOnClickListener(this);
		bYellow.setOnClickListener(this);
		bOrange.setOnClickListener(this);
		bRed.setOnClickListener(this);
		bDarkblue.setOnClickListener(this);
		bPink.setOnClickListener(this);
		bLightpink.setOnClickListener(this);

		bSave.setOnClickListener(this);
		bLoading.setOnClickListener(this);
		bEraser.setOnClickListener(this);
		bFreeStyle.setOnClickListener(this);
		bBack.setOnClickListener(this);

		ivColoringPic.setOnTouchListener(this);

		bGallery.setOnClickListener(this);
		bCamera.setOnClickListener(this);
	}

	private void preProcessing() {
		Utils.bitmapToMat(bitmap, mat);
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		preMat = mat.clone();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		float eventX = event.getX();
		float eventY = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!isBmpToMat) {
				preProcessing();
				isBmpToMat = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			Point endPoint = new Point(eventX, eventY);
			coloringImage(preMat, endPoint);
			points.add(endPoint);
			scalars.add(drawScalar);
			updateResult();
			break;
		}

		return true;
	}

	private void updateResult() {

		if (bmpResult == null) {
			bmpResult = Bitmap.createBitmap(matResult.width(),
					matResult.height(), Bitmap.Config.RGB_565);
		}

		Utils.matToBitmap(matResult, bmpResult);

		ivColoringPic.setImageBitmap(bmpResult);

		if (randomPainting) {
			drawScalar = getRandomScalar(3);
		}

	}

	private void coloringImage(Mat originalMat, Point point) {
		if (originalMat.channels() != 3) {
			Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_RGBA2RGB,
					3);
		}

		Mat mask = new Mat(originalMat.rows() + 2, originalMat.cols() + 2,
				CvType.CV_8UC1, new Scalar(0));

		Imgproc.floodFill(originalMat, mask, point, drawScalar, new Rect(),
				Scalar.all(30), Scalar.all(30), Imgproc.FLOODFILL_FIXED_RANGE);
		preMat = originalMat;
		matResult = originalMat;

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bTakePic2:
			clickCamera(CAMERA_PIC_REQUEST);
			break;
		case R.id.bFreestyle:
			if (!bFreeStyle.isSelected()) {
				bFreeStyle.setSelected(true);
				resetAllButton();
				bFreeStyle.setPadding(0, 45, 0, 0);
				drawScalar = getRandomScalar(3);
				randomPainting = true;

			}

			DisplayToast("blue button selected");

			break;
		case R.id.bUndo:
			if (!bBack.isSelected()) {
				setAllSelected(false);
				bBack.setSelected(true);
				if (points.size() > 0) {
					points.remove(points.size() - 1);
					scalars.remove(scalars.size() - 1);
				}
				reDraw();
				updateResult();
			} else {
				if (points.size() > 0) {
					points.remove(points.size() - 1);
					scalars.remove(scalars.size() - 1);
				}
				reDraw();
				updateResult();
			}
			break;
		case R.id.bLoading:
			if (!bLoading.isSelected()) {
				setAllSelected(false);
				bLoading.setSelected(true);
				startActivity(new Intent(INTENT_START_NEW));
			}
			break;
		case R.id.bSave:
			if (!bSave.isSelected()) {
				setAllSelected(false);
				bSave.setSelected(true);
				saveImage();
			}
			break;
		case R.id.bTakePic:
			if (!bGallery.isSelected()) {
				setAllSelected(false);
				bGallery.setSelected(true);
				clickGallery(GALERRY_PIC_REQUEST);
			}
			break;
		case R.id.bEraser:
			if (!bEraser.isSelected()) {
				setAllSelected(false);
				bEraser.setSelected(true);
				randomPainting = false;
				drawScalar = new Scalar(255, 255, 255);
			}
			break;
		case R.id.bBlue:
			changePen(bBlue);
			break;
		case R.id.bDarkblue:
			changePen(bDarkblue);
			break;
		case R.id.bGreen:
			changePen(bGreen);
			break;
		case R.id.bLightpink:
			changePen(bLightpink);
			break;
		case R.id.bOrange:
			changePen(bOrange);
			break;
		case R.id.bPink:
			changePen(bPink);
			break;
		case R.id.bRed:
			changePen(bRed);
			break;
		case R.id.bYellow:
			changePen(bYellow);
			break;
		}
	}

	private void reDraw() {
		Mat originalMat = mat.clone();
		if (originalMat.channels() != 3) {
			Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_RGBA2RGB,
					3);
		}

		Mat mask = new Mat(originalMat.rows() + 2, originalMat.cols() + 2,
				CvType.CV_8UC1, new Scalar(0));
		for (int i = points.size() - 1; i > -1; i--) {
			Imgproc.floodFill(originalMat, mask, points.get(i), scalars.get(i),
					new Rect(), Scalar.all(30), Scalar.all(30),
					Imgproc.FLOODFILL_FIXED_RANGE);
		}
		preMat = originalMat;
		matResult = originalMat;
	}

	private void setAllSelected(boolean b) {
		bGallery.setSelected(b);
		bSave.setSelected(b);
		bLoading.setSelected(b);
		bEraser.setSelected(b);
		bFreeStyle.setSelected(b);
		bBack.setSelected(b);
		bBlue.setSelected(b);
		bDarkblue.setSelected(b);
		bGreen.setSelected(b);
		bLightpink.setSelected(b);
		bOrange.setSelected(b);
		bPink.setSelected(b);
		bRed.setSelected(b);
		bYellow.setSelected(b);
	}

	private void changePen(Button button) {

		if (!button.isSelected()) {
			setAllSelected(false);
			button.setSelected(true);
			resetAllButton();
			button.setPadding(0, 45, 0, 0);

		}
		drawScalar = getRealScalar((String) button.getTag());
		randomPainting = false;
		DisplayToast("blue button selected");
	}

	private void resetAllButton() {
		bFreeStyle.setPadding(0, 0, 0, 0);
		bBlue.setPadding(0, 0, 0, 0);
		bDarkblue.setPadding(0, 0, 0, 0);
		bGreen.setPadding(0, 0, 0, 0);
		bLightpink.setPadding(0, 0, 0, 0);
		bOrange.setPadding(0, 0, 0, 0);
		bPink.setPadding(0, 0, 0, 0);
		bRed.setPadding(0, 0, 0, 0);
		bYellow.setPadding(0, 0, 0, 0);
	}

	private Scalar getRealScalar(String tag) {
		String[] array = tag.split(" ");
		Scalar scalar = new Scalar(Double.parseDouble(array[0]),
				Double.parseDouble(array[1]), Double.parseDouble(array[2]));
		return scalar;
	}

	private void saveImage() {
		AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
		saveDialog.setTitle("Save drawing");
		saveDialog.setMessage("Save drawing to device Gallery?");
		saveDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// save drawing
						ivColoringPic.setDrawingCacheEnabled(true);
						// attempt to save
						String imgSaved = MediaStore.Images.Media.insertImage(
								getContentResolver(),
								ivColoringPic.getDrawingCache(), UUID
										.randomUUID().toString() + ".png",
								"drawing");
						// feedback
						if (imgSaved != null) {
							Toast savedToast = Toast.makeText(
									getApplicationContext(),
									"Drawing saved to Gallery!",
									Toast.LENGTH_SHORT);
							savedToast.show();
						} else {
							Toast unsavedToast = Toast.makeText(
									getApplicationContext(),
									"Oops! Image could not be saved.",
									Toast.LENGTH_SHORT);
							unsavedToast.show();
						}
						ivColoringPic.destroyDrawingCache();
					}
				});
		saveDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		saveDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == GALERRY_PIC_REQUEST) {
				fromGallery(data);
			}
			if (requestCode == CAMERA_PIC_REQUEST) {
				fromCamera(data);
			}
		}
	}

	private void fromGallery(Intent data) {
		String path = "";
		Uri mImageCaptureUri = data.getData();
		path = getRealPathFromURI(mImageCaptureUri);

		Intent photoIntent = new Intent(this, ProImgActivity.class);
		photoIntent.putExtra(INTENT_PIC_PATH, path);
		startActivity(photoIntent);
	}

	private void fromCamera(Intent data) {
		// Bitmap newBitmap = BitmapFactory.decodeFile(PIC_PATH);
		Intent photoIntent = new Intent(this, ProImgActivity.class);
		photoIntent.putExtra(INTENT_PIC_PATH, PIC_PATH);
		startActivity(photoIntent);
	}

	public String getRealPathFromURI(Uri contentUri) {
		String res = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(contentUri, proj, null,
				null, null);

		if (cursor == null)
			return null;
		if (cursor.moveToFirst()) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
		}

		return res;
	}

	private Scalar getRandomScalar(int i) {
		switch (i) {
		case 1:
			return new Scalar(new Random().nextInt(255));
		case 3:
			return new Scalar(new Random().nextInt(255),
					new Random().nextInt(255), new Random().nextInt(255));
		case 4:
			return new Scalar(new Random().nextInt(255),
					new Random().nextInt(255), new Random().nextInt(255),
					new Random().nextInt(255));
		default:
			break;
		}
		return null;

	}

	private void clickGallery(int actionCode) {
		Intent intent = new Intent();

		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(
				Intent.createChooser(intent, "Complete action using"),
				actionCode);
	}

	private void clickCamera(int actionCode) {
		Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File photo = new File(PIC_PATH);
		photo.getParentFile().mkdirs();
		takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		startActivityForResult(takePicIntent, actionCode);
	}

	public void DisplayToast(String string) {

		Toast.makeText(MainColoringBoard.this, string, Toast.LENGTH_SHORT)
				.show();
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
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
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

}
