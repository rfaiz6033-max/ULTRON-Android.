# ULTRON Android starter

This is a native Android/Kotlin starter project with:
- Simple chat-style interface
- Typed input
- Android speech-recognition intent for Hindi or English input
- Android TextToSpeech output (device language support varies)
- Basic local responses for greetings, time, date, and help

## Important limitations
This is not yet a full general-purpose AI. The response logic is local pattern matching. Live weather, web search, general AI, advanced memory, and phone actions are not connected. Python files from the earlier prototype are not executed by this Android project; Android cannot import them directly as Kotlin code.

## Open in Android Studio
1. Extract the ZIP.
2. Open Android Studio.
3. Choose **Open** and select the extracted `ULTRON_Android` folder.
4. Let Gradle sync. If Android Studio asks to install Android SDK Platform 35, accept.
5. Connect an Android phone with USB debugging enabled, or create an emulator.
6. Press **Run**.

## Build an APK
In Android Studio select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
The debug APK is normally created at:
`app/build/outputs/apk/debug/app-debug.apk`

## Voice notes
- Speech input relies on the Android speech recognition service available on the phone.
- Spoken output relies on Android TextToSpeech and installed language voice data.
- The app asks for microphone permission when the mic button is pressed.
- No API keys are included.


## Build without Android Studio (cloud build)

This repository includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`.
1. Create a GitHub repository and upload the contents of this folder to its `main` branch.
2. Open the repository's **Actions** tab.
3. Select **Build ULTRON APK**, click **Run workflow**, and run it on `main`.
4. When it finishes, open that workflow run and download the `ULTRON-debug-APK` artifact.
5. Extract the downloaded artifact ZIP to get `app-debug.apk`, then transfer the APK to your Android phone and open it to install. You may need to allow installation from that file manager; only install APKs you built or trust.

No emulator is required. GitHub account sign-in is needed for this cloud-build route.
