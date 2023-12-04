// WebcamCapture.js

import React, { useRef, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Webcam from "react-webcam";
import "./WebcamCapture.css";
import axios from "axios";

const WebcamCapture = ({ onCapture }) => {
  const navigate = useNavigate();
  const webcamRef = useRef(null);
  const [countdown, setCountdown] = useState(5);
  const [capturedImage, setCapturedImage] = useState(null);

  useEffect(() => {
    const webcamElement = webcamRef.current.video;
    const overlayImage = document.getElementById("person-image");
  }, []);

  const handleCaptureClick = () => {
    setCountdown(5); // 초기화

    // 1초마다 countdown 값을 감소시키는 타이머
    const countdownTimer = setInterval(() => {
      setCountdown((prevCountdown) => prevCountdown - 1);
    }, 1000);

    // 5초 뒤에 실행되는 타이머
    setTimeout(async () => {
      clearInterval(countdownTimer); // 카운트다운 타이머 중지

      if (webcamRef.current) {
        const imageSrc = webcamRef.current.getScreenshot();

        try {
          // 이미지를 서버로 전송
          const formData = new FormData();
          formData.append("image", imageSrc);
          const response = await axios.post(
            "http://localhost:8000/api/upload/",
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
              },
            }
          );

          // 성공적으로 전송된 경우 처리
          console.log("이미지 업로드 성공:", response.data);

          // 전환을 위해 '/image'로 이동
        } catch (error) {
          // 전송 실패한 경우 오류 처리
          console.error("이미지 업로드 실패:", error);
        }
        // 서버로 이미지가 안보내져도 화면이 넘어가긴함!
        navigate("/image");
      }
    }, 5000); // 5초 후에 실행
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

      {/* 카운트다운 표시 */}
      {countdown > 0 && (
        <div className="countdown-indicator">{countdown}초 후에 촬영됩니다</div>
      )}
    </div>
  );
};

export default WebcamCapture;
