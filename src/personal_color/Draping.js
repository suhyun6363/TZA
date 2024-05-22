import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Draping.css";

const BestImageComponent = () => {
  const [bestImageSrcList, setBestImageSrcList] = useState([]);
  const [secondImageSrcList, setSecondImageSrcList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    // Best Color 이미지 가져오기
    const getBestImageData = async () => {
      try {
        const bestImageUrls = [];
        for (let i = 1; i <= 5; i++) {
          const imageUrl = `http://3.36.217.107/media/best_draping/best_${i}.png`;
          // const imageUrl = `http://localhost:8000/media/best_draping/best_${i}.png`;
          bestImageUrls.push(imageUrl);
        }

        const bestResponses = await Promise.all(
          bestImageUrls.map((url) => fetch(url))
        );

        const bestImageData = await Promise.all(
          bestResponses.map(async (response) => {
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

        setBestImageSrcList(bestImageData);
      } catch (error) {
        console.error("Best 이미지를 가져오는 중 오류가 발생했습니다:", error);
      }
    };

    // Second Color 이미지 가져오기
    const getSecondImageData = async () => {
      try {
        const secondImageUrls = [];
        for (let i = 1; i <= 5; i++) {
          //const imageUrl = `http://localhost:8000/media/second_draping/second_${i}.png`;
          const imageUrl = `http://3.36.217.107/media/second_draping/second_${i}.png`;
          secondImageUrls.push(imageUrl);
        }

        const secondResponses = await Promise.all(
          secondImageUrls.map((url) => fetch(url))
        );

        const secondImageData = await Promise.all(
          secondResponses.map(async (response) => {
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

        setSecondImageSrcList(secondImageData);
      } catch (error) {
        console.error(
          "Second 이미지를 가져오는 중 오류가 발생했습니다:",
          error
        );
      }
    };

    getBestImageData();
    getSecondImageData();
  }, []);

  const handleMeasureButtonClick = () => {
    navigate("/");
  };

  const goBack = () => {
    navigate(-1); // Navigates back to the previous page
  };

  return (
    <div className="draping-outer-container">
      <div className="draping-inner-container">
        <h2 id="draping-best-color">베스트 컬러 드레이핑</h2>
        <div className="draping-grid-container">
          {/* Best Color 이미지 출력 */}
          {bestImageSrcList.map((src, index) => (
            <div key={`best_${index}`} className="draping-grid-item-container">
              <img
                src={src}
                alt={`Best Color ${index + 1}`}
                className="draping-grid-item"
              />
              {index === 4}
            </div>
          ))}
        </div>
        <h2 id="draping-second-color">세컨드 컬러</h2>
        <div className="draping-grid-container">
          {/* Second Color 이미지 출력 */}
          {secondImageSrcList.map((src, index) => (
            <div
              key={`second_${index}`}
              className="draping-grid-item-container"
            >
              <img
                src={src}
                alt={`Second Color ${index + 1}`}
                className="draping-grid-item"
              />
              {index === 4}
            </div>
          ))}
        </div>
        <div className="draping-button-container">
          <button id="draping-back-button" onClick={goBack}>
            ⏎
          </button>
          <button id="draping-etc-button" onClick={handleMeasureButtonClick}>
            처음으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
};

export default BestImageComponent;
