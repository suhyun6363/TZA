package kr.ac.duksung.mycol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;

import com.google.mediapipe.framework.image.BitmapExtractor;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.Connection;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
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
import androidx.annotation.Nullable;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OverlayView extends View {

    private FaceLandmarkerResult results; // 얼굴 랜드마커 결과를 저장할 변수
    private Bitmap resultBitmap;
    private Bitmap bitmap;

    private float scaleFactor = 1f; // 이미지 크기 조정 비율
    private int imageWidth = 1; // 이미지의 너비
    private int imageHeight = 1; // 이미지의 높이

    private int blushColor = Color.TRANSPARENT; // 기본 블러셔 색상
    private int lipColor = Color.TRANSPARENT; // 기본 립 색상

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
        init();
    }

    private void init() {
        bitmap = null;
        resultBitmap = null;
    }

    public void clear() {
        results = null;
        bitmap = null;
        resultBitmap = null; // 결과 비트맵 초기화
        invalidate(); // 뷰 다시 그리기
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (results == null || results.faceLandmarks().isEmpty()) {
            clear(); // 결과가 없으면 초기화
            return;
        }

        if (results != null) {
            List<List<NormalizedLandmark>> landmarks = results.faceLandmarks();
            List<Integer> upperLipIndex = Arrays.asList(
                    0, 267, 269, 270, 409, 291, 375, 321, 405, 314,
                    17, 84, 181, 91, 146, 61, 185, 40, 39, 37
            );

            List<Integer> lowerLipIndex = Arrays.asList(
                    13, 312, 311, 310, 415, 308, 324, 318, 402, 317,
                    14, 87, 178, 88, 95, 78, 191, 80, 81, 82
            );
            List<Integer> leftCheekIndex = Arrays.asList(230, 120, 100, 36, 50, 117, 229);
            List<Integer> rightCheekIndex = Arrays.asList(450, 349, 329, 371, 266, 280, 346, 449);
            Log.d(TAG, "Drawing " + landmarks.size() + " faces.");
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

            createImage(upperLipPoints, lowerLipPoints, leftCheekPoints, rightCheekPoints);

            if (resultBitmap != null) {
                // 캔버스의 크기를 기준으로 비트맵을 스케일링하여 그리기
                canvas.drawBitmap(resultBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
            } else {
                Log.d(TAG, "Lip mask bitmap is null.");
            }
        }
    }

    private void createImage(List<Point> upperLipPoints, List<Point> lowerLipPoints, List<Point> leftCheekPoints, List<Point> rightCheekPoints) {
        if (upperLipPoints.isEmpty() || lowerLipPoints.isEmpty()) {
            Log.d(TAG, "Lip points are empty.");
            return;
        }
        if (leftCheekPoints.isEmpty() || rightCheekPoints.isEmpty()) {
            Log.d(TAG, "Cheek points are empty.");
            return;
        }

        Log.d(TAG, "Left Cheek points: " + leftCheekPoints.toString());
        Log.d(TAG, "Right Cheek points: " + rightCheekPoints.toString());

        Mat originalMat = new Mat();
        Utils.bitmapToMat(bitmap, originalMat);

        Mat upperLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        Mat lowerLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        Mat leftCheekMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        Mat rightCheekMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);

        MatOfPoint matOfUpperLipPoint = new MatOfPoint();
        MatOfPoint matOfLowerLipPoint = new MatOfPoint();
        matOfUpperLipPoint.fromList(upperLipPoints);
        matOfLowerLipPoint.fromList(lowerLipPoints);
        Log.d(TAG, "Lip matOfLeftCheek: " + matOfUpperLipPoint.toString());
        Log.d(TAG, "Lip matOfRightCheek: " + matOfLowerLipPoint.toString());

        Imgproc.fillConvexPoly(upperLipMask, matOfUpperLipPoint, new Scalar(255, 255, 255, 255));
        Imgproc.fillConvexPoly(lowerLipMask, matOfLowerLipPoint, new Scalar(255, 255, 255, 255));
        makeCircleMask(leftCheekMask, leftCheekPoints);
        makeCircleMask(rightCheekMask, rightCheekPoints);

        // 흰색 lipMask
        Mat lipMask = new Mat();
        Core.subtract(upperLipMask, lowerLipMask, lipMask);

        // 색 입힌 lipMask
        Mat overlayLip = new Mat(lipMask.size(), lipMask.type());
        Scalar lipScalar = colorToScalar(lipColor);
        overlayLip.setTo(lipScalar, lipMask);

        // 내 입술Mask
        Mat myLipMask = new Mat();
        Core.bitwise_and(originalMat, lipMask, myLipMask);

        // 알파 블렌딩(립)
        Mat blendedLip = new Mat();
        Core.addWeighted(myLipMask, 0.7, overlayLip, 0.3, 0.0, blendedLip);

        // 흰색 양 볼 Mask
        Mat cheekMask = new Mat();
        Core.add(leftCheekMask, rightCheekMask, cheekMask);

        // 내 볼 Mask
        Mat myCheekMask = new Mat();
        Core.bitwise_and(originalMat, cheekMask, myCheekMask);

        // 색 입힌 볼 Mask
        Mat overlayCheek = new Mat(myCheekMask.size(), myCheekMask.type());
        Scalar blushScalar = colorToScalar(blushColor);
        overlayCheek.setTo(blushScalar, cheekMask);

        // 자연스러운 효과를 위해 가우시안 블러 적용
        Imgproc.GaussianBlur(overlayCheek, overlayCheek, new Size(45, 45), 0);

        // 원본 이미지와 블러 처리된 볼 마스크를 알파 블렌딩
        Mat blendedCheek = new Mat();
        Core.addWeighted(originalMat, 0.8, overlayCheek, 0.2, 0.0, blendedCheek);

        Mat result = new Mat();
        Core.add(blendedLip, blendedCheek, result);

        Bitmap resultBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, resultBitmap);
        this.resultBitmap = resultBitmap;
    }

    private Scalar colorToScalar(int color) {
        // Color의 ARGB 값을 추출하여 Scalar로 변환
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return new Scalar(red, green, blue, alpha);
    }

    public void setBlushColor(int color) {
        this.blushColor = color;
        Log.d(TAG, "blushColor: " + blushColor);
        invalidate(); // 뷰 다시 그리기
    }

    public void setLipColor(int color) {
        this.lipColor = color;
        Log.d(TAG, "lipColor: " + lipColor);
        invalidate(); // 뷰 다시 그리기
    }

    public static void makeCircleMask(Mat mask, List<Point> points) {
        // 중심과 반지름 계산
        Point center = calculateCenter(points);
        int radius = calculateRadius(points, center);

        // 원 그리기
        Imgproc.circle(mask, center, radius, new Scalar(255), -1); // 흰색 원 (-1은 채우기를 의미)
    }

    // 중심 계산 함수
    public static Point calculateCenter(List<Point> points) {
        double sumX = 0, sumY = 0;
        for (Point point : points) {
            sumX += point.x;
            sumY += point.y;
        }
        return new Point(sumX / points.size(), sumY / points.size());
    }

    // 반지름 계산 함수
    public static int calculateRadius(List<Point> points, Point center) {
        double minDistance = 10000;
        for (Point point : points) {
            double distance = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return (int) Math.round(minDistance);
    }

    public void setResults(FaceLandmarkerResult faceLandmarkerResults, Bitmap bitmap, int imageHeight, int imageWidth, RunningMode runningMode) {
        this.results = faceLandmarkerResults;
        this.bitmap = bitmap;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;

        // saveBitmap(bitmap);

        // 실행 모드에 따라 스케일 팩터를 계산
        if (runningMode == RunningMode.IMAGE || runningMode == RunningMode.VIDEO) {
            scaleFactor = Math.min((float) getWidth() / imageWidth, (float) getHeight() / imageHeight);
        } else if (runningMode == RunningMode.LIVE_STREAM) {
            scaleFactor = Math.max((float) getWidth() / imageWidth, (float) getHeight() / imageHeight);
        }
        // 로그 추가
        Log.d(TAG, "FaceLandmarkerResult received with " + faceLandmarkerResults.faceLandmarks().size() + " faces.");
        Log.d(TAG, "Image dimensions: " + imageWidth + "x" + imageHeight + ", Scale factor: " + scaleFactor);

        invalidate(); // 뷰 다시 그리기
    }

    private static final String TAG = "Face Landmarker Overlay"; // 태그
}


