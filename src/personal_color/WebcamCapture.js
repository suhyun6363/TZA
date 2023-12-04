// WebcamCapture.js

import React, { useRef, useEffect } from "react";
import Webcam from "react-webcam";
import "./WebcamCapture.css";
import axios from 'axios';

const WebcamCapture = ({ onCapture }) => {
  const webcamRef = useRef(null);

  useEffect(() => {
    const webcamElement = webcamRef.current.video;

    const overlayImage = document.getElementById("person-image");
  }, []);

  const handleCaptureClick = async () => {
    if (webcamRef.current) {
      const imageSrc = webcamRef.current.getScreenshot();
      try {
        // 이미지를 서버로 전송
        const formData = new FormData();
        formData.append('image, imageScr');
        const response = await axios.post("http://localhost:8000/api/upload/", {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });
        // 성공적으로 전송된 경우 처리
        console.log("이미지 업로드 성공:", response.data);
      } catch (error) {
        // 전송 실패한 경우 오류 처리
        console.error("이미지 업로드 실패:", error);
      }
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
