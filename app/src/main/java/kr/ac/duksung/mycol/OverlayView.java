package kr.ac.duksung.mycol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OverlayView extends View {

    private FaceLandmarkerResult results;
    private Bitmap resultBitmap;
    private Bitmap bitmap;
    private Bitmap lipMakeupBitmap;
    private Bitmap blushMakeupBitmap;
    private Bitmap blushTemplateBitmap;

    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;

    private int blushColor = Color.TRANSPARENT;
    private int lipColor = Color.TRANSPARENT;
    private boolean isBlushMakeup = false;
    private boolean isLipMakeup = false;
    private Mat lipMask;
    private Mat cheekMask;
    private Mat originalMat;
    // Context 멤버 변수 선언
    private Context context;

    private List<Integer> upperLipIndex, lowerLipIndex, leftCheekIndex, rightCheekIndex;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV library not loaded");
        } else {
            System.loadLibrary("opencv_java4");
            Log.d("OpenCV", "OpenCV library loaded");
        }
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        bitmap = null;
        resultBitmap = null;
        lipMakeupBitmap = null;
        blushMakeupBitmap = null;
        upperLipIndex = Arrays.asList(
                0, 267, 269, 270, 409, 291, 375, 321, 405, 314,
                17, 84, 181, 91, 146, 61, 185, 40, 39, 37
        );

        lowerLipIndex = Arrays.asList(
                13, 312, 311, 310, 415, 308, 324, 318, 402, 317,
                14, 87, 178, 88, 95, 78, 191, 80, 81, 82
        );
        //leftCheekIndex = Arrays.asList(230, 120, 100, 36, 50, 117, 229);
        leftCheekIndex = Arrays.asList(230, 231, 121, 47, 126, 142, 36, 205, 187, 147, 123, 116, 111, 31, 228, 229);
        //rightCheekIndex = Arrays.asList(450, 349, 329, 371, 266, 280, 346, 449);
        rightCheekIndex = Arrays.asList(450, 451, 349, 329, 371, 266, 425, 411, 376, 401, 366, 447, 345, 340, 448, 449);

        lipMask = new Mat();
        cheekMask = new Mat();
        originalMat = new Mat();

        // 블러셔 템플릿 이미지 불러오기
        blushTemplateBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.blusher_mask);
        if (blushTemplateBitmap == null) {
            Log.e(TAG, "Failed to load blush template image from resources");
        }
    }

    public void clear() {
        results = null;
        bitmap = null;
        lipMakeupBitmap = null;
        blushMakeupBitmap = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (results == null || results.faceLandmarks().isEmpty()) {
            clear(); // 결과가 없으면 초기화
            return;
        }

        if (bitmap != null) {
            Rect destRect = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(bitmap, null, destRect, null);
        }

        if (isBlushMakeup && blushMakeupBitmap != null) {
            canvas.drawBitmap(blushMakeupBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
        }

        if (isLipMakeup && lipMakeupBitmap != null) {
            canvas.drawBitmap(lipMakeupBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
        }
    }

    private void createBlushFilter(List<Point> leftCheekPoints, List<Point> rightCheekPoints) {
        if (leftCheekPoints.isEmpty() || rightCheekPoints.isEmpty()) {
            Log.d(TAG, "Cheek points are empty.");
            return;
        }

        Mat leftCheekMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        Mat rightCheekMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);

        makePolygonMask(leftCheekMask, leftCheekPoints);
        makePolygonMask(rightCheekMask, rightCheekPoints);

        cheekMask = new Mat();
        Core.add(leftCheekMask, rightCheekMask, cheekMask);

        // Apply Gaussian Blur to create a stronger gradient effect
        Imgproc.GaussianBlur(cheekMask, cheekMask, new Size(101, 101), 0);

        Log.d(TAG, "Blush filter created with leftCheekMask and rightCheekMask.");
    }



    private void createLipFilter(List<Point> upperLipPoints, List<Point> lowerLipPoints) {
        if (upperLipPoints.isEmpty() || lowerLipPoints.isEmpty()) {
            Log.d(TAG, "Lip points are empty.");
            return;
        }

        Mat upperLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        Mat lowerLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        MatOfPoint matOfUpperLipPoint = new MatOfPoint();
        MatOfPoint matOfLowerLipPoint = new MatOfPoint();
        matOfUpperLipPoint.fromList(upperLipPoints);
        matOfLowerLipPoint.fromList(lowerLipPoints);

        Imgproc.fillConvexPoly(upperLipMask, matOfUpperLipPoint, new Scalar(255, 255, 255, 255));
        Imgproc.fillConvexPoly(lowerLipMask, matOfLowerLipPoint, new Scalar(255, 255, 255, 255));

        lipMask = new Mat();
        Core.subtract(upperLipMask, lowerLipMask, lipMask);

        Log.d(TAG, "Lip filter created with upperLipMask and lowerLipMask.");
    }

    private Scalar colorToScalar(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return new Scalar(red, green, blue, alpha);
    }

    private Scalar colorToScalar(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return new Scalar(red, green, blue, alpha);
    }

    public void setBlushColor(int color) {
        // 명도를 낮춘 색상 계산
        int alpha = Color.alpha(color);
        int red = (int) (Color.red(color) * 0.6); // 명도를 60%로 줄임
        int green = (int) (Color.green(color) * 0.6); // 명도를 60%로 줄임
        int blue = (int) (Color.blue(color) * 0.6); // 명도를 60%로 줄임
        int darkenedColor = Color.argb(alpha, red, green, blue);

        this.blushColor = darkenedColor;
        Log.d(TAG, "blushColor: " + blushColor);

        if (originalMat.empty() || cheekMask.empty()) {
            Log.e(TAG, "OriginalMat or CheekMask is empty, cannot apply GaussianBlur");
            return;
        }

        Mat overlayCheek = new Mat(cheekMask.size(), cheekMask.type());
        Scalar blushScalar = colorToScalar(blushColor, 255);  // 중심부는 불투명하게

        overlayCheek.setTo(blushScalar, cheekMask);

        // Apply GaussianBlur to create a gradient effect
        Imgproc.GaussianBlur(overlayCheek, overlayCheek, new Size(101, 101), 0);

        // Create a gradient mask for blending
        Mat gradientMask = new Mat(overlayCheek.size(), CvType.CV_8UC4);
        Core.multiply(overlayCheek, new Scalar(1, 1, 1, 0.5), gradientMask); // 알파 값을 0.5로 줄임

        // Adjust alpha channel for gradient effect
        List<Mat> channels = new ArrayList<>(4);
        Core.split(overlayCheek, channels);
        Mat alphaChannel = channels.get(3);
        Imgproc.GaussianBlur(alphaChannel, alphaChannel, new Size(101, 101), 0);
        channels.set(3, alphaChannel);
        Core.merge(channels, overlayCheek);

        Mat blendedCheek = new Mat();
        Core.addWeighted(originalMat, 0.8, overlayCheek, 0.2, 0.0, blendedCheek);  // 원본의 비율을 높이고 오버레이 비율을 낮춤

        blushMakeupBitmap = Bitmap.createBitmap(blendedCheek.cols(), blendedCheek.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blendedCheek, blushMakeupBitmap);

        Log.d(TAG, "Blush makeup applied and bitmap created with gradient effect.");

        invalidate();
    }



    private Bitmap applyColorToBlushTemplate(Bitmap template, int color) {
        Bitmap coloredTemplate = template.copy(Bitmap.Config.ARGB_8888, true);
        int width = coloredTemplate.getWidth();
        int height = coloredTemplate.getHeight();
        int[] pixels = new int[width * height];
        coloredTemplate.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int alpha = Color.alpha(pixels[i]);
            pixels[i] = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        }

        coloredTemplate.setPixels(pixels, 0, width, 0, 0, width, height);
        return coloredTemplate;
    }

    public void setLipColor(int color) {
        this.lipColor = color;
        Log.d(TAG, "lipColor: " + lipColor);

        if (originalMat.empty() || lipMask.empty()) {
            Log.e(TAG, "OriginalMat or LipMask is empty, cannot apply GaussianBlur");
            return;
        }

        Mat overlayLip = new Mat(lipMask.size(), lipMask.type());
        overlayLip.setTo(colorToScalar(lipColor), lipMask);

        Mat myLipMask = new Mat();
        Core.bitwise_and(originalMat, lipMask, myLipMask);

        Mat blendedLip = new Mat();
        Core.addWeighted(myLipMask, 0.7, overlayLip, 0.3, 0.0, blendedLip);

        lipMakeupBitmap = Bitmap.createBitmap(blendedLip.cols(), blendedLip.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blendedLip, lipMakeupBitmap);

        Log.d(TAG, "Lip makeup applied and bitmap created.");

        invalidate();
    }

    public void setMakeupStates(boolean isBlushMakeup, boolean isLipMakeup) {
        this.isBlushMakeup = isBlushMakeup;
        this.isLipMakeup = isLipMakeup;
    }

    public static void makePolygonMask(Mat mask, List<Point> points) {
        MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromList(points);
        Imgproc.fillConvexPoly(mask, matOfPoint, new Scalar(255, 255, 255, 255));
    }

    public void setResults(FaceLandmarkerResult faceLandmarkerResults, Bitmap bitmap, int imageHeight, int imageWidth, RunningMode runningMode) {
        this.results = faceLandmarkerResults;
        this.bitmap = bitmap;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;

        if (runningMode == RunningMode.IMAGE || runningMode == RunningMode.VIDEO) {
            scaleFactor = Math.min((float) getWidth() / imageWidth, (float) getHeight() / imageHeight);
        } else if (runningMode == RunningMode.LIVE_STREAM) {
            scaleFactor = Math.max((float) getWidth() / imageWidth, (float) getHeight() / imageHeight);
        }

        Log.d(TAG, "FaceLandmarkerResult received with " + faceLandmarkerResults.faceLandmarks().size() + " faces.");
        Log.d(TAG, "Image dimensions: " + imageWidth + "x" + imageHeight + ", Scale factor: " + scaleFactor);

        Utils.bitmapToMat(bitmap, originalMat);

        List<List<NormalizedLandmark>> landmarks = faceLandmarkerResults.faceLandmarks();
        List<Point> upperLipPoints = new ArrayList<>();
        List<Point> lowerLipPoints = new ArrayList<>();
        List<Point> leftCheekPoints = new ArrayList<>();
        List<Point> rightCheekPoints = new ArrayList<>();

        for (Integer upperIndex : upperLipIndex) {
            NormalizedLandmark normalizedLandmark = landmarks.get(0).get(upperIndex);
            upperLipPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
        }
        for (Integer lowerIndex : lowerLipIndex) {
            NormalizedLandmark normalizedLandmark = landmarks.get(0).get(lowerIndex);
            lowerLipPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
        }
        for (Integer leftIndex : leftCheekIndex) {
            NormalizedLandmark normalizedLandmark = landmarks.get(0).get(leftIndex);
            leftCheekPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
        }
        for (Integer rightIndex : rightCheekIndex) {
            NormalizedLandmark normalizedLandmark = landmarks.get(0).get(rightIndex);
            rightCheekPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
        }

        createLipFilter(upperLipPoints, lowerLipPoints);
        createBlushFilter(leftCheekPoints, rightCheekPoints);
    }

    private static final String TAG = "Face Landmarker Overlay";
}
