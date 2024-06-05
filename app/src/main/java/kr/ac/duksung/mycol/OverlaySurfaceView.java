package kr.ac.duksung.mycol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.graphics.Rect;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import org.opencv.core.Core;
import org.opencv.core.Point;

public class OverlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private FaceLandmarkerResult results;
    private Bitmap bitmap;
    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;
    private Bitmap lipMaskBitmap;
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
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // Try again shutting down the thread
            }
        }
    }

    public void clear() {
        results = null;
        lipMaskBitmap = null;
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
            Log.d(TAG, "Drawing " + landmarks.size() + " faces.");
            List<Point> upperLipPoints = new ArrayList<>();
            List<Point> lowerLipPoints = new ArrayList<>();

            for (Integer upperIndex : upperLipIndex) {
                NormalizedLandmark normalizedLandmark = landmarks.get(0).get(upperIndex);
                upperLipPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
            }
            for (Integer lowerIndex : lowerLipIndex) {
                NormalizedLandmark normalizedLandmark = landmarks.get(0).get(lowerIndex);
                lowerLipPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
            }

            createLipMask(upperLipPoints, lowerLipPoints);

            if (lipMaskBitmap != null) {
                Log.d(TAG, "Drawing lip mask bitmap.");
                canvas.drawBitmap(lipMaskBitmap, null, new Rect(0, 0, getWidth(), getHeight()), null);
            } else {
                Log.d(TAG, "Lip mask bitmap is null.");
            }
        }
    }

    private void createLipMask(List<Point> upperLipPoints, List<Point> lowerLipPoints) {
        if (upperLipPoints.isEmpty() || lowerLipPoints.isEmpty()) {
            Log.d(TAG, "Lip points are empty.");
            return;
        }

        Log.d(TAG, "Upper Lip points: " + upperLipPoints.toString());
        Log.d(TAG, "Lower Lip points: " + lowerLipPoints.toString());

        Mat originalMat = new Mat();
        Utils.bitmapToMat(bitmap, originalMat);

        Mat upperLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        Mat lowerLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);

        MatOfPoint matOfUpperLipPoint = new MatOfPoint();
        MatOfPoint matOfLowerLipPoint = new MatOfPoint();
        matOfUpperLipPoint.fromList(upperLipPoints);
        matOfLowerLipPoint.fromList(lowerLipPoints);
        Log.d(TAG, "Lip matOfUpperLipPoint: " + matOfUpperLipPoint.toString());
        Log.d(TAG, "Lip matOfLowerLipPoint: " + matOfLowerLipPoint.toString());

        Imgproc.fillConvexPoly(upperLipMask, matOfUpperLipPoint, new Scalar(255, 255, 255, 255));
        Imgproc.fillConvexPoly(lowerLipMask, matOfLowerLipPoint, new Scalar(255, 255, 255, 255));

        // Use bitwise_and to combine mask and overlay
        Mat lipMask = new Mat();
        Core.subtract(upperLipMask, lowerLipMask, lipMask);

        // Create a copy of the mask and fill it with (139, 0, 0, 255)
        Mat overlay = new Mat(lipMask.size(), lipMask.type());
        overlay.setTo(new Scalar(139, 0, 0, 255), lipMask);

        Mat myLipMask = new Mat();
        Core.bitwise_and(originalMat, lipMask, myLipMask);

        // 알파 블렌딩
        Mat blended = new Mat();
        Core.addWeighted(myLipMask, 0.8, overlay, 0.2, 0.0, blended);

        // 디버깅 로그 추가
        Log.d(TAG, "Mask size: " + myLipMask.size() + ", Overlay size: " + overlay.size());

        // Mat 객체를 Bitmap으로 변환
        Bitmap bmp = Bitmap.createBitmap(blended.cols(), blended.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blended, bmp);
        lipMaskBitmap = bmp;

        if (lipMaskBitmap != null) {
            Log.d(TAG, "Lip mask bitmap created with size: " + lipMaskBitmap.getWidth() + "x" + lipMaskBitmap.getHeight());
            // saveLipMaskBitmap(lipMaskBitmap); // 비트맵을 저장하는 메소드 호출
        } else {
            Log.d(TAG, "Lip mask bitmap is null.");
        }
    }

    private void saveResultBitmap(Bitmap bitmap) {
        // 파일 저장 경로 설정
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TZA");
        if (!directory.exists()) {
            directory.mkdirs(); // 디렉토리가 없으면 생성
        }

        String fileName = "result_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        // 파일 출력 스트림을 사용하여 비트맵 저장
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // 비트맵을 PNG 형식으로 압축하여 저장
            Log.d(TAG, "Bitmap saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap", e);
        }
    }

    private void saveOverlayBitmap(Bitmap bitmap) {
        // 파일 저장 경로 설정
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TZA");
        if (!directory.exists()) {
            directory.mkdirs(); // 디렉토리가 없으면 생성
        }

        String fileName = "overlay_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        // 파일 출력 스트림을 사용하여 비트맵 저장
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // 비트맵을 PNG 형식으로 압축하여 저장
            Log.d(TAG, "Bitmap saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap", e);
        }
    }

    private void saveLipMaskBitmap(Bitmap bitmap) {
        // 파일 저장 경로 설정
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TZA");
        if (!directory.exists()) {
            directory.mkdirs(); // 디렉토리가 없으면 생성
        }

        String fileName = "lip_mask_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        // 파일 출력 스트림을 사용하여 비트맵 저장
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // 비트맵을 PNG 형식으로 압축하여 저장
            Log.d(TAG, "Bitmap saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap", e);
        }
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

        if (drawThread != null) {
            drawThread.setRunning(true);
        }
    }

    private class DrawThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean running = false;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        if (canvas != null) {
                            drawOverlay(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}


