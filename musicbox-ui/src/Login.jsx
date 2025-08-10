//Login.jsx


import React, { useState } from 'react';
import axios from 'axios';

function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = async () => {
    try {
      const res = await axios.post('http://localhost:8080/api/auth/login', {
        username,
        password
      });
      console.log('Login success:', res.data);
      onLoginSuccess(res.data.token);
    } catch (err) {
      console.error('Login failed:', err);
    }
  };

  return (
    <div>
      <input
        type="text"
        placeholder="Email"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
      />
      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
      <button onClick={handleLogin}>Log In</button>
    </div>
  );
}

export default Login;
