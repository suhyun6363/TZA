// CapturePage.js

import React from "react";
import WebcamCapture from "./WebcamCapture";
import "./CapturePage.css";

const CapturePage = ({ onCapture }) => {
  return (
    <div>
      <h2>퍼스널컬러 측정하기</h2>
      {/* WebcamCapture 컴포넌트를 사용하여 웹캠 촬영 영역을 표시합니다. */}
      <WebcamCapture onCapture={onCapture} />
    </div>
  );
};

export default CapturePage;