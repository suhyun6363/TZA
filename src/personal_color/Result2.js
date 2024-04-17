import React, { useState, useEffect } from "react";
import PersonalColorDiagnosis from "./PersonalColorsDiagnosis";
import { useNavigate } from "react-router-dom";
import QRCode from "qrcode.react";
import "./Result2.css";

const Result2 = () => {
  const [imageSrc, setImageSrc] = useState("");
  const [analysisData, setAnalysisData] = useState({
    personal_color: "",
    second_color: "",
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const imageUrl =
      "http://127.0.0.1:8000/media/cluster_images/total_weighted_mean_color.png";
    const analysisUrl = "http://127.0.0.1:8000/analysis";

    const fetchData = async () => {
      try {
        // 이미지 가져오기
        const responseImage = await fetch(imageUrl);
        if (!responseImage.ok) {
          throw new Error(`HTTP error! Status: ${responseImage.status}`);
        }

        const blobImage = await responseImage.blob();
        const readerImage = new FileReader();

        readerImage.onloadend = () => {
          setImageSrc(readerImage.result);
        };

        readerImage.readAsDataURL(blobImage);

        // 분석 데이터 가져오기
        const responseAnalysis = await fetch(analysisUrl);
        if (!responseAnalysis.ok) {
          throw new Error(`HTTP error! Status: ${responseAnalysis.status}`);
        }

        const data = await responseAnalysis.json();
        console.log("Analysis Data:", data);

        // 가장 최근 데이터를 선택
        const latestData = data[data.length - 1];

        setAnalysisData({
          personal_color: latestData.personal_color,
          second_color: latestData.second_color,
        });

        setLoading(false); // 로딩 완료
      } catch (error) {
        console.error("Error fetching data:", error);
        setLoading(false); // 로딩 완료 (오류 발생)
      }
    };

    fetchData();
  }, []);

  // 두 정보를 합쳐서 하나의 문자열로 만듭니다.
  const combinedInfo = `${analysisData.personal_color}-${analysisData.second_color}`;

  const handleMeasureButtonClick = () => {
    // 연예인 이미지 페이지로
    navigate("/result3");
  };

  return (
    <div className="result2-page">
      <div className="result2-container">
        {loading ? (
          <p>Loading...</p>
        ) : (
          <div>
            <h2 id="result2-check-result">퍼스널 컬러 진단 결과</h2>
            <div className="result2-flex-container">
              <div className="image-and-qr">
                <div className="result2-image-section">
                  <p>추출 이미지</p>
                  <img
                    src={imageSrc}
                    alt="Total Weighted Mean Color"
                    className="skin-image"
                  />
                </div>
                <div className="qr-section">
                  {/* QR 코드를 표시합니다. */}
                  <QRCode value={combinedInfo} className="qr-code" />
                </div>
              </div>
              <div className="result2-section">
                {/* personal_color 정보를 그 다음에 표시합니다. */}
                <div className="color-info">
                  <div>
                    <b>Personal Color: {analysisData.personal_color}</b>
                    <PersonalColorDiagnosis
                      personalColor={analysisData.personal_color}
                      type="chart" // PersonalColorDiagnosis에게 컬러 차트 이미지를 표시하도록 타입을 전달
                    />
                  </div>
                  <div>
                    <b>Second Color: {analysisData.second_color}</b>
                    <PersonalColorDiagnosis
                      personalColor={analysisData.second_color}
                      type="chart" // PersonalColorDiagnosis에게 컬러 차트 이미지를 표시하도록 타입을 전달
                    />
                  </div>
                </div>
              </div>
              <div className="result2-button-container">
                <button
                  id="result2-etc-button"
                  onClick={handleMeasureButtonClick}
                >
                  컬러 대표 연예인 확인하기
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Result2;