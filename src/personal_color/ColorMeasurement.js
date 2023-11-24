// ColorMeasurement.js

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
          <h2>퍼스널컬러 측정하기</h2>
          <button onClick={handleMeasureButtonClick}>측정하기</button>
        </div>
      ) : (
        <p>화면을 클릭하세요</p>
      )}
    </div>
  );
};

export default ColorMeasurement;
