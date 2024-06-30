package kr.ac.duksung.mycol;

// ColorItem.java

import java.util.List;

public class ColorItem {
    private List<Integer> rgbList;
    private String productName;
    private String optionName;

    public ColorItem(List<Integer> rgbList, String productName, String optionName) {
        this.rgbList = rgbList;
        this.productName = productName;
        this.optionName = optionName;
    }

    public List<Integer> getRgbList() {
        return rgbList;
    }

    public String getProductName() {
        return productName;
    }

    public String getOptionName() {
        return optionName;
    }
}

