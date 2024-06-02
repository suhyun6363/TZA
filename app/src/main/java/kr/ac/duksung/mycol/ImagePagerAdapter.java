package kr.ac.duksung.mycol;

import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    private List<Integer> imageResIds = new ArrayList<>();

    public ImagePagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // 새로운 ImageFragment를 생성합니다.
        return ImageFragment.newInstance(imageResIds.get(position));
    }

    @Override
    public int getCount() {
        return imageResIds.size();
    }

    // 이미지를 업데이트하는 메서드를 수정합니다.
    public void updateImage(List<Integer> imageResIds) {
        this.imageResIds.clear();
        this.imageResIds.addAll(imageResIds);
        notifyDataSetChanged();
    }

    // restoreState() 메서드를 오버라이드하여 복원된 Fragment의 상태를 관리합니다.
    @Override
    public void restoreState(@Nullable Parcelable state, ClassLoader loader) {
        try {
            // 예외 처리를 하지 않고 상태 복원을 시도합니다.
            super.restoreState(state, loader);
        } catch (IllegalStateException e) {
            // 예외 발생 시 로그를 출력합니다.
            Log.e("ImagePagerAdapter", "Error restoring state: " + e.getMessage());
        } catch (NullPointerException e) {
            // 예외 발생 시 로그를 출력합니다.
            Log.e("ImagePagerAdapter", "Null pointer exception during state restore: " + e.getMessage());
        }
    }
}
