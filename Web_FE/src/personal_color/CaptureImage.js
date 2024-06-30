import { useNavigate, useLocation } from "react-router-dom";
import "./CaptureImage.css";
import axios from "axios";
import Webcam from "react-webcam";

const CaptureImage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // location 상태에서 촬영된 이미지를 가져옵니다
  const capturedImage = location.state && location.state.capturedImage;

  const handleRetakeClick = () => {
    navigate("/capture");
  };

  const handleResultClick = async () => {
    navigate("/loading");
    if (capturedImage) {
      // capturedImage가 존재하는지 확인
      try {
        // 이미지 데이터 URL을 Blob으로 변환
        const response = await fetch(capturedImage);
        const blobImage = await response.blob();

        // FormData에 이미지 추가
        const formData = new FormData();
        formData.append("image", blobImage, "captured_image.jpg");

        // 이미지를 서버로 전송
        const uploadResponse = await axios.post(
          //"http://localhost:8000/api/upload/",
          "http://3.36.217.107/api/upload/",
          formData,
          {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          }
        );

        console.log("이미지 업로드 성공:", uploadResponse.data);

        navigate("/image", {
          state: { capturedImage: capturedImage }, // 이미지 경로를 다시 전달
        });
      } catch (error) {
        // 전송 실패한 경우 오류 처리
        console.error("이미지 업로드 실패:", error);
      }
    }
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
