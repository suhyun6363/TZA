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
    private Paint upperLinePaint = new Paint();
    private Paint lowerLinePaint = new Paint(); // 선을 그리기 위한 페인트 객체
    // private Paint pointPaint = new Paint(); // 점을 그리기 위한 페인트 객체
    private Paint lipPaint = new Paint(); // 립스틱 색상을 그리기 위한 페인트 객체
    private Bitmap bitmap;
    private Bitmap lipMaskBitmap; // 입술 마스크 비트맵

    private float scaleFactor = 1f; // 이미지 크기 조정 비율
    private int imageWidth = 1; // 이미지의 너비
    private int imageHeight = 1; // 이미지의 높이

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
        // Inflate the layout and find the ImageView
        // inflate(context, R.layout.overlay_view, this);
        // lipMaskImageView = findViewById(R.id.lip_mask_image_view);
        initPaints();
    }

    public void clear() {
        results = null;
        upperLinePaint.reset(); // 선 페인트 리셋
        lowerLinePaint.reset();
        // pointPaint.reset(); // 점 페인트 리셋
        lipMaskBitmap = null; // 입술 마스크 비트맵 초기화
        initPaints(); // 페인트 객체 다시 초기화
        invalidate(); // 뷰 다시 그리기
    }

    private void initPaints() {

        // 선 페인트 초기화
        upperLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.mp_color_primary));
        upperLinePaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        upperLinePaint.setStyle(Paint.Style.STROKE);

        lowerLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.purple_700));
        lowerLinePaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        lowerLinePaint.setStyle(Paint.Style.STROKE);

        /*
        // 점 페인트 초기화
        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        pointPaint.setStyle(Paint.Style.FILL);

         */
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
            Log.d(TAG, "Drawing " + landmarks.size() + " faces.");
            List<Point> upperLipPoints = new ArrayList<>();
            List<Point> lowerLipPoints = new ArrayList<>();

            Log.d(TAG, "lip_landmark: " + FaceLandmarker.FACE_LANDMARKS_LIPS.toString());
            for (Integer upperIndex : upperLipIndex) {
                NormalizedLandmark normalizedLandmark = landmarks.get(0).get(upperIndex);
                upperLipPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
            }
            for (Integer lowerIndex : lowerLipIndex) {
                NormalizedLandmark normalizedLandmark = landmarks.get(0).get(lowerIndex);
                lowerLipPoints.add(new Point(normalizedLandmark.x() * imageWidth, normalizedLandmark.y() * imageHeight));
            }

            // 로그 추가: lipPoints 확인
            Log.d(TAG, "Upper Lip points: " + upperLipPoints.toString());
            Log.d(TAG, "Lower Lip points: " + lowerLipPoints.toString());


            // 입술 마스크 생성
            createLipMask(upperLipPoints, lowerLipPoints);
/*
            for (int i = 0; i < upperLipPoints.size() - 1; i++) {
                Point startPoint = upperLipPoints.get(i);
                Point endPoint = upperLipPoints.get(i + 1);

                float startX = (float) startPoint.x  * scaleFactor;
                float startY = (float) startPoint.y  * scaleFactor;
                float endX = (float) endPoint.x * scaleFactor;
                float endY = (float) endPoint.y * scaleFactor;

                canvas.drawLine(startX, startY, endX, endY, upperLinePaint);
            }

            // 마지막 점과 첫 번째 점을 연결하여 폐곡선을 만듭니다.
            if (!upperLipPoints.isEmpty()) {
                Point startPoint = upperLipPoints.get(upperLipPoints.size() - 1);
                Point endPoint = upperLipPoints.get(0);

                float startX = (float) startPoint.x  * scaleFactor;
                float startY = (float) startPoint.y  * scaleFactor;
                float endX = (float) endPoint.x * scaleFactor;
                float endY = (float) endPoint.y * scaleFactor;

                canvas.drawLine(startX, startY, endX, endY, upperLinePaint);
            }

            // lowerLipPoints의 각 점을 연결합니다.
            for (int i = 0; i < lowerLipPoints.size() - 1; i++) {
                Point startPoint = lowerLipPoints.get(i);
                Point endPoint = lowerLipPoints.get(i + 1);

                float startX = (float) startPoint.x  * scaleFactor;
                float startY = (float) startPoint.y  * scaleFactor;
                float endX = (float) endPoint.x * scaleFactor;
                float endY = (float) endPoint.y * scaleFactor;

                canvas.drawLine(startX, startY, endX, endY, lowerLinePaint);
            }

            // 마지막 점과 첫 번째 점을 연결하여 폐곡선을 만듭니다.
            if (!lowerLipPoints.isEmpty()) {
                Point startPoint = lowerLipPoints.get(lowerLipPoints.size() - 1);
                Point endPoint = lowerLipPoints.get(0);

                float startX = (float) startPoint.x  * scaleFactor;
                float startY = (float) startPoint.y  * scaleFactor;
                float endX = (float) endPoint.x * scaleFactor;
                float endY = (float) endPoint.y * scaleFactor;

                canvas.drawLine(startX, startY, endX, endY, lowerLinePaint);
            }
*/
            createLipMask(upperLipPoints, lowerLipPoints);

            if (lipMaskBitmap != null) {
                Log.d(TAG, "Drawing lip mask bitmap.");
                // 캔버스의 크기를 기준으로 비트맵을 스케일링하여 그리기
                Log.d(TAG, "lipMaskBitmap width: " + lipMaskBitmap.getWidth() + ", height: " + lipMaskBitmap.getHeight());
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

        // 로그 추가: lipPoints 확인
        Log.d(TAG, "Upper Lip points: " + upperLipPoints.toString());
        Log.d(TAG, "Lower Lip points: " + lowerLipPoints.toString());

        // Convert originalBitmap to Mat
        Mat originalMat = new Mat();
        Utils.bitmapToMat(bitmap, originalMat);

        // 이미지 크기와 동일한 크기의 Mat 객체 생성
        Mat upperLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);
        Mat lowerLipMask = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC4);

        // 입술 영역을 채우기
        MatOfPoint matOfUpperLipPoint = new MatOfPoint();
        MatOfPoint matOfLowerLipPoint = new MatOfPoint();
        matOfUpperLipPoint.fromList(upperLipPoints);
        matOfLowerLipPoint.fromList(lowerLipPoints);
        Log.d(TAG, "Lip matOfUpperLipPoint: " + matOfUpperLipPoint.toString());
        Log.d(TAG, "Lip matOfLowerLipPoint: " + matOfLowerLipPoint.toString());

        Imgproc.fillConvexPoly(upperLipMask, matOfUpperLipPoint, new Scalar(255, 255, 255, 255));
        Imgproc.fillConvexPoly(lowerLipMask, matOfLowerLipPoint, new Scalar(255, 255, 255, 255));
        // Imgproc.fillConvexPoly(overlay, matOfPoint, new Scalar(139, 0, 0, 255)); // 색상 및 투명도 설정

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
        Core.addWeighted(myLipMask, 0.5, overlay, 0.5, 0.0, blended);

        // 디버깅 로그 추가
        Log.d(TAG, "Mask size: " + myLipMask.size() + ", Overlay size: " + overlay.size());

        // Mat 객체를 Bitmap으로 변환
        Bitmap bmp = Bitmap.createBitmap(blended.cols(), blended.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blended, bmp);
        lipMaskBitmap = bmp;

        // 로그 추가: 비트맵 크기 및 null 확인
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

    private static final float LANDMARK_STROKE_WIDTH = 8F; // 랜드마크 선 두께
    private static final String TAG = "Face Landmarker Overlay"; // 태그
}


