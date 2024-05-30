package kr.ac.duksung.mycol;

public class Product {
    private String name;
    private String optionName;
    private String imageUrl;

    public Product() {
        // 파이어스토어에서 객체를 생성할 때 기본 생성자가 필요합니다.
    }

    public Product(String name, String optionName, String imageUrl) {
        this.name = name;
        this.optionName = optionName;
        this.imageUrl = imageUrl;
    }

    // 이미지 URL을 받는 생성자 추가
    public Product(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getName() {
        if (name != null) {
            int firstSpaceIndex = name.indexOf(" ");
            if (firstSpaceIndex != -1) {
                String firstPart = name.substring(0, firstSpaceIndex);
                String secondPart = name.substring(firstSpaceIndex + 1);
                return firstPart + "\n" + secondPart;
            } else {
                return name;
            }
        } else {
            return "";
        }
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
