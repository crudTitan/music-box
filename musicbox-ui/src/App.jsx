import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Login from './Login';
import SongList from "./SongList";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [storageTypes, setStorageTypes] = useState([]);
  const [songs, setSongs] = useState([]);

  // Clears any stale tokens on full reload
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      clearSession();
      return;
    }
    // If token exists, try to verify it before marking as logged in
    verifyToken(token);
  }, []);

  const clearSession = () => {
    localStorage.removeItem('token');
    delete axios.defaults.headers.common['Authorization'];
    setIsLoggedIn(false);
    setStorageTypes([]);
    setSongs([]);
  };

  const verifyToken = async (token) => {
    try {
      // Optional: backend endpoint to validate token (could be /api/auth/validate)
      await axios.get('http://localhost:8080/api/auth/validate', {
        headers: { Authorization: `Bearer ${token}` }
      });
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setIsLoggedIn(true);
      await fetchStorageTypes();
      await fetchSongs();
    } catch (err) {
      console.warn('Invalid or expired token, clearing session', err);
      clearSession();
    }
  };

  const fetchStorageTypes = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/storage/types');
      setStorageTypes(res.data);
    } catch (err) {
      console.error('Failed to fetch storage types', err);
    }
  };

  const fetchSongs = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/songs/users');
      setSongs(res.data);
    } catch (err) {
      console.error('Failed to fetch songs', err);
    }
  };

  const handleLoginSuccess = (token) => {
    localStorage.setItem('token', token);
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    setIsLoggedIn(true);
    fetchStorageTypes();
    fetchSongs();
  };

  return (
    <>
      {isLoggedIn ? (
        <SongList storageTypes={storageTypes} songs={songs} />
      ) : (
        <Login onLoginSuccess={handleLoginSuccess} />
      )}
    </>
  );
}

export default App;
