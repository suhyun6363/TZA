// Draping2.js 파일

import React, { useState, useEffect } from "react";
import "./Draping2.css";

const SecondImageComponent = () => { // 이름을 변경함
  const [imageSrcList, setImageSrcList] = useState([]);

  useEffect(() => {
    // Django 서버에 있는 이미지들의 URL 배열
    const imageUrls = [];

    for (let i = 1; i <= 8; i++) {
      const imageUrl = `http://127.0.0.1:8000/media/second_draping/second_${i}.png`;
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
            {index === 7 && <p>Second Color</p>}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default SecondImageComponent; 
