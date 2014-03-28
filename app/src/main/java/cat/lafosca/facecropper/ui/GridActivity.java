/*
 * Copyright 2014 ignasi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cat.lafosca.facecropper.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import cat.lafosca.facecropper.R;
import cat.lafosca.facecropper.util.FaceCropper;

/**
 * Created by ignasi on 28/03/14.
 */
public class GridActivity extends Activity {
    private Picasso mPicasso;
    private FaceCropper mFaceCropper;
    private LruCache mCache;
    private GridView mGridView;

    private Transformation mCropTransformation = new Transformation() {

        @Override
        public Bitmap transform(Bitmap source) {
            return mFaceCropper.cropFace(source);
        }

        @Override
        public String key() {
            return "crop()";
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        mFaceCropper = new FaceCropper(1f);
        mFaceCropper.setFaceMinSize(0);
        mCache = new LruCache(this);
        mPicasso = new Picasso.Builder(this).memoryCache(mCache).build();

        final ImageAdapter adapter = new ImageAdapter(this);
        mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setAdapter(adapter);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mCache.clear();
                mFaceCropper.setEyeDistanceFactorMargin((float) i / 10);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar.setMax(30);
        seekBar.setProgress(10);
    }

    class ImageAdapter extends BaseAdapter {
//        private String[] urls = new String[] {
//                "http://lh4.ggpht.com/-9mrs8_wnkng/TyWXc74v_EI/AAAAAAAABqA/aMXk-ytB7t8/Mart%2525C3%2525AD%252520Marc%2525C3%2525B3.jpg?imgmax=640",
//                "http://www.nonada.es/wp-content/uploads/guillem-agullo-txt2.jpg",
//                "http://old.kaosenlared.net/img2/134/134641_flix_goi.jpg",
//                "http://1.bp.blogspot.com/-PeUq8jz5xEE/TutMy-d3liI/AAAAAAAAAKM/7lYfDeDxmj8/s1600/4303290026_6944019949_o.jpg"
//        };

        private String[] urls = new String[] {
                "https://lh6.googleusercontent.com/-T2Rh2NlkyAs/Ut0sXoTUQ-I/AAAAAAAAC-s/IiSrdL8uQTE/w1227-h920-no/IMG_20140120_150153.jpg"
        };

        @Override
        public int getCount() {
            return (urls == null) ? 0 : urls.length;
        }

        @Override
        public String getItem(int i) {
            return urls[i];
        }

        @Override
        public long getItemId(int i) {
            return getItem(i).hashCode();
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            final ViewHolder vh;
            if ( convertView == null ) {
                View view = mInflater.inflate( R.layout.grid_item, parent, false );
                vh = new ViewHolder(view);
                view.setTag( vh );
            } else {
                vh = (ViewHolder)convertView.getTag();
            }

            mPicasso.load(getItem(i)).transform(mCropTransformation).into(vh.imageViewCropped);

            return vh.root;
        }

        public class ViewHolder {
            public final ImageView imageViewCropped;
            public final View root;

            public ViewHolder(View root) {
                imageViewCropped = (ImageView) root.findViewById(R.id.imageViewCropped);
                this.root = root;
            }
        }

        private LayoutInflater mInflater;

        // Constructors
        public ImageAdapter(Context context) {
            this.mInflater = LayoutInflater.from( context );
        }

    }
}
