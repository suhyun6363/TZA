import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Draping.css";

const BestImageComponent = () => {
  const [imageSrcList, setImageSrcList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    // Django 서버에 있는 이미지들의 URL 배열
    const imageUrls = [];

    for (let i = 1; i <= 8; i++) {
      const imageUrl = `http://127.0.0.1:8000/media/best_draping/best_${i}.png`;
      imageUrls.push(imageUrl);
    }

    // 각 이미지 가져오기
    const getImageData = async () => {
      for (const url of imageUrls) {
        try {
          const response = await fetch(url);
          if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
          }

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
    navigate("/draping2");
  }; 

  return (
    <div className="outer-container">
      <div className="inner-container">
        <div className="grid-container">
          {imageSrcList.map((src, index) => (
            <div key={index} className="grid-item-container">  
              <img
                src={src}
                alt={`draping ${index + 1}`}
                className="grid-item"
              />
            {index === 7 && <p>Best Color</p>}
            </div>
          ))}
        </div>
        <button id="etc-button" onClick={handleMeasureButtonClick}>나와 어울리는 세컨드 컬러 확인하기</button>
      </div>
    </div>
  );
  
  
};

export default BestImageComponent;
