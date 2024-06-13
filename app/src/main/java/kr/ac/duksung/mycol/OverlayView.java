package kr.ac.duksung.mycol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Environment;
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
        leftCheekIndex = Arrays.asList(230, 120, 100, 36, 50, 117, 229);
        rightCheekIndex = Arrays.asList(450, 349, 329, 371, 266, 280, 346, 449);
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

        makeCircleMask(leftCheekMask, leftCheekPoints);
        makeCircleMask(rightCheekMask, rightCheekPoints);

        cheekMask = new Mat();
        Core.add(leftCheekMask, rightCheekMask, cheekMask);

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
        this.blushColor = color;
        Log.d(TAG, "blushColor: " + blushColor);

        if (originalMat.empty() || cheekMask.empty()) {
            Log.e(TAG, "OriginalMat or CheekMask is empty, cannot apply GaussianBlur");
            return;
        }

        // 블러셔 원의 중심을 위한 원색 부분 생성
        Mat centerOverlay = new Mat(cheekMask.size(), cheekMask.type());
        Scalar blushScalar = colorToScalar(blushColor, 255);  // 원색 유지
        centerOverlay.setTo(blushScalar, cheekMask);

        // 원의 중심에서 멀어질수록 블러링이 심해지는 효과 적용
        Mat blurredOverlay = new Mat(cheekMask.size(), cheekMask.type());
        Scalar transparentScalar = colorToScalar(blushColor, 100);  // 투명도 조정
        blurredOverlay.setTo(transparentScalar, cheekMask);

        // 큰 커널 크기로 가우시안 블러 적용
        Imgproc.GaussianBlur(blurredOverlay, blurredOverlay, new Size(75, 75), 0);

        // 중심과 블러링된 부분을 합쳐서 자연스러운 블러셔 효과 생성
        Mat combinedOverlay = new Mat();
        Core.addWeighted(centerOverlay, 0.5, blurredOverlay, 0.5, 0.0, combinedOverlay);

        // 원본 이미지와 블러셔 이미지를 혼합
        Mat blendedCheek = new Mat();
        Core.addWeighted(originalMat, 0.8, combinedOverlay, 0.2, 0.0, blendedCheek);

        blushMakeupBitmap = Bitmap.createBitmap(blendedCheek.cols(), blendedCheek.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blendedCheek, blushMakeupBitmap);

        Log.d(TAG, "Blush makeup applied and bitmap created.");

        invalidate();
    }
/*
    public void setBlushColor(int color) {
        this.blushColor = color;
        Log.d(TAG, "blushColor: " + blushColor);

        if (originalMat.empty() || blushTemplateBitmap == null) {
            Log.e(TAG, "OriginalMat or blushTemplateBitmap is empty, cannot apply blush");
            return;
        }

        // 템플릿 비트맵을 변환
        Bitmap coloredBlushBitmap = applyColorToBlushTemplate(blushTemplateBitmap, color);

        // 변환된 비트맵을 Mat으로 변환
        Mat blushMat = new Mat();
        Utils.bitmapToMat(coloredBlushBitmap, blushMat);

        // 원본 이미지와 블러셔 이미지를 혼합
        Mat blendedCheek = new Mat();
        Core.addWeighted(originalMat, 0.8, blushMat, 0.2, 0.0, blendedCheek);

        blushMakeupBitmap = Bitmap.createBitmap(blendedCheek.cols(), blendedCheek.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blendedCheek, blushMakeupBitmap);

        Log.d(TAG, "Blush makeup applied and bitmap created.");

        invalidate(); // 뷰 다시 그리기
    }
*/
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

    public static void makeCircleMask(Mat mask, List<Point> points) {
        Point center = calculateCenter(points);
        int radius = calculateRadius(points, center);
        Log.d(TAG, "radius: " + radius);

        Imgproc.circle(mask, center, radius, new Scalar(255, 255, 255), -1);
    }

    public static Point calculateCenter(List<Point> points) {
        double sumX = 0, sumY = 0;
        for (Point point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        return new Point(sumX / points.size(), sumY / points.size());
    }

    public static int calculateRadius(List<Point> points, Point center) {
        double maxDistance = 0;
        for (Point point : points) {
            double distance = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return (int) Math.round(maxDistance);
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

