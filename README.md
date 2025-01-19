# Videoverse Video API Assignment

This is a Spring Boot project developed as an assignment for **Videoverse**, implementing a RESTful API for managing video files. The application supports video uploads, trimming, merging, and link-sharing functionalities.

---

## ✨ Features

- **Authentication**: All API calls require static token-based authentication.
- **Video Uploads**: Upload videos with configurable size and duration limits.
- **Video Trimming**: Trim videos by specifying start and end times.
- **Video Merging**: Merge multiple video clips into a single file.
- **Link Sharing**: Generate shareable links with time-based expiry.
- **API Documentation**: Interactive API documentation available via Swagger.

---

## 🛠️ Technologies Used

- **Java 17**
- **Spring Boot 3**
- **Spring Security**
- **Hibernate with SQLite**
- **FFmpeg** (for video processing)
- **Swagger** (for API documentation)

---

## 📦 Setup Instructions

### Prerequisites

- Ensure **Java 17** and **Maven** are installed on your system.
- Install **FFmpeg** for video processing (ensure it's added to your system's PATH).

### Steps

1. **Clone the repository**:

bash
git clone <repository-url>
cd videoverse-video-api