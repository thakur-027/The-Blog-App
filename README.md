<h1 align="center">📝 The Blog App</h1>

<p align="center">
  A native Android blogging platform built with <strong>Kotlin</strong> and modern Jetpack libraries
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Android-API%2021+-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/License-Open%20Source-green?style=for-the-badge"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Room-Database-FF6B35?style=flat-square&logo=sqlite"/>
  <img src="https://img.shields.io/badge/Jetpack-ViewModel%20%7C%20LiveData%20%7C%20Navigation-4285F4?flat-square&logo=jetpackcompose"/>
  <img src="https://img.shields.io/badge/UI-Material%20Design%203-757575?flat-square&logo=materialdesign"/>
</p>

---

## Overview

The Blog App is a native Android application that lets users **create, read, and manage blog posts** entirely from their device. It demonstrates clean Android architecture with local data persistence, lifecycle-aware components, and a fluid Material Design interface - written entirely in Kotlin.

---

## Screenshots

<p align="center">
  <img src="contents/screen_login.png" width="180" alt="Login Screen"/>
  &nbsp;&nbsp;
  <img src="contents/screen_feed.png" width="180" alt="News Feed"/>
  &nbsp;&nbsp;
  <img src="contents/screen_add_blog.png" width="180" alt="Add Blog"/>
  &nbsp;&nbsp;
  <img src="contents/screen_articles.png" width="180" alt="Your Articles"/>
  &nbsp;&nbsp;
  <img src="contents/screen_profile.png" width="180" alt="Profile"/>
</p>

<p align="center">
  <sub>Login &nbsp;|&nbsp; News Feed &nbsp;|&nbsp; Add Blog &nbsp;|&nbsp; Your Articles &nbsp;|&nbsp; Profile</sub>
</p>

---

## Features

- **🔐 Auth Flow** — Login & Register screens with form validation
- **📰 News Feed** — Scrollable feed of all published posts with search bar
- **❤️ Like & Bookmark** — React to posts directly from the feed
- **📝 Create & Publish** — Add a new blog with title and description
- **✏️ Edit & Delete** — Full CRUD on your own articles
- **👤 Profile Screen** — View your info and manage your content
- **💾 Offline-First** — All data persisted locally with Room Database

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin (100%) |
| Architecture | MVVM (Model-View-ViewModel) |
| UI | XML Layouts · Data Binding · Material Design 3 |
| Lifecycle | ViewModel · LiveData |
| Navigation | Jetpack Navigation Component |
| Local Storage | Room Database (SQLite) |
| Build | Gradle (Kotlin DSL) |

---

## Architecture

The app follows a clean MVVM layered architecture:

```
┌──────────────────────────────┐
│       Presentation Layer     │  Activities · Fragments · XML
├──────────────────────────────┤
│        ViewModel Layer       │  Business logic · UI State
├──────────────────────────────┤
│        Repository Layer      │  Single source of truth
├──────────────────────────────┤
│        Data Source Layer     │  Room Database
└──────────────────────────────┘
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11+
- Android SDK · API Level 21 (Android 5.0) minimum

### Setup

```bash
# Clone the repository
git clone https://github.com/thakur-027/The-Blog-App.git

# Open in Android Studio
# File → Open → Navigate to cloned folder → OK
# Wait for Gradle sync to complete
```

### Build & Run

```bash
# Debug APK (Unix/macOS)
./gradlew assembleDebug

# Debug APK (Windows)
gradlew.bat assembleDebug
```

Or press **Shift + F10** in Android Studio to run directly on your emulator or device.

> **Enable USB Debugging** on a physical device: Settings → About Phone → tap *Build Number* 7× → Developer Options → USB Debugging ✓

---

## Project Structure

```
The-Blog-App/
├── app/
│   └── src/main/
│       ├── java/         # Kotlin source — activities, fragments, VMs, repos
│       ├── res/          # Layouts, drawables, strings, themes
│       └── AndroidManifest.xml
├── contents/             # Screenshots and media assets
├── build.gradle.kts      # App-level build config
└── settings.gradle.kts   # Project settings
```

---

## Roadmap

- [ ] Firebase Authentication + cloud sync
- [ ] Image upload & media attachments
- [ ] Rich text editor (bold, italic, headings)
- [ ] Comment system
- [ ] Categories & tags
- [ ] Social sharing

---

## Author

**Ayush Thakur**  
Pre-final year ECE student @ SMVIT Bengaluru · Android Developer

[![GitHub](https://img.shields.io/badge/GitHub-thakur--027-181717?style=flat-square&logo=github)](https://github.com/thakur-027)

---

## License

This project is open source.
