// qr.js

import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import QRCode from "qrcode.react";
import "./qr.css";

const QR = () => {
  const navigate = useNavigate();
  const [analysisData, setAnalysisData] = useState({
    personal_color: "",
    second_color: "",
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const analysisUrl = "http://3.36.217.107/analysis/";

    const fetchData = async () => {
      try {
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

  const combinedInfo = analysisData.personal_color;

  const handleMeasureButtonClick = () => {
    navigate("/");
  };

  const goBack = () => {
    navigate(-1);
  };

  return (
    <div>
      <div className="result-section">
        <h2 id="intro-app">QR 코드를 스캔하고 앱으로 자세한 정보를 확인해보세요!</h2>
        <div className="introductionforapp">
          <h3 id="more-intro-app">우리 앱은 여러분에게 더욱 자세한 퍼스널 컬러 정보를 제공합니다. <br />추천 화장품부터 다양한 아이템까지, 당신에게 어울리는 제품을 추천해 드립니다.</h3>
        </div>
        {/* Display QR code first */}
        <QRCode value={combinedInfo} className="qr-code" />
  
        {/* Display personal_color information next */}
        <div>
          {/* Display additional personal color information here */}
        </div>
      </div>
  
      {/* Move the draping-button-container outside of the result-section */}
      <div className="draping-button-container">
        <button id="draping-back-button" onClick={goBack}>
          ⏎
        </button>
        <button id="draping-etc-button" onClick={handleMeasureButtonClick}>
          처음으로 돌아가기
        </button>
      </div>
    </div>
  );
  
};
export default QR;
