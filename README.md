# 🎤 Voice-to-Text Keyboard (Android IME)

## Overview
This is a custom Android keyboard (IME) built for an internship technical assessment.  
It enables **voice-to-text input** using a single press-and-hold button, powered by **Groq's Whisper API**.

---

## ✨ Features
- 🟢 **Custom Keyboard (IME)** with a single mic button
- 🎙️ **Press & Hold Recording** – starts immediately when pressed, stops when released
- ⏳ **Transcription** after recording completes (no real-time streaming)
- 📝 **Automatic Text Insertion** at current cursor
- 🔄 **Visual Feedback States**:
  - Idle → “Hold to Speak”
  - Recording → “Recording…”
  - Processing → “Transcribing…”
  - Done → “Inserted ✓”  

---

## 🛠️ Tech Stack
- **Kotlin**
- **InputMethodService (IME)**
- **MediaRecorder** for audio capture
- **Retrofit2 + OkHttp** for API calls
- **Groq Whisper API** for transcription
- **Coroutines** for async tasks

---

## 🚀 Setup Instructions
1. Clone this repo:
   ```bash
   git clone https://github.com/YOUR_USERNAME/voice-to-text-keyboard.git
2.Open in Android Studio.

3.Add your Groq API key:
local.properites , build.gradle(app), apiConstantsfile
with your actual key.

3.Build & Run on device/emulator.

4.Enable the keyboard:

5.Settings → System → Languages & Input → Keyboards → Enable VoiceToTextKeyboard

6.Switch to it when typing.
