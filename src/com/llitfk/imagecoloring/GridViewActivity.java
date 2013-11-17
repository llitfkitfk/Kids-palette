package com.llitfk.imagecoloring;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class GridViewActivity extends Activity {

	Integer[] imageIDs = { R.drawable.outline001_balloons,
			R.drawable.outline002_spaceship, R.drawable.outline003_horses,
			R.drawable.outline004_castle, R.drawable.outline005_house,
			R.drawable.outline006_dino, R.drawable.outline007_flowers,
			R.drawable.outline008_sealife, R.drawable.outline009_zoo,
			R.drawable.outline010_roadrunner, R.drawable.outline011_plane,
			R.drawable.outline012_birthday, R.drawable.outline013_18wheeler,
			R.drawable.outline014_motorbike, R.drawable.outline015_f15eagle,
			R.drawable.outline016_beagle, R.drawable.outline017_butterfly,
			R.drawable.outline018_snail, R.drawable.outline019_helicopter,
			R.drawable.outline020_bee, R.drawable.outline021_spider,
			R.drawable.outline022_medeival_city, R.drawable.test,
			R.drawable.outline023_outer_space, R.drawable.outline024_world_map };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		GridView gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(new ImageAdapter(this));

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				backToMainBoard(arg2);
				finish();
			}

			private void backToMainBoard(int arg2) {
				Intent intent = new Intent(GridViewActivity.this,
						MainColoringBoard.class);
				intent.putExtra(MainColoringBoard.INTENT_PIC_ID, imageIDs[arg2]);
				startActivity(intent);
			}
		});

	}

	public class ImageAdapter extends BaseAdapter {

		private Context context;

		public ImageAdapter(Context c) {
			context = c;
		}

		@Override
		public int getCount() {
			return imageIDs.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(context);
				imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
				// imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setPadding(5, 5, 5, 5);
			} else {
				imageView = (ImageView) convertView;
			}
			Options options = new Options();
			options.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					imageIDs[position], options);
			imageView.setImageBitmap(bitmap);
			return imageView;
		}

	}
}
