package cat.lafosca.facecropper.sample;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.squareup.picasso.Transformation;

import cat.lafosca.facecropper.FaceCropper;

/**
 * Created by david on 8/10/14.
 */
public class CropTransformation implements Transformation {
    private FaceCropper mFaceCropper;
    private PointF faceCenter;
    private TopCropImage v;

    public CropTransformation(FaceCropper mFaceCropper, TopCropImage v) {
        this.mFaceCropper = mFaceCropper;
        this.v = v;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        FaceCropper.CropResult cropResult = mFaceCropper.getCroppedResult(source);
        faceCenter = cropResult.getmCroppedCenterFace();
        v.setFaceCenter(faceCenter);

        return cropResult.getCroppedBitmap();
    }

    @Override
    public String key() {
        return System.currentTimeMillis() + "";
    }
}