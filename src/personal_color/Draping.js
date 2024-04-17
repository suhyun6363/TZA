import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Draping.css";

const BestImageComponent = () => {
  const [bestImageSrcList, setBestImageSrcList] = useState([]);
  const [secondImageSrcList, setSecondImageSrcList] = useState([]);
  const [personalColorSentence, setPersonalColorSentence] = useState('');
  const [secondColorSentence, setSecondColorSentence] = useState('');
  const navigate = useNavigate();
  const [analysisData, setAnalysisData] = useState({
    personal_color: "",
    second_color: "",
  });

  useEffect(() => {
    // Best Color 이미지 가져오기
    const getBestImageData = async () => {
      try {
        const bestImageUrls = [];
        for (let i = 1; i <= 5; i++) {
          // 내가 이부분을 로컬호스트로바꿧는데됐어 근데 윈도우에서 상관없으련지 모르겠어 한번 해봐바다들
          const imageUrl = `http://localhost:8000/media/best_draping/best_${i}.png`;
          bestImageUrls.push(imageUrl);
        }

        const bestResponses = await Promise.all(
          bestImageUrls.map((url) => fetch(url))
        );

        // 분석 데이터 가져오기
        const analysisUrl = "http://127.0.0.1:8000/analysis";

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

        const bestImageData = await Promise.all(
          bestResponses.map(async (response) => {
            if (!response.ok) {
              throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const blob = await response.blob();
            const reader = new FileReader();
            return new Promise((resolve, reject) => {
              reader.onloadend = () => {
                resolve(reader.result);
              };
              reader.onerror = reject;
              reader.readAsDataURL(blob);
            });
          })
        );

        setBestImageSrcList(bestImageData);
      } catch (error) {
        console.error("Best 이미지를 가져오는 중 오류가 발생했습니다:", error);
      }
    };

    // Second Color 이미지 가져오기
    const getSecondImageData = async () => {
      try {
        const secondImageUrls = [];
        for (let i = 1; i <= 5; i++) {
          const imageUrl = `http://localhost:8000/media/second_draping/second_${i}.png`;
          secondImageUrls.push(imageUrl);
        }

        const secondResponses = await Promise.all(
          secondImageUrls.map((url) => fetch(url))
        );

        const secondImageData = await Promise.all(
          secondResponses.map(async (response) => {
            if (!response.ok) {
              throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const blob = await response.blob();
            const reader = new FileReader();
            return new Promise((resolve, reject) => {
              reader.onloadend = () => {
                resolve(reader.result);
              };
              reader.onerror = reject;
              reader.readAsDataURL(blob);
            });
          })
        );

        setSecondImageSrcList(secondImageData);
      } catch (error) {
        console.error(
          "Second 이미지를 가져오는 중 오류가 발생했습니다:",
          error
        );
      }
    };

    getBestImageData();
    getSecondImageData();
  }, []);

  useEffect(() => {
    // personal_color 값에 따라 문장 선택
    if (analysisData.personal_color === 'Spring_warm_bright') {
      setPersonalColorSentence('발랄한 과즙상 분위기');
    } else if (analysisData.personal_color === 'Spring warm light') {
      setPersonalColorSentence('청초하고 여리여리한 분위기');
    } else if (analysisData.personal_color === 'Summer cool light') {
      setPersonalColorSentence('청초하고 시원한 분위기');
    } else if (analysisData.personal_color === 'Summer cool mute') {
      setPersonalColorSentence('차분하고 편안한 분위기');
    } else if (analysisData.personal_color === 'Autumn warm mute') {
      setPersonalColorSentence('부드럽고 우아한 분위기');
    } else if (analysisData.personal_color === 'Autumn warm deep') {
      setPersonalColorSentence('고급스럽고 안정된 분위기');
    } else if (analysisData.personal_color === 'Winter cool bright') {
      setPersonalColorSentence('시원하고 세련된 분위기');
    } else if (analysisData.personal_color === 'Winter cool deep') {
      setPersonalColorSentence('무게감있고 고급스러운 분위기');
    }
    
    // second_color 값에 따라 문장 선택
    if (analysisData.second_color === 'Spring_warm_bright') {
      setSecondColorSentence('발랄한 과즙상 분위기');
    } else if (analysisData.second_color === 'Spring warm light') {
      setSecondColorSentence('청초하고 여리여리한 분위기');
    } else if (analysisData.second_color === 'Summer cool light') {
      setSecondColorSentence('청초하고 시원한 분위기');
    } else if (analysisData.second_color === 'Summer cool mute') {
      setSecondColorSentence('차분하고 편안한 분위기');
    } else if (analysisData.second_color === 'Autumn warm mute') {
      setSecondColorSentence('부드럽고 우아한 분위기');
    } else if (analysisData.second_color === 'Autumn warm deep') {
      setSecondColorSentence('고급스럽고 안정된 분위기');
    } else if (analysisData.second_color === 'Winter cool bright') {
      setSecondColorSentence('시원하고 세련된 분위기');
    } else if (analysisData.second_color === 'Winter cool deep') {
      setSecondColorSentence('무게감있고 고급스러운 분위기');
    }
  }, [analysisData]);

  const handleMeasureButtonClick = () => {
    navigate("/");
  };

  return (
    <div className="draping-outer-container">
      <div className="draping-inner-container">
        <h2 id="draping-best-color">Best Color #{personalColorSentence}</h2>
        <div className="draping-grid-container">
          {/* Best Color 이미지 출력 */}
          {bestImageSrcList.map((src, index) => (
            <div key={`best_${index}`} className="draping-grid-item-container">
              <img
                src={src}
                alt={`Best Color ${index + 1}`}
                className="draping-grid-item"
              />
              {index === 4}
            </div>
          ))}
        </div>
        <h2 id="draping-second-color">Second Color #{secondColorSentence}</h2>
        <div className="draping-grid-container">
          {/* Second Color 이미지 출력 */}
          {secondImageSrcList.map((src, index) => (
            <div
              key={`second_${index}`}
              className="draping-grid-item-container"
            >
              <img
                src={src}
                alt={`Second Color ${index + 1}`}
                className="draping-grid-item"
              />
              {index === 4}
            </div>
          ))}
        </div>
        {/* <button id="draping-etc-button" onClick={handleMeasureButtonClick}>
          처음으로 돌아가기
        </button> */}
      </div>
    </div>
  );
};

export default BestImageComponent;