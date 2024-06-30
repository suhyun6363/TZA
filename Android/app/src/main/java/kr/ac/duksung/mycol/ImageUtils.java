package kr.ac.duksung.mycol;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    public static void saveBitmapToFile(Bitmap bitmap, String fileName) {
        FileOutputStream out = null;
        try {
            // 경로 설정
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "LipMakeup");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 파일 생성
            File file = new File(directory, fileName + ".jpg");
            out = new FileOutputStream(file);

            // Bitmap을 JPEG 형식으로 파일에 저장
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Log.d(TAG, "이미지 저장 성공: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "이미지 저장 실패", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

