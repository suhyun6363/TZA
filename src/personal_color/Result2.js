import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import PersonalColorDiagnosis from "./PersonalColorsDiagnosis";
import "./Result2.css";

const colorToTitleMap = {
  "Spring warm bright": "러블리의 인간화",
  "Spring warm light": "봄날의 아이콘",
  "Summer cool light": "싱그럽운 청량감의 대표",
  "Summer cool mute": "청초함의 끝판왕",
  "Autumn warm deep": "독보적인 분위기",
  "Autumn warm mute": "가을 햇살의 주인공",
  "Winter cool bright": "쿨하지만 화려한",
  "Winter cool deep": "도시적이고 세련된"
};

const imageMap = {
  "Spring warm bright": "Spring_warm_bright_g.png",
  "Spring warm light": "Spring_warm_light_g.png",
  "Summer cool light": "Summer_cool_light_g.png",
  "Summer cool mute": "Summer_cool_mute_g.png",
  "Autumn warm deep": "Autumn_warm_deep_g.png",
  "Autumn warm mute": "Autumn_warm_mute_g.png",
  "Winter cool bright": "Winter_cool_bright_g.png",
  "Winter cool deep": "Winter_cool_depp_g.png"
};

const getImageSrc = (personalColor) => {
  return `image/${imageMap[personalColor] || 'default_image.png'}`;
};

const Result2 = () => {
  const [imageSrc, setImageSrc] = useState("");
  const [analysisData, setAnalysisData] = useState({
    personal_color: "",
    second_color: "",
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  
  useEffect(() => {
    const imageUrl = "http://127.0.0.1:8000/media/cluster_images/total_weighted_mean_color.png";
    const analysisUrl = "http://127.0.0.1:8000/analysis";

    const fetchData = async () => {
      try {
        const responseImage = await fetch(imageUrl);
        const responseAnalysis = await fetch(analysisUrl);

        if (!responseImage.ok || !responseAnalysis.ok) {
          throw new Error(`HTTP error! Status: ${responseImage.status}`);
        }

        const blobImage = await responseImage.blob();
        const readerImage = new FileReader();

        readerImage.onloadend = () => {
          setImageSrc(readerImage.result);
        };

        readerImage.readAsDataURL(blobImage);

        const data = await responseAnalysis.json();
        setAnalysisData({
          personal_color: data[data.length - 1].personal_color,
          second_color: data[data.length - 1].second_color,
        });
      } catch (error) {
        console.error("Error fetching data:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleMeasureButtonClick = () => {
    navigate("/result3");
  };

  const headerTitle = colorToTitleMap[analysisData.personal_color] || "퍼스널 컬러 진단 결과";

  return (
    <div className="result2-page">
      <div className="header-section">
        <h1>{headerTitle}</h1>
      </div>
      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="result2-container">
          <div className="image-and-qr">
            <div className="result2-image-section">
              <p>당신은 <br/> {analysisData.personal_color}</p>
              <img src={imageSrc} alt="Total Weighted Mean Color" className="skin-image" />
            </div>
          </div>
          <div className="personal-color-graph">
            <img src={getImageSrc(analysisData.personal_color)} alt={`${analysisData.personal_color}`} style={{ width: "100%", height: "auto" }} />
          </div>
          <div className="result2-section">
            <div className="color-info">
              <div className="best-color-section">
                <div className="best-color-title">
                  <b>Best : {analysisData.personal_color}</b>
                </div>
                <p>가장 잘 어울리는 색상 추천</p>
                <PersonalColorDiagnosis personalColor={analysisData.personal_color} type="chart" />
              </div>
              <div className="second-color-section">
                <div className="second-color-title">
                  <b>Second : {analysisData.second_color}</b>
                </div>
                <p>두번째로 잘 어울리는 색상</p>
                <PersonalColorDiagnosis personalColor={analysisData.second_color} type="chart" />
              </div>
            </div>
          </div>
        </div>
      )}
      <div className="result2-button-container">
        <button id="result2-etc-button" onClick={handleMeasureButtonClick}>
          컬러 대표 연예인 확인하기
        </button>
      </div>
    </div>
  );
};

export default Result2;
