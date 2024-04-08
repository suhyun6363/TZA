import { useNavigate, useLocation } from "react-router-dom";
import "./CaptureImage.css";

const CaptureImage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // location 상태에서 촬영된 이미지를 가져옵니다
  const capturedImage = location.state && location.state.capturedImage;

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
      <div className="button-container-captureimage">
        <button className="retake-button" onClick={handleRetakeClick}>
          다시 촬영하기
        </button>
        <button className="result-button" onClick={handleResultClick}>
          분석 보기
        </button>
      </div>
    </div>
  );
};

export default CaptureImage;
