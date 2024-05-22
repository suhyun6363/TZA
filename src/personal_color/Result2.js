import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import PersonalColorDiagnosis from "./PersonalColorsDiagnosis";
import "./Result2.css";

const colorToTitleMap = {
  "Spring warm bright": "청순한 봄의 요정",
  "Spring warm light": "과일의 인간화",
  "Summer cool light": "청초함의 끝판왕",
  "Summer cool mute": "차분한 분위기",
  "Autumn warm deep": "차분하고 고급스러운",
  "Autumn warm mute": "부드럽고 우아한",
  "Winter cool bright": "강렬하고 화려한",
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
  "Spring warm light":
    "밝고 산뜻한 봄의 이미지가 느껴지는 톤이네요!\n 흰빛이 섞인 따뜻한 파스텔 계열이 가장 잘 어울려요.\n 명도와 채도가 낮은 색은 피해주세요!",
  "Spring warm bright":
    "귀엽고 러블리한 이미지가 느껴지는 톤이네요!\n과일처럼 생기있고 상큼한 이미지에 맞게 고채도의 따뜻한 밝은 색이 잘 어울려요.\n차갑고 어두운 색은 피해주세요!",
  "Summer cool light":
    "이온음료 광고가 떠오르는 여름의 청초함이 느껴지는 톤이네요!\n은은하고 부드러운 파스텔 톤이 가장 잘 어울려요.\n린넨과 쉬폰 소재의 옷을 입으면 이미지를 더 살릴 수 있어요!",
  "Summer cool mute":
    "차분하고 편안한 에너지를 갖고 계시네요!\n깔끔하고 단아한 이미지가 매력적인 톤이에요.\n깨끗한 셔츠 스타일링이 찰떡이에요.",
  "Autumn warm mute":
    "부드럽고 차분한 에너지를 갖고 계시네요!\n앤틱하고 에스닉한 무드가 잘 어울려요.\n내추럴함이 돋보이는 메이크업과 톤온톤 스타일링이 베스트!",
  "Autumn warm deep":
    "따뜻하고 차분한 이미지가 잘 어울리시네요!\n성숙하고 고급스러운 스타일링이 잘 어울려요.\n화려한 주얼리 스타일링을 누구보다 잘 소화하는 타입!",
  "Winter cool bright":
    "쿨하고 화려한 이미지가 잘 어울리시네요!\n대비감있는 스타일링이 잘 어울려요. \n회기 도는 파스텔 계열은 피해주세요!",
  "Winter cool deep":
    "시크하고 도도한 이미지가 매력적이시네요!.\n진한 레드나 블랙이 누구보다 잘 어울려요.\n노란 계열과 파스텔 톤은 피해주세요!",
};

const getImageSrc = (personalColor) => {
  return `image/${imageMap[personalColor] || "default_image.png"}`;
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
    //"http://localhost:8000/media/cluster_images/total_weighted_mean_color.png";
    const analysisUrl = "http://3.36.217.107/analysis/";
    //const analysisUrl = "http://localhost:8000/analysis/";

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
    return color.startsWith("N-") ? color.substring(2) : color;
  };

  const headerTitle =
    colorToTitleMap[analysisData.personal_color] || "퍼스널 컬러 진단 결과";

  return (
    <div className="result2-page">
      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="result2-container">
          <div className="result2-first-section">
            <div className="header-section">
              <h1>{headerTitle}</h1>
            </div>
            <div className="image-and-qr">
              <div className="result2-image-section">
                {analysisData.personal_color.startsWith("N-") ? (
                  <p>
                    당신은 다 잘 어울려요~!
                    <br /> "뉴트럴톤"
                  </p>
                ) : (
                  <p>
                    당신은
                    <br />
                    {analysisData.personal_color}
                  </p>
                )}
                <img
                  src={imageSrc}
                  alt="Total Weighted Mean Color"
                  className="skin-image"
                />
              </div>
            </div>
            <div className="personal-color-graph">
              <img
                src={getImageSrc(analysisData.personal_color)}
                alt={`${analysisData.personal_color}`}
                style={{ width: "380px", height: "250.5px" }}
              />
              <div className="color-descriptions">
                {analysisData.personal_color.startsWith("N-") ? (
                  <p>
                    당신은 웜톤과 쿨톤, 두 가지를 모두 잘 소화할 수 있는
                    <br /> 특별한 뉴트럴 톤을 가지고 계시네요.
                    <br /> 소화할 수 있는 컬러가 풍부한 타입이네요. <br />
                    뉴트럴 톤이지만, 당신에게 조금 더 어울리는 컬러를 오른쪽에서
                    알려드릴게요!
                  </p>
                ) : (
                  <p>{colorDescriptions[analysisData.personal_color]}</p>
                )}
              </div>
            </div>
          </div>

          <div className="result2-section">
            <div className="color-info">
              <div className="best-color-section">
                <div className="best-color-title">
                  <b>Best : {displayColor(analysisData.personal_color)}</b>
                </div>
                <p>가장 잘 어울리는 색상 추천</p>
                <PersonalColorDiagnosis
                  personalColor={displayColor(analysisData.personal_color)}
                  type="chart"
                />
              </div>
              <div className="second-color-section">
                <div className="second-color-title">
                  <b>Second : {displayColor(analysisData.second_color)}</b>
                </div>
                <p>두번째로 잘 어울리는 색상</p>
                <PersonalColorDiagnosis
                  personalColor={displayColor(analysisData.second_color)}
                  type="chart"
                />
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
