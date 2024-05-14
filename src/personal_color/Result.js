import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Result.css";

const ImageComponent = () => {
  const [imageSrcList, setImageSrcList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const imageUrls = [
      "http://127.0.0.1:8000/media/cluster_images/cluster_1.png",
      "http://127.0.0.1:8000/media/cluster_images/cluster_2.png",
      "http://127.0.0.1:8000/media/cluster_images/cluster_3.png",
      "http://127.0.0.1:8000/media/face_analysis.png",
    ];

    const getImageData = async () => {
      try {
        const responses = await Promise.all(imageUrls.map(url => fetch(url)));
        const blobs = await Promise.all(responses.map(response => {
          if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
          return response.blob();
        }));
        const imageSrcs = await Promise.all(blobs.map(blob => new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onloadend = () => resolve(reader.result);
          reader.onerror = reject;
          reader.readAsDataURL(blob);
        })));
        setImageSrcList(imageSrcs);
      } catch (error) {
        setError('Failed to load images');
        console.error("Error loading images:", error);
      } finally {
        setIsLoading(false);
      }
    };

    getImageData();
  }, []);

  const handleMeasureButtonClick = () => {
    navigate("/result2");
  };

  return (
    <div className="main-container">
      <h2 id="face-mesh">피부 색상 인식 완료</h2>
      {error && <p>{error}</p>}
      {isLoading ? (
        <p>Loading images...</p>
      ) : (
        <div className="outer-container">
          <div className="inner-container">
            <div className="grid-container-cluster">
              {imageSrcList.map((src, index) => (
                <img key={index} src={src} alt={`Cluster Image ${index + 1}`} className="grid-item" />
              ))}
            </div>
            <button id="etc-button" onClick={handleMeasureButtonClick}>
              퍼스널컬러 진단 받기
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ImageComponent;
