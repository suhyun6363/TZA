// WebcamCapture.js

import React, { useRef, useEffect } from "react";
import Webcam from "react-webcam";
import "./WebcamCapture.css";

const WebcamCapture = ({ onCapture }) => {
  const webcamRef = useRef(null);

  useEffect(() => {
    const webcamElement = webcamRef.current.video;

    const overlayImage = document.getElementById("person-image");
  }, []);

  const handleCaptureClick = () => {
    if (webcamRef.current) {
      const imageSrc = webcamRef.current.getScreenshot();
      onCapture(imageSrc);
    }
  };

  return (
    <div id="video-container">
      {/* mirrored 속성을 true로 설정하여 웹캠 피드를 좌우로 뒤집습니다. */}
      <Webcam
        audio={false}
        ref={webcamRef}
        screenshotFormat="image/jpeg"
        mirrored={true}
      />

      {/* 이미지를 겹치게 표시할 부분 */}
      <div className="overlay-image-container">
        <img id="person-image" src="/person.png" alt="Person Image" />
      </div>

      {/* 촬영 버튼 */}
      <button onClick={handleCaptureClick}>촬영하기</button>
    </div>
  );
};

export default WebcamCapture;
