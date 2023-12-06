// App.js

import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./App.css";
import CapturePage from "./personal_color/CapturePage";
import ColorMeasurement from "./personal_color/ColorMeasurement";
import CaptureImage from "./personal_color/CaptureImage";
import Result from "./personal_color/Result";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <Routes>
          <Route path="/" element={<ColorMeasurement />} />
          <Route path="/capture" element={<CapturePage />} />
          <Route path="/image" element={<CaptureImage />} />
          <Route path="/result" element={<Result />} />
        </Routes>
      </header>
    </div>
  );
}

export default App;
