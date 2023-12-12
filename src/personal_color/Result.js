// Result.js
import React, { useState, useEffect } from "react";

const ImageComponent = () => {
  const [imageSrcList, setImageSrcList] = useState([]);

  useEffect(() => {
    // Django 서버에 있는 이미지들의 URL 배열
    const imageUrls = [
      "http://127.0.0.1:8000/media/cluster_images/cluster_1.png",
      "http://127.0.0.1:8000/media/cluster_images/cluster_2.png",
      "http://127.0.0.1:8000/media/cluster_images/cluster_3.png",
      "http://127.0.0.1:8000/media/cluster_images/total_weighted_mean_color.png",
    ];

    // 각 이미지 가져오기
    const getImageData = async () => {
      const promises = imageUrls.map((url) => {
        return new Promise((resolve, reject) => {
          const xhr = new XMLHttpRequest();
          xhr.open("GET", url);
          xhr.responseType = "blob";
          xhr.onload = () => {
            const reader = new FileReader();
            reader.onloadend = () => {
              // Base64로 변환된 이미지 데이터를 배열에 추가
              setImageSrcList((prevList) => [...prevList, reader.result]);
              resolve();
            };
            reader.onerror = reject;
            reader.readAsDataURL(xhr.response);
          };
          xhr.send();
        });
      });

      try {
        await Promise.all(promises);
      } catch (error) {
        console.error("Error fetching image:", error);
      }
    };

    getImageData();
  }, []);

  return (
    <div>
      {imageSrcList.map((src, index) => (
        <img key={index} src={src} alt={`Cluster Image ${index + 1}`} />
      ))}
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
