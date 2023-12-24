import React, { useState, useEffect } from "react";
import PersonalColorDiagnosis from "./PersonalColorsDiagnosis";
import "./Result2.css";

const Result2 = () => {
    const [imageSrc, setImageSrc] = useState("");
    const [analysisData, setAnalysisData] = useState({ personal_color: "", second_color: "" });
    const [loading, setLoading] = useState(true);
  
    useEffect(() => {
      const imageUrl = "http://127.0.0.1:8000/media/cluster_images/total_weighted_mean_color.png";
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
  
    return (
      <div className="result-container">
        {loading ? (
          <p>Loading...</p>
        ) : (
          <div>
            <h2 id="check-result">진단 결과 확인하기</h2>
            <div className="image-section">
              <p>추출 이미지</p>
              <img src={imageSrc} alt="Total Weighted Mean Color" className="skin-image" />
            </div>
            <div className="result-section">
              <b>Personal Color: {analysisData.personal_color}</b>
              <PersonalColorDiagnosis personalColor={analysisData.personal_color} />
              <b>Second Color: {analysisData.second_color}</b>
              <PersonalColorDiagnosis personalColor={analysisData.second_color} />
            </div>
          </div>
        )}
      </div>
    );
  };

export default Result2;