# 🎵 Music Box

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

**Music Box** is a personal music library web application built with **Java**, **Spring Boot**, and **PostgreSQL**.  
It lets you upload, organize, and play your favorite songs — complete with album metadata and cover art.

---

##  Features
- Upload songs (`.mp3`, `.m4a`, etc.) with automatic metadata extraction
- Organize tracks into albums & artists
- JWT-based authentication for secure access
- Optional AWS S3 storage support
- Detailed logging for uploads and playback
- REST API for easy integration with frontends

---

## Tech Stack
- **Backend:** Java 17, Spring Boot, Hibernate
- **Database:** PostgreSQL
- **Storage:** Local filesystem / AWS S3
- **Auth:** JWT, Spring Security
- **Build Tool:** Maven

---

## Getting Started

### Clone the repository
```bash
git clone https://github.com/your-username/music-box.git
cd music-box

