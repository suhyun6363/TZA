import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import PersonalColorDiagnosis from "./PersonalColorsDiagnosis";
import "./Result2.css";

const colorToTitleMap = {
  "Spring warm bright": "러블리의 인간화",
  "Spring warm light": "봄날의 아이콘",
  "Summer cool light": "싱그러운 청량감의 대표",
  "Summer cool mute": "청초함의 끝판왕",
  "Autumn warm deep": "독보적인 분위기",
  "Autumn warm mute": "가을 햇살의 주인공",
  "Winter cool bright": "쿨하지만 화려한",
  "Winter cool deep": "도시적이고 세련된",
  "N-Spring warm light": "봄날의 아이콘 & 싱그러운 청량감의 대표",
  "N-Summer cool light": "싱그러운 청량감의 대표 & 봄날의 아이콘",
  "N-Spring warm bright": "러블리의 인간화 & 쿨하지만 화려한",
  "N-Winter cool bright": "쿨하지만 화려한 & 러블리의 인간화",
  "N-Autumn warm deep": "독보적인 분위기 & 도시적이고 세련된",
  "N-Winter cool deep": "도시적이고 세련된 & 독보적인 분위기",
  "N-Summer cool mute": "청초함의 끝판왕 & 가을 햇살의 주인공",
  "N-Autumn warm mute": "가을 햇살의 주인공 & 청초함의 끝판왕",
};

const imageMap = {
  "Spring warm bright": "Spring_warm_bright_g.png",
  "Spring warm light": "Spring_warm_light_g.png",
  "Summer cool light": "Summer_cool_light_g.png",
  "Summer cool mute": "Summer_cool_mute_g.png",
  "Autumn warm deep": "Autumn_warm_deep_g.png",
  "Autumn warm mute": "Autumn_warm_mute_g.png",
  "Winter cool bright": "Winter_cool_bright_g.png",
  "Winter cool deep": "Winter_cool_depp_g.png",
  "N-Spring warm light": "v_h_s_l.png",
  "N-Summer cool light": "v_h_s_l.png",
  "N-Spring warm bright": "v_h_s_h.png",
  "N-Winter cool bright": "v_h_s_h.png",
  "N-Autumn warm deep": "v_l_s_h.png",
  "N-Winter cool deep": "v_l_s_h.png",
  "N-Summer cool mute": "v_l_s_l.png",
  "N-Autumn warm mute": "v_l_s_l.png",
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
    const imageUrl =
      "http://3.36.217.107/media/cluster_images/total_weighted_mean_color.png";
    // "http://localhost:8000/media/cluster_images/total_weighted_mean_color.png";
    const analysisUrl = "http://3.36.217.107/analysis/";
    // const analysisUrl = "http://localhost:8000/analysis/";

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

  const goBack = () => {
    navigate(-1); // Navigates back to the previous page
  };

  const displayColor = (color) => {
    return color.startsWith('N-') ? color.substring(2) : color;
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
        <div className="result2-content">
          <div className="image-and-qr">
            <div className="result2-image-section">
              {analysisData.personal_color.startsWith('N-') ? (
                <p>당신은<br/>웜쿨 가리지 않는 뉴트럴톤</p>
              ) : (
                <p>당신은<br/>{analysisData.personal_color}</p>
              )}
              <img src={imageSrc} alt="Total Weighted Mean Color" className="skin-image" />
              <div className="personal-color-graph">
                <img src={getImageSrc(analysisData.personal_color)} alt={`${analysisData.personal_color}`} style={{ width: "auto", height: "auto" }} />
              </div>
            </div>
          </div>
          <div className="color-info_1">
            <div className="best-color-section">
              <div className="best-color-title">
                <b>Best : {displayColor(analysisData.personal_color)}</b>
              </div>
              <p>가장 잘 어울리는 색상 추천</p>
              <PersonalColorDiagnosis personalColor={displayColor(analysisData.personal_color)} type="chart" />
            </div>
          </div>
          <div className="color-info_2">
            <div className="second-color-section">
              <div className="second-color-title">
                <b>Second : {displayColor(analysisData.second_color)}</b>
              </div>
              <p>두번째로 잘 어울리는 색상</p>
              <PersonalColorDiagnosis personalColor={displayColor(analysisData.second_color)} type="chart" />
            </div>
          </div>
        </div>
      )}
      <div className="result2-button-container">
        <button id="result2-back-button" onClick={goBack}>
          뒤로 가기
        </button>
        <button id="result2-etc-button" onClick={handleMeasureButtonClick}>
          컬러 대표 연예인 확인하기
        </button>
      </div>
    </div>
  );
};

export default Result2;
