import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import "./CaptureImage.css";

const CaptureImage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [wearingInfo, setWearingInfo] = useState({});
  const [showResultButton, setShowResultButton] = useState(true); 

  // location 상태에서 촬영된 이미지를 가져옵니다
  const capturedImage = location.state && location.state.capturedImage;

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get("http://127.0.0.1:8000/wearing/latest");
        setWearingInfo(response.data);

        // 값이 있는 경우 분석 보기 버튼 표시X
        if (
          response.data.cap_wearing !== undefined &&
          response.data.glasses_wearing !== undefined &&
          (response.data.cap_wearing !== "" || response.data.glasses_wearing !== "")
        ) {
          setShowResultButton(false);
        }

      } catch (error) {
        console.error("착용 정보를 가져오는 데 실패했습니다.", error);
      }
    };

    fetchData();
  }, []);

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
  
      <div>
        {wearingInfo.cap_wearing !== undefined && wearingInfo.glasses_wearing !== undefined ? (
          <div>
            {wearingInfo.cap_wearing && (
              <p>{wearingInfo.cap_wearing}</p>
            )}
            {wearingInfo.glasses_wearing && (
              <p>{wearingInfo.glasses_wearing}</p>
            )}
            <div className="button-container">
              <button className="retake-button" onClick={handleRetakeClick}>
                다시 촬영하기
              </button>
              {(!wearingInfo.cap_wearing && !wearingInfo.glasses_wearing) && (
                <button className="result-button" onClick={handleResultClick}>
                  분석 보기
                </button>
              )}
            </div>
          </div>
        ) : (
          // 착용 정보가 존재하지 않거나 값이 비어있는 경우
          <div className="button-container">
            {showResultButton && (
              <button className="result-button" onClick={handleResultClick}>
                분석 보기
              </button>
            )}
            <button className="retake-button" onClick={handleRetakeClick}>
              다시 촬영하기
            </button>
          </div>
        )}
        
      </div>
    </div>
  );
};

export default CaptureImage;
