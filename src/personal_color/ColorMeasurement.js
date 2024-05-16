// ColorMeasurement.js
// 초기화면

import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./ColorMeasurement.css";

const ColorMeasurement = () => {
  const [isClicked, setIsClicked] = useState(false);
  const navigate = useNavigate();

  const handleScreenClick = () => {
    setIsClicked(true);
  };

  const handleMeasureButtonClick = () => {
    // 측정하기
    navigate("/capture");
  };

  return (
    <div
      className={`color-measurement ${isClicked ? "clicked" : ""}`}
      onClick={handleScreenClick}
    >
      {isClicked ? (
        <div className="measurement-screen">
          <h2>당신의 퍼스널컬러를 알아보세요</h2>
          <p>안경, 모자 등 얼굴을 가리는 것들을 제거해주세요</p>
          <p>얼굴 주변의 머리카락들을 정리해주세요</p>
          <p1>추후 비식별화된 진단 정보가 연구용으로 사용될 수 있습니다. </p1>
          <button className="start-button" onClick={handleMeasureButtonClick}>측정하기</button>
        </div>
      ) : (
        <p id="click-msg">화면을 클릭하세요</p>
      )}
    </div>
  );
};

export default ColorMeasurement;
