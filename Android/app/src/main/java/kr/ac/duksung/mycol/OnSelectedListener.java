package kr.ac.duksung.mycol;

public interface OnSelectedListener {
    void onColorSelected(String type, int color);
    void onMakeupSelected(String selectedCategory);
    void onNoneSelected(String selectedCategory);
}

