package kr.ac.duksung.mycol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapExtractor;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FaceLandmarkerHelper {
    // 기본 설정 상수들
    private static final String TAG = "FaceLandmarkerHelper";
    private static final String MP_FACE_LANDMARKER_TASK = "face_landmarker.task";
    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;
    public static final float DEFAULT_FACE_DETECTION_CONFIDENCE = 0.5f;
    public static final float DEFAULT_FACE_TRACKING_CONFIDENCE = 0.5f;
    public static final float DEFAULT_FACE_PRESENCE_CONFIDENCE = 0.5f;
    public static final int DEFAULT_NUM_FACES = 1;
    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;

    // 인스턴스 변수들
    public float minFaceDetectionConfidence = DEFAULT_FACE_DETECTION_CONFIDENCE;
    public float minFaceTrackingConfidence = DEFAULT_FACE_TRACKING_CONFIDENCE;
    public float minFacePresenceConfidence = DEFAULT_FACE_PRESENCE_CONFIDENCE;
    public int maxNumFaces = DEFAULT_NUM_FACES;
    public int currentDelegate = DELEGATE_GPU;
    public RunningMode runningMode = RunningMode.IMAGE;
    public final Context context;
    public final LandmarkerListener faceLandmarkerHelperListener;
    public FaceLandmarker faceLandmarker;
    public Bitmap rotatedBitmap;

    public FaceLandmarkerHelper(
            float minFaceDetectionConfidence,
            float minFaceTrackingConfidence,
            float minFacePresenceConfidence,
            int maxNumFaces,
            int currentDelegate,
            RunningMode runningMode,
            Context context,
            LandmarkerListener faceLandmarkerHelperListener
    ) {
        this.minFaceDetectionConfidence = minFaceDetectionConfidence;
        this.minFaceTrackingConfidence = minFaceTrackingConfidence;
        this.minFacePresenceConfidence = minFacePresenceConfidence;
        this.maxNumFaces = maxNumFaces;
        this.currentDelegate = currentDelegate;
        this.runningMode = runningMode;
        this.context = context;
        this.faceLandmarkerHelperListener = faceLandmarkerHelperListener;

        // Face Landmarker 초기화
        setupFaceLandmarker();
    }

    public void clearFaceLandmarker() {
        if (faceLandmarker != null) {
            faceLandmarker.close();
            faceLandmarker = null;
        }
    }

    // FaceLandmarkerHelper의 실행 상태 반환
    public boolean isClose() {
        return faceLandmarker == null;
    }

    // 현재 설정을 사용하여 Face Landmarker 초기화
    public void setupFaceLandmarker() {
        BaseOptions.Builder baseOptionBuilder = BaseOptions.builder();

        switch (currentDelegate) {
            case DELEGATE_CPU:
                baseOptionBuilder.setDelegate(Delegate.CPU);
                break;
            case DELEGATE_GPU:
                baseOptionBuilder.setDelegate(Delegate.GPU);
                break;
        }

        baseOptionBuilder.setModelAssetPath(MP_FACE_LANDMARKER_TASK);

        if (runningMode == RunningMode.LIVE_STREAM && faceLandmarkerHelperListener == null) {
            throw new IllegalStateException("faceLandmarkerHelperListener는 runningMode가 LIVE_STREAM일 때 설정되어야 합니다.");
        }

        try {
            BaseOptions baseOptions = baseOptionBuilder.build();

            FaceLandmarker.FaceLandmarkerOptions.Builder optionsBuilder =
                    FaceLandmarker.FaceLandmarkerOptions.builder()
                            .setBaseOptions(baseOptions)
                            .setMinFaceDetectionConfidence(minFaceDetectionConfidence)
                            .setMinTrackingConfidence(minFaceTrackingConfidence)
                            .setMinFacePresenceConfidence(minFacePresenceConfidence)
                            .setNumFaces(maxNumFaces)
                            .setOutputFaceBlendshapes(true)
                            .setRunningMode(runningMode);

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                        .setResultListener(this::returnLivestreamResult)
                        .setErrorListener(this::returnLivestreamError);
            }

            FaceLandmarker.FaceLandmarkerOptions options = optionsBuilder.build();
            faceLandmarker = FaceLandmarker.createFromOptions(context, options);
            Log.d(TAG, "Face Landmarker initialized successfully");
        } catch (IllegalStateException e) {
            if (faceLandmarkerHelperListener != null) {
                faceLandmarkerHelperListener.onError("Face Landmarker 초기화 실패. 오류 로그를 참조하세요.", OTHER_ERROR);
            }
            Log.e(TAG, "MediaPipe가 오류로 인해 작업을 로드하지 못했습니다: " + e.getMessage());
        } catch (RuntimeException e) {
            if (faceLandmarkerHelperListener != null) {
                faceLandmarkerHelperListener.onError("Face Landmarker 초기화 실패. 오류 로그를 참조하세요.", GPU_ERROR);
            }
            Log.e(TAG, "Face Landmarker가 모델을 로드하지 못했습니다: " + e.getMessage());
        }
    }

    // ImageProxy를 MPImage로 변환하고 FaceLandmarker에 전달하여 실시간 스트림 감지
    public void detectLiveStream(ImageProxy imageProxy, boolean isFrontCamera) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw new IllegalArgumentException(
                    "RunningMode.LIVE_STREAM이 아닌 상태에서 detectLiveStream을 호출하려고 합니다."
            );
        }

        long frameTime = SystemClock.uptimeMillis();

        Bitmap bitmapBuffer = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
        // ImageProxy의 Plane 버퍼를 통해 Bitmap에 픽셀 복사
        // imageProxy.getPlanes()[0].getBuffer().rewind();
        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

        imageProxy.close();

        Matrix matrix = new Matrix();
        matrix.postRotate((float) imageProxy.getImageInfo().getRotationDegrees());

        if (isFrontCamera) {
            matrix.postScale(-1f, 1f, (float) imageProxy.getWidth(), (float) imageProxy.getHeight());
        }

        rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);
        // saveBitmap(rotatedBitmap);
        MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();
        // saveBitmap(mpImageToBitmap);

        detectAsync(mpImage, frameTime);
    }

    // MediaPipe Face Landmarker API를 사용하여 비동기적으로 얼굴 랜드마크 감지
    @VisibleForTesting
    public void detectAsync(MPImage mpImage, long frameTime) {
        if (faceLandmarker != null) {
            faceLandmarker.detectAsync(mpImage, frameTime);
        }
    }

/*
    // 사용자의 갤러리에서 로드된 비디오 파일의 URI를 받아서 비디오에 얼굴 랜드마커 추론을 실행
    public VideoResultBundle detectVideoFile(Uri videoUri, long inferenceIntervalMs) {
        if (runningMode != RunningMode.VIDEO) {
            throw new IllegalArgumentException(
                    "RunningMode.VIDEO가 아닌 상태에서 detectVideoFile을 호출하려고 합니다."
            );
        }

        long startTime = SystemClock.uptimeMillis();
        boolean didErrorOccurred = false;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUri);
        Long videoLengthMs = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        Bitmap firstFrame = retriever.getFrameAtTime(0);
        Integer width = firstFrame.getWidth();
        Integer height = firstFrame.getHeight();

        if (videoLengthMs == null || width == null || height == null) {
            return null;
        }

        List<FaceLandmarkerResult> resultList = new ArrayList<>();
        long numberOfFrameToRead = videoLengthMs / inferenceIntervalMs;

        for (int i = 0; i <= numberOfFrameToRead; i++) {
            long timestampMs = i * inferenceIntervalMs;

            Bitmap frame = retriever.getFrameAtTime(timestampMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
            if (frame != null) {
                Bitmap argb8888Frame = frame.getConfig() == Bitmap.Config.ARGB_8888 ? frame : frame.copy(Bitmap.Config.ARGB_8888, false);
                MPImage mpImage = new BitmapImageBuilder(argb8888Frame).build();

                FaceLandmarkerResult detectionResult = faceLandmarker.detectForVideo(mpImage, timestampMs);
                if (detectionResult != null) {
                    resultList.add(detectionResult);
                } else {
                    didErrorOccurred = true;
                    if (faceLandmarkerHelperListener != null) {
                        faceLandmarkerHelperListener.onError("detectVideoFile에서 ResultBundle을 반환할 수 없습니다.");
                    }
                }
            } else {
                didErrorOccurred = true;
                if (faceLandmarkerHelperListener != null) {
                    faceLandmarkerHelperListener.onError("비디오에서 지정된 시간에 프레임을 검색할 수 없습니다.");
                }
            }
        }

        retriever.release();
        long inferenceTimePerFrameMs = (SystemClock.uptimeMillis() - startTime) / numberOfFrameToRead;

        return didErrorOccurred ? null : new VideoResultBundle(resultList, inferenceTimePerFrameMs, height, width);
    }
*/

    // Bitmap을 받아서 얼굴 랜드마커 추론을 실행하고 결과 반환
    public void detectImage(Bitmap image) {
        if (runningMode != RunningMode.IMAGE) {
            throw new IllegalArgumentException("RunningMode.IMAGE가 아닌 상태에서 detectImage를 호출하려고 합니다.");
        }

        long startTime = SystemClock.uptimeMillis();
        MPImage mpImage = new BitmapImageBuilder(image).build();
        FaceLandmarkerResult landmarkResult = faceLandmarker.detect(mpImage);

        if (landmarkResult != null) {
            long inferenceTimeMs = SystemClock.uptimeMillis() - startTime;
            if (faceLandmarkerHelperListener != null) {
                faceLandmarkerHelperListener.onResults(new ResultBundle(landmarkResult, inferenceTimeMs, image.getHeight(), image.getWidth()));
            }
        } else {
            if (faceLandmarkerHelperListener != null) {
                faceLandmarkerHelperListener.onError("Face Landmarker가 감지에 실패했습니다.", OTHER_ERROR);
            }
        }
    }

    // 얼굴 랜드마크 결과를 반환
    private void returnLivestreamResult(FaceLandmarkerResult result, MPImage input) {
        if (result.faceLandmarks().size() > 0) {
            long finishTimeMs = SystemClock.uptimeMillis();
            long inferenceTime = finishTimeMs - result.timestampMs();

            if (faceLandmarkerHelperListener != null) {
                faceLandmarkerHelperListener.onResults(new ResultBundle(result, inferenceTime, input.getHeight(), input.getWidth()));
            }
        } else {
            if (faceLandmarkerHelperListener != null) {
                faceLandmarkerHelperListener.onEmpty();
            }
        }
    }

    // 감지 중 발생한 오류를 반환
    private void returnLivestreamError(RuntimeException error) {
        if (faceLandmarkerHelperListener != null) {
            faceLandmarkerHelperListener.onError(error.getMessage() != null ? error.getMessage() : "알 수 없는 오류가 발생했습니다.", OTHER_ERROR);
        }
    }

    // Face Landmarker 결과를 담은 번들 클래스
    public static class ResultBundle {
        private final FaceLandmarkerResult result;
        private final long inferenceTime;
        private final int inputImageHeight;
        private final int inputImageWidth;

        public ResultBundle(FaceLandmarkerResult result, long inferenceTime, int inputImageHeight, int inputImageWidth) {
            this.result = result;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }

        public FaceLandmarkerResult getResult() {
            return result;
        }

        public long getInferenceTime() {
            return inferenceTime;
        }

        public int getInputImageHeight() {
            return inputImageHeight;
        }

        public int getInputImageWidth() {
            return inputImageWidth;
        }
    }
    /*
    // 비디오 파일의 얼굴 랜드마커 결과를 담은 번들 클래스
    public static class VideoResultBundle {
        private final List<FaceLandmarkerResult> results;
        private final long inferenceTime;
        private final int inputImageHeight;
        private final int inputImageWidth;

        public VideoResultBundle(List<FaceLandmarkerResult> results, long inferenceTime, int inputImageHeight, int inputImageWidth) {
            this.results = results;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }

        public List<FaceLandmarkerResult> getResults() {
            return results;
        }

        public long getInferenceTime() {
            return inferenceTime;
        }

        public int getInputImageHeight() {
            return inputImageHeight;
        }

        public int getInputImageWidth() {
            return inputImageWidth;
        }
    }
    */
    // Face LandmarkerHelper의 리스너 인터페이스
    public interface LandmarkerListener {
        void onError(String error, int errorCode);

        void onResults(ResultBundle resultBundle);

        default void onEmpty() {
        }
    }
}
