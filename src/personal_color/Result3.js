import React, { useState, useEffect } from "react";
import PersonalColorDiagnosis from "./PersonalColorsDiagnosis";
import { useNavigate } from "react-router-dom";
import "./Result3.css";

const Result3 = () => {
  const [analysisData, setAnalysisData] = useState({
    personal_color: "",
    second_color: "",
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const analysisUrl = "http://127.0.0.1:8000/analysis";

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

  // 두 정보를 합쳐서 하나의 문자열로 만듭니다.
  const combinedInfo = `${analysisData.personal_color}-${analysisData.second_color}`;

  const handleMeasureButtonClick = () => {
    // 측정하기
    navigate("/draping");
  };

  return (
    <div className="result3-container">
      {loading ? (
        <p>Loading...</p>
      ) : (
        <div>
          <h2 id="result3-check-result">퍼스널 컬러별 대표 연예인</h2>
          <div className="result3-flex-container">
            <div className="result3-section">
              {/* personal_color 정보를 그 다음에 표시합니다. */}
              <div>
                <PersonalColorDiagnosis
                  personalColor={analysisData.personal_color}
                  type="celebrities"
                />
              </div>

              {/* second_color 정보를 그 다음에 표시합니다. */}
              <div>
                <PersonalColorDiagnosis
                  personalColor={analysisData.second_color}
                  type="celebrities"
                />
              </div>
              <div className="result3-button-container">
                <button
                  id="result3-etc-button"
                  onClick={handleMeasureButtonClick}
                >
                  나와 어울리는 베스트 컬러 확인하기
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Result3;