// App.js

import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./App.css";
import CapturePage from "./personal_color/CapturePage";
import ColorMeasurement from "./personal_color/ColorMeasurement";
import CaptureImage from "./personal_color/CaptureImage";
import Loading from "./personal_color/Loading";
import Result from "./personal_color/Result";
import Result2 from "./personal_color/Result2";
import Result3 from "./personal_color/Result3";
import Draping from "./personal_color/Draping";
import Draping2 from "./personal_color/Draping2";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <Routes>
          <Route path="/" element={<ColorMeasurement />} />
          <Route path="/capture" element={<CapturePage />} />
          <Route path="/loading" element={<Loading />} />
          <Route path="/image" element={<CaptureImage />} />
          <Route path="/result" element={<Result />} />
          <Route path="/result2" element={<Result2 />} />
          <Route path="/result3" element={<Result3 />} />
          <Route path="/draping" element={<Draping />} />
          <Route path="/draping2" element={<Draping2 />} />
        </Routes>
      </header>
    </div>
  );
}

export default App;
