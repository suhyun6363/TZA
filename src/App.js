// App.js

import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./App.css";
import CapturePage from "./personal_color/CapturePage";
import ColorMeasurement from "./personal_color/ColorMeasurement";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <Routes>
          <Route path="/" element={<ColorMeasurement />} />
          <Route path="/capture" element={<CapturePage />} />
        </Routes>
      </header>
    </div>
  );
}

export default App;
