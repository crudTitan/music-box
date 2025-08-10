// UploadForm.jsx
import React, { useState } from 'react';
import axios from 'axios';

export default function UploadForm() {
  const [file, setFile] = useState(null);
  const [storageType, setStorageType] = useState('LOCAL');
  const token = localStorage.getItem('token');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) return alert('Please select a file');

    const formData = new FormData();
    formData.append('file', file);

    try {
      await axios.post(`http://localhost:8080/api/songs/upload?storageType=${storageType}`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      alert('Upload successful!');
    } catch (err) {
      console.error(err);
      alert('Upload failed');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Storage Type:{" "}
        <select value={storageType} onChange={e => setStorageType(e.target.value)}>
          <option value="LOCAL">LOCAL</option>
          <option value="S3">S3</option>
        </select>
      </label>
      <br />
      <input type="file" onChange={e => setFile(e.target.files[0])} />
      <button type="submit">Upload</button>
    </form>
  );
}
