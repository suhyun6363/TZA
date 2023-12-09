import React, { useState, useEffect } from "react";
import axios from "axios";
import "./Result.css";

const Result = () => {
  const [faceImages, setFaceImages] = useState([]);
  const [averageSkinImage, setAverageSkinImage] = useState("");
  const [personalColorResult, setPersonalColorResult] = useState("");

  useEffect(() => {
    const apiUrl = "http://your-api-endpoint"; //서버파고수정하기~

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
  }, []); // 컴포넌트가 마운트될 때 한 번만 실행

  return (
    <div className="result-container">
      <h2>결과 반환 페이지</h2>

      {faceImages.map((faceImage, index) => (
        <img key={index} src={faceImage} alt={`Face ${index + 1}`} />
      ))}

      <img src={averageSkinImage} alt="Average Skin" />

      <p>{personalColorResult}</p>
    </div>
  );
};

export default Result;
