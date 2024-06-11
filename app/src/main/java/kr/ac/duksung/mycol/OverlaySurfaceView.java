package kr.ac.duksung.mycol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import org.opencv.core.Core;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OverlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private FaceLandmarkerResult results;
    private Bitmap bitmap;
    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private Bitmap imageBitmap;
    private static final String TAG = "Face Landmarker Overlay";
    private DrawThread drawThread;

    public OverlaySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        init();
    }

    private void init() {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV library not loaded");
        } else {
            Log.d("OpenCV", "OpenCV library loaded");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface dimensions changed, adjust your drawing here
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (drawThread != null) {
            drawThread.setRunning(false); // 그리기를 중지합니다.
            boolean retry = true;
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // Try again shutting down the thread
                }
            }
        }
    }

    public void clear() {
        results = null;
        imageBitmap = null;
        invalidate();
    }

    private void drawOverlay(Canvas canvas) {
        if (results == null || results.faceLandmarks().isEmpty()) {
            clear();
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

            if (imageBitmap != null) {
                Log.d(TAG, "Drawing lip mask bitmap.");
                canvas.drawBitmap(imageBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
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

        // 반전(주변 흰색, lipMask 검은색)
        Mat invertedLipMask = new Mat();
        Core.bitwise_not(lipMask, invertedLipMask);

        // 입술만 뺀 image
        Mat imageWithoutLip = new Mat();
        Core.bitwise_and(originalMat, invertedLipMask, imageWithoutLip);

        // 색 입힌 lipMask
        Mat overlayLip = new Mat(lipMask.size(), lipMask.type());
        overlayLip.setTo(new Scalar(139, 0, 0, 255), lipMask);

        // 흰색 양 볼Mask
        Mat cheekMask = new Mat();
        Core.add(leftCheekMask, rightCheekMask, cheekMask);

        // 내 입술Mask
        Mat myLipMask = new Mat();
        Core.bitwise_and(originalMat, lipMask, myLipMask);

        // 내 볼Mask
        Mat myCheekMask = new Mat();
        Core.bitwise_and(originalMat, cheekMask, myCheekMask);

        // 색 입힌 볼Mask
        Mat overlayCheek = new Mat(lipMask.size(), lipMask.type());
        overlayCheek.setTo(new Scalar(88, 174, 169, 180), lipMask);

        // 알파 블렌딩(립)
        Mat blendedLip = new Mat();
        Core.addWeighted(myLipMask, 0.8, overlayLip, 0.2, 0.0, blendedLip);

        // 알파 블렌딩(볼)
        Mat blendedCheek = new Mat();
        Core.addWeighted(myCheekMask, 0.7, overlayCheek, 0.3, 0.0, blendedCheek);

        Mat imageMat = new Mat();
        Core.add(imageWithoutLip, blendedLip, imageMat);

        // 디버깅 로그 추가
        Log.d(TAG, "Mask size: " + myCheekMask.size() + ", Overlay size: " + overlayCheek.size());

        // Mat 객체를 Bitmap으로 변환
        Bitmap bmp = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageMat, bmp);
        imageBitmap = bmp;

        if (imageBitmap != null) {
            Log.d(TAG, "Image bitmap created with size: " + imageBitmap.getWidth() + "x" + imageBitmap.getHeight());
        } else {
            Log.d(TAG, "Lip mask bitmap is null.");
        }
    }

    // 원을 그리기 위한 헬퍼 함수
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

        //invalidate(); // SurfaceView의 경우, onDraw()를 직접 호출하지 않으므로 이 호출은 제거
    }

    private class DrawThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean running;

        public DrawThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            running = false;
        }

        public void setRunning(boolean isRunning) {
            running = isRunning;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            // Clear the canvas
                            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                            drawOverlay(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                try {
                    // Delay to limit frame rate (e.g., 30 FPS)
                    Thread.sleep(33); // 30 FPS
                } catch (InterruptedException e) {
                    // Handle exception
                }
            }
        }
    }
}





