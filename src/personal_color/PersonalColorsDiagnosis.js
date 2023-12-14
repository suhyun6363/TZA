// PersonalColorDiagnosis.js
import React, { useEffect, useState } from "react";
import PersonalColors from "./PersonalColors";

const PersonalColorDiagnosis = ({ personalColor }) => {
  const [chart, setChart] = useState(null);

  useEffect(() => {
    const foundChart = PersonalColors.find((item) => item.color === personalColor);

    if (foundChart) {
      setChart(foundChart);
    } else {
      setChart(null);
    }
  }, [personalColor]);

  return (
    <div>
      {chart ? (
        <div>
          <p>{`${personalColor}`}에 해당하는 연예인: {chart.members.join(", ")}</p>
          <img
            src={`image/${personalColor}.png`}
            alt={`${personalColor} 차트`}
            style={{ width: "100%", height: "200px" }}
          />
        </div>
      ) : (
        <p>정보를 찾을 수 없습니다.</p>
      )}
    </div>
  );
};

export default PersonalColorDiagnosis;


