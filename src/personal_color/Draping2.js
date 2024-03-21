import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Draping2.css";

const SecondImageComponent = () => {
  const [imageSrcList, setImageSrcList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const getImageData = async () => {
      try {
        const imageUrls = [];
        for (let i = 1; i <= 8; i++) {
          const imageUrl = `http://127.0.0.1:8000/media/second_draping/second_${i}.png`;
          imageUrls.push(imageUrl);
        }

        const responses = await Promise.all(imageUrls.map(url => fetch(url)));

        const imageData = await Promise.all(
          responses.map(async (response) => {
            if (!response.ok) {
              throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const blob = await response.blob();
            const reader = new FileReader();
            return new Promise((resolve, reject) => {
              reader.onloadend = () => {
                resolve(reader.result);
              };
              reader.onerror = reject;
              reader.readAsDataURL(blob);
            });
          })
        );

        setImageSrcList(imageData);
      } catch (error) {
        console.error("이미지를 가져오는 중 오류가 발생했습니다:", error);
      }
    };

    getImageData();
  }, []);

  const handleMeasureButtonClick = () => {
    navigate("/");
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
              {index === 7 && <p>Second Color</p>}
            </div>
          ))}
        </div>
        <button id="etc-button" onClick={handleMeasureButtonClick}>처음으로 돌아가기</button>
      </div>
    </div>
  );
};

export default SecondImageComponent;
