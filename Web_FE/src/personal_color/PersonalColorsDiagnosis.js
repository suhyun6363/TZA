// PersonalColorDiagnosis.js
import React, { useEffect, useState } from "react";
import PersonalColors from "./PersonalColors";

const PersonalColorDiagnosis = ({ personalColor, type }) => {
  const [members, setMembers] = useState(null);

  useEffect(() => {
    if (type === "celebrities") {
      const foundChart = PersonalColors.find(
        (item) => item.color === personalColor
      );

      if (foundChart) {
        setMembers(foundChart.members);
      } else {
        setMembers([]); // 해당 색상에 대한 정보가 없으면 빈 배열로 설정
      }
    }
  }, [personalColor, type]);

  return (
    <div>
      {type === "celebrities" && members ? ( // 연예인 정보를 표시할 때
        <>
          {/* <p>{`${personalColor}`}에 해당하는 연예인: {members.join(", ")}</p> */}
          <img
            src={`image/${personalColor
              .replace(/\s+/g, "_")
              .toLowerCase()}_c.png`}
            alt={`${personalColor} 대표 연예인 이미지`}
            style={{ width: "100%", height: "370px", borderRadius: "5%" }}
          />
        </>
      ) : type === "chart" ? ( // 컬러 차트 이미지를 표시할 때
        <img
          src={`image/${personalColor.replace(/\s+/g, "_").toLowerCase()}.png`}
          alt={`${personalColor} 차트`}
          style={{ width: "100%", height: "340px" }}
        />
      ) : (
        <p>정보를 찾을 수 없습니다.</p>
      )}
    </div>
  );
};

export default PersonalColorDiagnosis;
