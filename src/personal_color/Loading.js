import React from 'react';
import classes from './Loading.css';

const Loading = () => {

    return (
        <div className={classes.loadingContainer}>
            <h1>퍼스널 컬러 분석 중</h1>
            <img src="/Spinner2.gif" alt="Spinner" />
        </div>
    );
}

export default Loading;
