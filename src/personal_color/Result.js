// Result.js
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Result.css";

const ImageComponent = () => {
  const [imageSrcList, setImageSrcList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    // Django 서버에 있는 이미지들의 URL 배열
    const imageUrls = [
      "http://127.0.0.1:8000/media/cluster_images/cluster_1.png",
      "http://127.0.0.1:8000/media/cluster_images/cluster_2.png",
      "http://127.0.0.1:8000/media/cluster_images/cluster_3.png",
      "http://127.0.0.1:8000/media/face_analysis.png",
    ];

    // 각 이미지 가져오기
    const getImageData = async () => {
      for (const url of imageUrls) {
        try {
          const response = await fetch(url);
          const blob = await response.blob();
          const reader = new FileReader();

          reader.onloadend = () => {
            // Base64로 변환된 이미지 데이터를 배열에 추가
            setImageSrcList((prevList) => [...prevList, reader.result]);
          };

          reader.onerror = (error) => {
            console.error("이미지를 읽는 중 오류가 발생했습니다:", error);
          };

          reader.readAsDataURL(blob);
        } catch (error) {
          console.error("이미지를 가져오는 중 오류가 발생했습니다:", error);
        }
      }
    };

    getImageData();
  }, []);

  console.log(imageSrcList);

  const handleMeasureButtonClick = () => {
    // 측정하기
    navigate("/result2");
  };

  return (
    <div className="outer-container">
      <div className="inner-container">
        <div className="grid-container-cluster">
          {imageSrcList.map((src, index) => (
            <img
              key={index}
              src={src}
              alt={`Cluster Image ${index + 1}`}
              className="grid-item"
            />
          ))}
        </div>
        <button id="etc-button" onClick={handleMeasureButtonClick}>
          퍼스널컬러 진단 받기
        </button>
      </div>
    </div>
  );
};

export default ImageComponent;
