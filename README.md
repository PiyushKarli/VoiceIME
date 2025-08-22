#  VoiceIME – Custom Speech-to-Text Keyboard

VoiceIME is a custom Android **keyboard (IME)** built in **Java**.  
It lets users press & hold a single button to record speech, sends the audio to the **Groq Whisper API**, and inserts the transcribed text directly into any text field.

---

##  Features
- Custom **Input Method Service (IME)**
- Press & hold button → record audio
- Release button → audio sent to **Groq Whisper API**
- Transcribed text inserted at cursor position
- Visual feedback for **Idle / Recording / Processing / Done / Error**
- Runtime microphone permission handling

---

##  Setup

1. **Clone this repo**:
   ```bash
   git clone https://github.com/<your-username>/VoiceIME.git

2. **Open in Android Studio**
3. **Add your Groq API Key**
   Open the root gradle.properties file
   Add:
     GROQ_API_KEY=your_key_here
   Sync Gradle 
   Your API key will now be accessible in code as:
     BuildConfig.GROQ_API_KEY
4. **Build & run** on a device or emulator (Android 8.0 / API 26+)


## Usage
- Launch the VoiceIME app
  Tap Enable Keyboard → turn on VoiceIME in system settings
  Tap Select Keyboard → switch to VoiceIME

- Open any app with a text field (Messages, Notes, WhatsApp, etc.)

- Hold the big button → speak
  Release → transcription sent + text inserted

## Requirements
- Android 8.0 (API 26) or higher
- Internet connection
- Groq API key (get one at Groq Console)
