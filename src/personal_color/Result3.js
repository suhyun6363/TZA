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
    const analysisUrl = "http://3.36.217.107/analysis/";

    const fetchData = async () => {
      try {
        const responseAnalysis = await fetch(analysisUrl);
        if (!responseAnalysis.ok) {
          throw new Error(`HTTP error! Status: ${responseAnalysis.status}`);
        }

        const data = await responseAnalysis.json();
        console.log("Analysis Data:", data);

        const latestData = data[data.length - 1];
        setAnalysisData({
          personal_color: latestData.personal_color,
          second_color: latestData.second_color,
        });

        setLoading(false);
      } catch (error) {
        console.error("Error fetching data:", error);
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const displayColor = (color) => {
    return color.startsWith('N-') ? color.substring(2) : color;
  };

  const handleMeasureButtonClick = () => {
    navigate("/draping");
  };

  const goBack = () => {
    navigate(-1);
  };

  return (
    <>
      <div className="result3-container">
        {loading ? (
          <p>Loading...</p>
        ) : (
          <div>
            <h2 id="result3-check-result">퍼스널 컬러별 대표 연예인</h2>
            <div className="result3-flex-container">
              <div>
                <PersonalColorDiagnosis
                  personalColor={displayColor(analysisData.personal_color)}
                  type="celebrities"
                />
              </div>
              <div>
                <PersonalColorDiagnosis
                  personalColor={displayColor(analysisData.second_color)}
                  type="celebrities"
                />
              </div>
            </div>
          </div>
        )}
      </div>
      <div className="result3-button-container">
        <button id="result3-back-button" onClick={goBack}>
          ⏎
        </button>
        <button id="result3-etc-button" onClick={handleMeasureButtonClick}>
          나와 어울리는 베스트 컬러 확인하기
        </button>
      </div>
    </>
  );  
};

export default Result3;
