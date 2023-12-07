// CaptureImage.js
// 촬영본 확인하는 페이지

import React from "react";
import { useNavigate } from "react-router-dom";
import "./CaptureImage.css";

const CaptureImage = ({ capturedImage }) => {
  const navigate = useNavigate();

  const handleRetakeClick = () => {
    navigate("/capture");
  };

  const handleResultClick = () => {
    navigate("/result");
  };

  return (
    <div className="capture-image-container">
      <h2>촬영된 이미지</h2>
      {capturedImage && <img src={capturedImage} alt="Captured" />}

      <div className="button-container">
        <button className="retake-button" onClick={handleRetakeClick}>
          다시 촬영하기
        </button>

        <button className="result-button" onClick={handleResultClick}>
          결과보기
        </button>
      </div>
    </div>
  );
};

export default CaptureImage;
