//Register.jsx

import React, { useState } from "react";
import axios from "axios";

export default function Register({ onRegisterSuccess, onSwitchToLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleRegister = async () => {
    try {
      await axios.post("http://localhost:8080/api/auth/register", { username, password });
      alert("Registration successful! Please log in.");
      onRegisterSuccess();
    } catch (err) {
      console.error("Registration failed:", err);
      alert("Registration failed: " + (err.response?.data || "Unknown error"));
    }
  };

  return (
    <div>
      <h2>Register</h2>
      <input
        type="text"
        placeholder="Username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
      /><br />
      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      /><br />
      <button onClick={handleRegister}>Register</button>
      <p>
        Already have an account? <button onClick={onSwitchToLogin}>Login</button>
      </p>
    </div>
  );
}
