// Result.js
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Result.css";

const ImageComponent = () => {
  const [imageSrcList, setImageSrcList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchAnalysisData = async () => {
      try {
        // analysis 데이터를 가져옴
        const responseAnalysisData = await fetch(
          //"http://localhost:8000/analysis/"
          "http://3.36.217.107/analysis/"
        );
        if (!responseAnalysisData.ok) {
          throw new Error(`HTTP error! Status: ${responseAnalysisData.status}`);
        }

        const data = await responseAnalysisData.json();
        // 가장 최근 데이터를 선택
        const latestData = data[data.length - 1];
        const analysisId = latestData.id;

        // Django 서버에 있는 이미지들의 URL 배열
        const imageUrls = [
          // "http://localhost:8000/media/cluster_images/cluster_1.png",
          // "http://localhost:8000/media/cluster_images/cluster_2.png",
          // "http://localhost:8000/media/cluster_images/cluster_3.png",
          // `http://localhost:8000/media/face_analysis_${analysisId}.png`, // 최신 ID로 URL 설정
          "http://3.36.217.107/media/cluster_images/cluster_1.png",
          "http://3.36.217.107/media/cluster_images/cluster_2.png",
          "http://3.36.217.107/media/cluster_images/cluster_3.png",
          `http://3.36.217.107/media/face_analysis_${analysisId}.png`,
        ];

        // 각 이미지 가져오기
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
      } catch (error) {
        console.error("데이터를 가져오는 중 오류가 발생했습니다:", error);
      }
    };

    fetchAnalysisData();
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
