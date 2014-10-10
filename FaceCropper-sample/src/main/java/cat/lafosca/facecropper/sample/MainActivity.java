package cat.lafosca.facecropper.sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import cat.lafosca.facecropper.FaceCropper;

public class MainActivity extends ActionBarActivity {

    private Picasso mPicasso;
    private FaceCropper mFaceCropper;
    private ViewPager mViewPager;

    private Transformation mDebugCropTransformation = new Transformation() {

        @Override
        public Bitmap transform(Bitmap source) {
            return mFaceCropper.getFullDebugImage(source);
        }

        @Override
        public String key() {
            StringBuilder builder = new StringBuilder();

            builder.append("faceDebugCrop(");
            builder.append("minSize=").append(mFaceCropper.getFaceMinSize());
            builder.append(",maxFaces=").append(mFaceCropper.getMaxFaces());

            FaceCropper.SizeMode mode = mFaceCropper.getSizeMode();
            if (FaceCropper.SizeMode.EyeDistanceFactorMargin.equals(mode)) {
                builder.append(",distFactor=").append(mFaceCropper.getEyeDistanceFactorMargin());
            } else if (FaceCropper.SizeMode.FaceMarginPx.equals(mode)) {
                builder.append(",margin=").append(mFaceCropper.getFaceMarginPx());
            }

            return builder.append(")").toString();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFaceCropper = new FaceCropper(1f);
        mFaceCropper.setFaceMinSize(0);
        mFaceCropper.setMaxFaces(1);
        mFaceCropper.setEyeDistanceFactorMargin(10);
        mFaceCropper.setDebug(true);
        mPicasso = Picasso.with(this);

        final ImageAdapter adapter = new ImageAdapter();
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(adapter);
    }

    class ImageAdapter extends PagerAdapter {

        private int[] urls = new int[] {
                R.drawable.lluis1,
                R.drawable.vueling,
                R.drawable.arol1,
                R.drawable.git1,
                R.drawable.git2,
                R.drawable.face01,
                R.drawable.face02,
                R.drawable.face03
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

            ImageView image = (ImageView) v.findViewById(R.id.imageView);
            TopCropImage imageCropped = (TopCropImage) v.findViewById(R.id.imageViewCropped);

            mPicasso.load(urls[position]).transform(mDebugCropTransformation).into(image);

            mPicasso.load(urls[position])
                    .config(Bitmap.Config.RGB_565)
                    .transform(new CropTransformation(mFaceCropper, imageCropped))
                    .into(imageCropped);

            container.addView(v);

            return v;
        }
    }
}
