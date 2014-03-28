package cat.lafosca.facecropper.ui;

import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import cat.lafosca.facecropper.R;
import cat.lafosca.facecropper.util.FaceCropper;


public class MainActivity extends ActionBarActivity {

    private Picasso mPicasso;
    private FaceCropper mFaceCropper;
    private ViewPager mViewPager;
    private LruCache mCache;
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
        setContentView(R.layout.activity_main);

        mFaceCropper = new FaceCropper(1f);
        mFaceCropper.setFaceMinSize(0);
        mCache = new LruCache(this);
        mPicasso = new Picasso.Builder(this).memoryCache(mCache).build();

        final ImageAdapter adapter = new ImageAdapter();
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                adapter.updateView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mCache.clear();
                mFaceCropper.setEyeDistanceFactorMargin((float) i / 10);
                adapter.updateView(mViewPager.getCurrentItem());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar.setProgress(10);
    }

    class ImageAdapter extends PagerAdapter {
//        private String[] urls = new String[] {
//                "https://lh6.googleusercontent.com/-T2Rh2NlkyAs/Ut0sXoTUQ-I/AAAAAAAAC-s/IiSrdL8uQTE/w1227-h920-no/IMG_20140120_150153.jpg",
//                "https://lh6.googleusercontent.com/-9MR9Bor8CS4/UukoxJqpBpI/AAAAAAAADA0/mBpxKgcMBY4/w690-h920-no/IMG_20140129_171350.jpg",
//                "http://lh4.ggpht.com/-9mrs8_wnkng/TyWXc74v_EI/AAAAAAAABqA/aMXk-ytB7t8/Mart%2525C3%2525AD%252520Marc%2525C3%2525B3.jpg?imgmax=640",
//                "http://www.nonada.es/wp-content/uploads/guillem-agullo-txt2.jpg",
//                "http://old.kaosenlared.net/img2/134/134641_flix_goi.jpg",
//                "http://1.bp.blogspot.com/-PeUq8jz5xEE/TutMy-d3liI/AAAAAAAAAKM/7lYfDeDxmj8/s1600/4303290026_6944019949_o.jpg"
//        };

        private int[] urls = new int[] {
                R.drawable.lluis1,
                R.drawable.vueling,
                R.drawable.arol1,
                R.drawable.git1,
                R.drawable.git2
        };

        @Override
        public int getCount() {
            return (urls == null) ? 0 : urls.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = getLayoutInflater().inflate(R.layout.pager_item, null, false);

            setupView(v, position);

            v.setTag(position);
            container.addView(v);
            return v;
        }

        public void setupView(View v, int position) {
            if (v == null) return;
            ImageView image = (ImageView) v.findViewById(R.id.imageView);
            ImageView imageCropped = (ImageView) v.findViewById(R.id.imageViewCropped);

            mPicasso.load(urls[position]).into(image);
            mPicasso.load(urls[position]).transform(mCropTransformation).into(imageCropped);
        }

        public void updateView(int position) {
            setupView(mViewPager.findViewWithTag(position), position);
        }
    }
}
