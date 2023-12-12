// Result.js
import React, { useState, useEffect } from "react";
import axios from "axios";
import "./Result.css";

const Result = () => {
  const [faceImages, setFaceImages] = useState([]);
  const [averageSkinImage, setAverageSkinImage] = useState("");
  const [personalColorResult, setPersonalColorResult] = useState("");

  useEffect(() => {
    const apiUrl = "http://127.0.0.1:8000/analysis"; // 서버 주소로 수정

    // axios를 사용하여 데이터 가져오기
    axios
      .get(apiUrl)
      .then((response) => {
        const data = response.data;

        // 가져온 데이터를 상태 변수에 설정
        setFaceImages(data.faceImages);
        setAverageSkinImage(data.averageSkinImage);
        setPersonalColorResult(data.personalColorResult);
      })
      .catch((error) => {
        console.error("Error fetching data:", error);
      });
  }, []);

  return (
    <div className="result-container">
      <h2>결과 확인하기</h2>

      <div className="face-images">
        {faceImages.map((faceImage, index) => (
          <img key={index} src={faceImage} alt={`Face ${index + 1}`} />
        ))}
      </div>

      <img
        className="average-skin-image"
        src={averageSkinImage}
        alt="Average Skin"
      />

      <p className="personal-color-result">{personalColorResult}</p>
    </div>
  );
};

export default Result;
