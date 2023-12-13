// Result.js
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Result.css";

const ImageComponent = () => {
  const [imageSrcList, setImageSrcList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    // Django 서버에 있는 이미지들의 URL 배열
    const imageUrls = [
      'http://127.0.0.1:8000/media/cluster_images/cluster_1.png',
      'http://127.0.0.1:8000/media/cluster_images/cluster_2.png',
      'http://127.0.0.1:8000/media/cluster_images/cluster_3.png',
      'http://127.0.0.1:8000/media/face_only0.png'
    ];

    // 각 이미지 가져오기
    const getImageData = async () => {
      for (const url of imageUrls) {
        try {
          const response = await fetch(url);
          const blob = await response.blob();
          const reader = new FileReader();

          reader.onloadend = () => {
            // Base64로 변환된 이미지 데이터를 배열에 추가
            setImageSrcList((prevList) => [...prevList, reader.result]);
          };

          reader.onerror = (error) => {
            console.error("이미지를 읽는 중 오류가 발생했습니다:", error);
          };

          reader.readAsDataURL(blob);
        } catch (error) {
          console.error("이미지를 가져오는 중 오류가 발생했습니다:", error);
        }
      }
    };

    getImageData();
  }, []);

  const handleMeasureButtonClick = () => {
    // 측정하기
    navigate("/result2");
  };  

  return (
    <div className="outer-container">
      <div className="inner-container">
        <div className="grid-container">
          {imageSrcList.map((src, index) => (
            <img
              key={index}
              src={src}
              alt={`Cluster Image ${index + 1}`}
              className="grid-item"
            />
          ))}
        </div>
        <button id="etc-button" onClick={handleMeasureButtonClick}>퍼스널컬러 진단 받기</button>
      </div>
    </div>
  );
};

export default ImageComponent;





//기존 예은언니가 작성한 코드

// import React, { useState, useEffect } from "react";
// import axios from "axios";
// import "./Result.css";

// const Result = () => {
//   const [faceImages, setFaceImages] = useState([]);
//   const [averageSkinImage, setAverageSkinImage] = useState("");
//   const [personalColorResult, setPersonalColorResult] = useState("");

//   useEffect(() => {
//     const apiUrl = "http://127.0.0.1:8000/뭐시기뭐시기"; // 서버 주소로 수정

//     // axios를 사용하여 데이터 가져오기
//     axios
//       .get(apiUrl)
//       .then((response) => {
//         const data = response.data;

//         // 가져온 데이터를 상태 변수에 설정
//         setFaceImages(data.faceImages);
//         setAverageSkinImage(data.averageSkinImage);
//         setPersonalColorResult(data.personalColorResult);
//       })
//       .catch((error) => {
//         console.error("Error fetching data:", error);
//       });
//   }, []);

//   return (
//     <div className="result-container">
//       <h2>결과 확인하기</h2>

//       <div className="face-images">
//         {faceImages.map((faceImage, index) => (
//           <img key={index} src={faceImage} alt={`Face ${index + 1}`} />
//         ))}
//       </div>

//       <img
//         className="average-skin-image"
//         src={averageSkinImage}
//         alt="Average Skin"
//       />

//       <p className="personal-color-result">{personalColorResult}</p>
//     </div>
//   );
// };

// export default Result;
