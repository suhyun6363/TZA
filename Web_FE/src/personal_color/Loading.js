import React from 'react';
import './Loading.css';

const Loading = () => {
    return (
        <div className="scan">
            <div className="face">
                <div className="dots"></div>
            </div>
            <h3>컬러 분석 중</h3>
        </div>
    );
}

export default Loading;
