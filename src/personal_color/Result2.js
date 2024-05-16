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
  "N-Spring warm light": "웜&쿨 두가지 매력을 가진",
  "N-Summer cool light": "웜&쿨 두가지 매력을 가진",
  "N-Spring warm bright": "웜&쿨 두가지 매력을 가진",
  "N-Winter cool bright": "웜&쿨 두가지 매력을 가진",
  "N-Autumn warm deep": "웜&쿨 두가지 매력을 가진",
  "N-Winter cool deep": "웜&쿨 두가지 매력을 가진",
  "N-Summer cool mute": "웜&쿨 두가지 매력을 가진",
  "N-Autumn warm mute": "웜&쿨 두가지 매력을 가진",
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

const colorDescriptions = {
  "Spring warm light": "밝고 산뜻한 봄의 이미지가 느껴지는 톤입니다.\n 주로 흰색빛의 파스텔 계열이 잘 어울립니다.\n 명도와 채도가 낮은 색은 피해주세요!",
  "Spring warm bright": "귀엽고 러블리한 이미지가 느껴지는 톤입니다.\n과일처럼 생기 있고 상큼한 이미지에 맞게 고채도의 따뜻한 밝은 색이 잘 어울립니다.\n차갑고 어두운 색은 피해주세요!",
  "Summer cool light": "이온음료 광고가 떠오르는 여름의 싱그러움이 느껴지는 톤입니다.\n은은하고 부드러운 파스텔 톤이 가장 잘 어울립니다.\n린넨과 쉬폰 소재의 옷을 입으면 이미지를 더 살릴 수 있어요!",
  "Summer cool mute": "대체로 회색기가 많이 도는 톤다운된 파스텔 계열이 잘 어울립니다.\n깔끔하고 단아한 이미지가 매력적입니다.\n회기가 많이 도는 경우 자칫 칙칙해 보일 수 있어요.",
  "Autumn warm mute": "부드럽고 차분한 이미지가 떠오르는 톤입니다.\n회기가 돌지만 따뜻한 색이 잘 어울립니다.\n내추럴함이 돋보이는 메이크업과 스타일링이 베스트입니다!",
  "Autumn warm deep": "고급스럽지만 화려한 느낌이 나는 톤입니다.\n어둡지만 채도가 강한 색들이 잘 어울립니다.\n화려한 주얼리도 잘 어울립니다.",
  "Winter cool bright": "쿨하지만 화려한 이미지의 톤입니다.\n고채도의 원색에 가까운 쨍한 색이 잘 어울립니다.\n회기 도는 파스텔 계열은 피해주세요.",
  "Winter cool deep": "시크하고 도도한 이미지가 매력적인 톤입니다.\n진한 레드나 블랙이 누구보다 잘 어울립니다.\n노란 계열과 파스텔 톤은 피해주세요!"
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
    navigate(-1);
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
        <div className="result2-container">
          <div className="image-and-qr">
            <div className="result2-image-section">
            {analysisData.personal_color.startsWith('N-') ? (
                <p>당신은 다 잘 어울려요~!<br/> "뉴트럴톤"</p>
              ) : (
                <p>당신은<br/>{analysisData.personal_color}</p>
              )}
              <img src={imageSrc} alt="Total Weighted Mean Color" className="skin-image" />
            </div>
          </div>
          <div className="personal-color-graph">
            <img src={getImageSrc(analysisData.personal_color)} alt={`${analysisData.personal_color}`} style={{ width: "380px", height: "250.5px" }} />
            <div className="color-descriptions">
            {analysisData.personal_color.startsWith('N-') ? (
                <p>당신은 웜톤과 쿨톤, 두 가지를 모두 잘 소화할 수 있는<br/> 특별한 뉴트럴 톤을 가지고 계시네요.<br/> 더 자유롭게 여러 스타일을 시도할 수 있어요. <br/>뉴트럴 톤이지만, 당신에게 조금 더 조화로운 컬러가 있는데요, 바로 오른쪽에 나와 있는 계절의 톤이에요!</p>
              ) : (
                <p>{colorDescriptions[analysisData.personal_color]}</p>
              )}
            </div>
          </div>
          <div className="result2-section">
            <div className="color-info">
              <div className="best-color-section">
                <div className="best-color-title">
                  <b>Best : {displayColor(analysisData.personal_color)}</b>
                </div>
                <p>가장 잘 어울리는 색상 추천</p>
                <PersonalColorDiagnosis personalColor={displayColor(analysisData.personal_color)} type="chart" />
              </div>
              <div className="second-color-section">
                <div className="second-color-title">
                <b>Second : {displayColor(analysisData.second_color)}</b>
                </div>
                <p>두번째로 잘 어울리는 색상</p>
                <PersonalColorDiagnosis personalColor={displayColor(analysisData.second_color)} type="chart" />
              </div>
            </div>
          </div>
        </div>
      )}
      <div className="result2-button-container">
        <button id="result2-back-button" onClick={goBack}>
          ⏎
        </button>
        <button id="result2-etc-button" onClick={handleMeasureButtonClick}>
          컬러 대표 연예인 확인하기
        </button>
      </div>
    </div>
  );
};

export default Result2;