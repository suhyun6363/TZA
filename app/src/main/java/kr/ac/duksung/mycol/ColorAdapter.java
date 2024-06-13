package kr.ac.duksung.mycol;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private List<ColorItem> colorList;
    private String selectedCategory;
    private OnSelectedListener selectedListener;

    public ColorAdapter(List<ColorItem> colorList, String selectedCategory, OnSelectedListener selectedListener) {
        this.colorList = colorList;
        this.selectedCategory = selectedCategory;
        this.selectedListener = selectedListener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        ColorItem colorItem = colorList.get(position);

        // RGB 값을 가져와서 int로 변환
        int color = convertRgbListToColor(colorItem.getRgbList());

        // 변환된 색상 값을 사용하여 ImageView의 배경색 설정
        holder.colorImageView.setBackgroundColor(color);

        // 아이템을 클릭하면 선택된 색상을 알림
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Makeup 선택 상태 업데이트
                selectedListener.onMakeupSelected(selectedCategory);

                // 색상이 선택되었음을 ColorOptionFragment에 알림
                selectedListener.onColorSelected(selectedCategory, color);

                // productName과 optionName을 Toast 메시지로 출력
                Context context = v.getContext();
                String message = colorItem.getProductName() +"\n" + colorItem.getOptionName();
                showToast(context, message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        ImageView colorImageView;

        ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorImageView = itemView.findViewById(R.id.colorImageView);
        }
    }

    private int convertRgbListToColor(List<Integer> rgbList) {
        if (rgbList == null || rgbList.size() < 3) {
            throw new IllegalArgumentException("RGB list must have at least 3 values");
        }

        int red = rgbList.get(0);
        int green = rgbList.get(1);
        int blue = rgbList.get(2);

        // Color.rgb 메서드는 alpha 값을 255로 설정하여 반환합니다.
        return Color.rgb(red, green, blue);
    }

    private void showToast(Context context, String message) {
        // 커스텀 토스트 레이아웃 인플레이트
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // 메시지를 설정
        TextView textView = layout.findViewById(R.id.toast_text);
        textView.setText(message);

        // 토스트 생성 및 표시
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
