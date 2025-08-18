// 📄 SongList.jsx

import React, {useEffect, useState, useRef} from "react";
import axios from "axios";

export default function SongList({token, onLogout}) {
    const [songs, setSongs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [uploadTarget, setUploadTarget] = useState("local");
    const audioRef = useRef(null);
    const [currentSong, setCurrentSong] = useState(null);
    const [selectedFile, setSelectedFile] = useState(null);
    const fileInputRef = useRef(null);

    const fetchSongs = async () => {
        try {
            const authToken = token || localStorage.getItem("token");
            if (!authToken) {
                console.warn("No auth token found.");
                setLoading(false);
                return;
            }

            const res = await axios.get("http://localhost:8080/api/songs/users", {
                headers: {Authorization: `Bearer ${authToken}`},
            });

            if (Array.isArray(res.data)) {
                setSongs(res.data);
            } else {
                console.warn("Unexpected response:", res.data);
                setSongs([]);
            }
        } catch (err) {
            console.error("Error fetching songs:", err);
            setSongs([]);
        } finally {
            setLoading(false);
        }
    };

    const handleUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const authToken = token || localStorage.getItem("token");
        const formData = new FormData();
        formData.append("file", file);

        try {
            const endpoint =
                uploadTarget === "s3"
                    ? "http://localhost:8080/api/songs/upload?storageType=S3"
                    : "http://localhost:8080/api/songs/upload?storageType=LOCAL";


            await axios.post(endpoint, formData, {
                headers: {
                    Authorization: `Bearer ${authToken}`,
                    "Content-Type": "multipart/form-data",
                },
            });

            alert(`Uploaded to ${uploadTarget.toUpperCase()} successfully!`);
            fetchSongs(); // refresh list
        } catch (err) {
            console.error("Upload failed:", err);
            alert("Upload failed.");
        }
    };

    // // version 4
    const handlePlay = async (song) => {
        console.info("start handlePlay:", song);

        if (!audioRef.current) {
            console.warn("No audioRef.current found — audio element might not be mounted yet.");
            return;
        }

        setCurrentSong(song); // Update the UI with the current song

        const token = localStorage.getItem("token");
        if (!token) {
            console.warn("No JWT token found.");
            return;
        }

        const streamUrl = `http://localhost:8080/api/songs/stream/${song.id}`;

        try {
            const res = await fetch(streamUrl, {
                headers: { Authorization: `Bearer ${token}` },
            });

            if (!res.ok) throw new Error(`Failed to fetch audio: ${res.statusText}`);

            const blob = await res.blob();
            const audioUrl = URL.createObjectURL(blob);

            audioRef.current.src = audioUrl;
            audioRef.current.load();
            await audioRef.current.play();
            console.info("Playback started for:", song.title);
        } catch (err) {
            console.error("Playback failed:", err);
        }
    };



    const handleLogout = () => {
        localStorage.removeItem("token");
        if (onLogout) onLogout();
        window.location.reload(); // Optional: force re-render/login screen
    };


    useEffect(() => {
        fetchSongs();
    }, [token]);

    return (
        <div>

            <header style={{display: "flex", justifyContent: "space-between", alignItems: "center"}}>
                <h2>My Music</h2>
                {token || localStorage.getItem("token") ? (
                    <button onClick={handleLogout}>Logout</button>
                ) : (
                    <button onClick={() => window.location.href = "/login"}>Login</button>
                )}
            </header>


            {loading && <p>Loading songs...</p>}
            {!loading && songs.length > 0 && (
                <ul>
                    {songs.map((song, idx) => (
                        <li key={idx}>
                            {song.title || JSON.stringify(song)}
                            <button onClick={() => handlePlay(song)}>▶ Play</button>
                        </li>
                    ))}
                </ul>
            )}
            {!loading && songs.length === 0 && <p>No songs available</p>}

            {/*/!* Audio Player *!/*/}
            {/*{currentSong && (*/}
            {/*    <div style={{marginTop: "10px"}}>*/}
            {/*        <p>Now Playing: {currentSong.title}</p>*/}
            {/*        <audio ref={audioRef} controls>*/}
            {/*            <source src={currentSong.url} type="audio/mpeg"/>*/}
            {/*            Your browser does not support the audio element.*/}
            {/*        </audio>*/}
            {/*    </div>*/}
            {/*)}*/}

            {/* Audio Player */}
            <audio ref={audioRef} controls style={{ width: "100%", marginTop: "10px" }} />
            {currentSong && <p>Now Playing: {currentSong.title}</p>}



            {/* Upload Section */}
            <div style={{marginTop: "20px"}}>
                <h3>Upload More Music</h3>

                {/* Storage destination dropdown */}
                <label>
                    Destination:{" "}
                    <select
                        value={uploadTarget}
                        onChange={(e) => setUploadTarget(e.target.value)}
                    >
                        <option value="local">Local</option>
                        <option value="s3">S3</option>
                    </select>
                </label>

                {/* File input */}
                <input
                    ref={fileInputRef}
                    type="file"
                    accept="audio/*"
                    onChange={(e) => setSelectedFile(e.target.files[0])}
                    style={{display: "block", marginTop: "10px"}}
                />

                {/* Upload button */}
                <button
                    onClick={async () => {
                        if (!selectedFile) {
                            alert("Please select a file first.");
                            return;
                        }

                        const authToken = token || localStorage.getItem("token");
                        const formData = new FormData();
                        formData.append("file", selectedFile);

                        try {
                            const endpoint =
                                uploadTarget === "s3"
                                    ? "http://localhost:8080/api/songs/upload?storageType=S3"
                                    : "http://localhost:8080/api/songs/upload?storageType=LOCAL";

                            await axios.post(endpoint, formData, {
                                headers: {
                                    Authorization: `Bearer ${authToken}`,
                                    "Content-Type": "multipart/form-data",
                                },
                            });

                            alert(`Uploaded to ${uploadTarget.toUpperCase()} successfully!`);
                            setSelectedFile(null); // reset input
                            fileInputRef.current.value = null; // reset file input UI
                            fetchSongs(); // refresh song list
                        } catch (err) {
                            console.error("Upload failed:", err);
                            alert("Upload failed.");
                        }
                    }}
                    style={{marginTop: "10px"}}
                >
                    ⬆️ Upload
                </button>
            </div>

        </div>
    );
}
