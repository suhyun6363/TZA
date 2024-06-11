package kr.ac.duksung.mycol;

// ColorAdapter.java
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private List<ColorItem> colorList;
    private String selectedCategory; // "블러셔" 또는 "립" 등의 타입
    private ColorOptionFragment fragment;

    public ColorAdapter(List<ColorItem> colorList, String selectedCategory, ColorOptionFragment fragment) {
        this.colorList = colorList;
        this.selectedCategory = selectedCategory;
        this.fragment = fragment;
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
               // 색상이 선택되었음을 ColorOptionFragment에 알림
                fragment.onColorSelected(color, selectedCategory);
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

}