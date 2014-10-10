/*
 * Copyright (C) 2014 lafosca Studio, SL
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

package cat.lafosca.facecropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

/**
 * An utility that crops faces from bitmaps.
 * It support multiple faces (max 8 by default) and crop them all, fitted in the same image.
 */
public class FaceCropper {

    private static final String LOG_TAG = FaceCropper.class.getSimpleName();

    public enum SizeMode { FaceMarginPx, EyeDistanceFactorMargin };

    private static final int MAX_FACES = 8;
    private static final int MIN_FACE_SIZE = 200;

    private int mFaceMinSize = MIN_FACE_SIZE;
    private int mFaceMarginPx = 100;
    private float mEyeDistanceFactorMargin = 2f;
    private int mMaxFaces = MAX_FACES;
    private SizeMode mSizeMode = SizeMode.EyeDistanceFactorMargin;
    private boolean mDebug;
    private Paint mDebugPainter;
    private Paint mDebugAreaPainter;

    public FaceCropper() {
        initPaints();
    }

    public FaceCropper(int faceMarginPx) {
        setFaceMarginPx(faceMarginPx);
        initPaints();
    }

    public FaceCropper(float eyesDistanceFactorMargin) {
        setEyeDistanceFactorMargin(eyesDistanceFactorMargin);
        initPaints();
    }

    private void initPaints() {
        mDebugPainter = new Paint();
        mDebugPainter.setColor(Color.RED);
        mDebugPainter.setAlpha(80);

        mDebugAreaPainter = new Paint();
        mDebugAreaPainter.setColor(Color.GREEN);
        mDebugAreaPainter.setAlpha(80);
    }

    public int getMaxFaces() {
        return mMaxFaces;
    }

    public void setMaxFaces(int maxFaces) {
        this.mMaxFaces = maxFaces;
    }

    public int getFaceMinSize() {
        return mFaceMinSize;
    }

    public void setFaceMinSize(int faceMinSize) {
        mFaceMinSize = faceMinSize;
    }

    public int getFaceMarginPx() {
        return mFaceMarginPx;
    }

    public void setFaceMarginPx(int faceMarginPx) {
        mFaceMarginPx = faceMarginPx;
        mSizeMode = SizeMode.FaceMarginPx;
    }

    public SizeMode getSizeMode() {
        return mSizeMode;
    }

    public float getEyeDistanceFactorMargin() {
        return mEyeDistanceFactorMargin;
    }

    public void setEyeDistanceFactorMargin(float eyeDistanceFactorMargin) {
        mEyeDistanceFactorMargin = eyeDistanceFactorMargin;
        mSizeMode = SizeMode.EyeDistanceFactorMargin;
    }

    public boolean isDebug() {
        return mDebug;
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    protected CropResult cropFace(Bitmap original, boolean debug) {
        Bitmap fixedBitmap = BitmapUtils.forceEvenBitmapSize(original);
        fixedBitmap = BitmapUtils.forceConfig565(fixedBitmap);
        Bitmap mutableBitmap = fixedBitmap.copy(Bitmap.Config.RGB_565, true);

        if (fixedBitmap != mutableBitmap) {
            fixedBitmap.recycle();
        }

        FaceDetector faceDetector = new FaceDetector(
                mutableBitmap.getWidth(), mutableBitmap.getHeight(),
                mMaxFaces);

        FaceDetector.Face[] faces = new FaceDetector.Face[mMaxFaces];

        // The bitmap must be in 565 format (for now).
        int faceCount = faceDetector.findFaces(mutableBitmap, faces);

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, faceCount + " faces found");
        }

        if (faceCount == 0) {
            return new CropResult(mutableBitmap);
        }

        int initX = mutableBitmap.getWidth();
        int initY = mutableBitmap.getHeight();
        int endX = 0;
        int endY = 0;

        PointF centerFace = new PointF();

        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawBitmap(mutableBitmap, new Matrix(), null);

        // Calculates minimum box to fit all detected faces
        for (int i = 0; i < faceCount; i++) {
            FaceDetector.Face face = faces[i];

            // Eyes distance * 3 usually fits an entire face
            int faceSize = (int) (face.eyesDistance() * 3);

            if (SizeMode.FaceMarginPx.equals(mSizeMode)) {
                faceSize += mFaceMarginPx * 2; // *2 for top and down/right and left effect
            }
            else if (SizeMode.EyeDistanceFactorMargin.equals(mSizeMode)) {
                faceSize += face.eyesDistance() * mEyeDistanceFactorMargin;
            }

            faceSize = Math.max(faceSize, mFaceMinSize);

            face.getMidPoint(centerFace);

            if (debug) {
                canvas.drawPoint(centerFace.x, centerFace.y, mDebugPainter);
                canvas.drawCircle(centerFace.x, centerFace.y, face.eyesDistance() * 1.5f, mDebugPainter);
            }

            int tInitX = (int) (centerFace.x - faceSize / 2);
            int tInitY = (int) (centerFace.y - faceSize / 2);
            tInitX = Math.max(0, tInitX);
            tInitY = Math.max(0, tInitY);

            int tEndX = tInitX + faceSize;
            int tEndY = tInitY + faceSize;
            tEndX = Math.min(tEndX, mutableBitmap.getWidth());
            tEndY = Math.min(tEndY, mutableBitmap.getHeight());

            initX = Math.min(initX, tInitX);
            initY = Math.min(initY, tInitY);
            endX = Math.max(endX, tEndX);
            endY = Math.max(endY, tEndY);
        }

        int sizeX = endX - initX;
        int sizeY = endY - initY;

        if (sizeX + initX > mutableBitmap.getWidth()) {
            sizeX = mutableBitmap.getWidth() - initX;
        }
        if (sizeY + initY > mutableBitmap.getHeight()) {
            sizeY = mutableBitmap.getHeight() - initY;
        }

        Point init = new Point(initX, initY);
        Point end = new Point(initX + sizeX, initY + sizeY);

        return new CropResult(mutableBitmap, init, end, centerFace);
    }

    @Deprecated
    public Bitmap cropFace(Context ctx, int resDrawable) {
        return getCroppedImage(ctx, resDrawable);
    }

    @Deprecated
    public Bitmap cropFace(Bitmap bitmap) {
        return getCroppedImage(bitmap);
    }

    public Bitmap getFullDebugImage(Context ctx, int resDrawable) {
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        return getFullDebugImage(BitmapFactory.decodeResource(ctx.getResources(), resDrawable, bitmapOptions));
    }

    public Bitmap getFullDebugImage(Bitmap bitmap) {
        CropResult result = cropFace(bitmap, true);
        Canvas canvas = new Canvas(result.getOriginalBitmap());

        canvas.drawBitmap(result.getOriginalBitmap(), new Matrix(), null);
        canvas.drawRect(result.getInit().x,
                result.getInit().y,
                result.getEnd().x,
                result.getEnd().y,
                mDebugAreaPainter);

        return result.getOriginalBitmap();
    }

    public Bitmap getCroppedImage(Context ctx, int resDrawable) {
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        return getCroppedImage(BitmapFactory.decodeResource(ctx.getResources(), resDrawable, bitmapOptions));
    }

    public Bitmap getCroppedImage(Bitmap bitmap) {
        CropResult result = cropFace(bitmap, mDebug);

//        Bitmap croppedBitmap = Bitmap.createBitmap(result.getBitmap(),
//                result.getInit().x,
//                result.getInit().y,
//                result.getEnd().x - result.getInit().x,
//                result.getEnd().y - result.getInit().y);

        if (result.getOriginalBitmap() != result.getCroppedBitmap()) {
            result.getOriginalBitmap().recycle();
        }

        return result.getCroppedBitmap();
    }

    public CropResult getCroppedResult(Bitmap bitmap) {
        CropResult result = cropFace(bitmap, mDebug);
        return result;
    }

    public class CropResult {
        Point mInit;
        Point mEnd;
        Bitmap mOriginalBitmap;
        Bitmap mCroppedBitmap;
        PointF mOriginalCenterFace;
        PointF mCroppedCenterFace;

        public CropResult(Bitmap originalBitmap, Point init, Point end, PointF originalCenterFace) {
            mOriginalBitmap = originalBitmap;
            mInit = init;
            mEnd = end;
            mOriginalCenterFace = originalCenterFace;
            findCroppedCenterFace();
            findCroppedBitmap();
        }

        public CropResult(Bitmap originalBitmap) {
            mOriginalBitmap = originalBitmap;
            mInit = new Point(0, 0);
            mEnd = new Point(mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight());
            mOriginalCenterFace = new PointF(mOriginalBitmap.getWidth() / 2, mOriginalBitmap.getHeight() /2);
            findCroppedCenterFace();
            findCroppedBitmap();
        }

        private void findCroppedCenterFace() {
            mCroppedCenterFace = new PointF(mOriginalCenterFace.x - mInit.x,
                    mOriginalCenterFace.y - mInit.y);

        }

        private void findCroppedBitmap() {
            mCroppedBitmap = Bitmap.createBitmap(getOriginalBitmap(),
                    getInit().x,
                    getInit().y,
                    getEnd().x - getInit().x,
                    getEnd().y - getInit().y);
        }

        public Bitmap getOriginalBitmap() {
            return mOriginalBitmap;
        }

        public Bitmap getCroppedBitmap() {
            return mCroppedBitmap;
        }

        public Point getInit() {
            return mInit;
        }

        public Point getEnd() {
            return mEnd;
        }

        public PointF getmCroppedCenterFace() {
            return mCroppedCenterFace;
        }
    }
}
