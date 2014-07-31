AndroidFaceCropper
==================

Android bitmap Face Cropper

[Link to sample apk and jar](https://github.com/lafosca/AndroidFaceCropper/releases/tag/1.1)


##Usage 
To crop faces automatically, you have to instantiate an object of `FaceCropper` class in that way:

	FaceCropper mFaceCropper = new FaceCropper();
    mFaceCropper.getCroppedImage(source);
    
`getCroppedImage` method supports `int` argument as a drawable resource, or directly a `Bitmap`.

##Configuration
There are 4 important methods to configure its behavior:

`setMaxFaces(int faces)`, to adjust the maximum number of faces to be recognized.

`setFaceMinSize(int faceMinSize)`, in pixels.

`setFaceMarginPx(int faceMarginPx)`, in pixels, and for each side.

`setEyeDistanceFactorMargin(float eyeDistanceFactorMargin)`, as a multiplier of the distance between the detected face eyes.

`setDebug(boolean debug)`, to enable painting red circles over detected faces.

`getFullDebugImage(Bitmap bitmap)`, to obtain a non-cropped image as the original, but with the detected faces painted, and the cropped area painted in green.
