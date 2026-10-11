package com.example.ultron

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var chat: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var speakReplies = true
    private var selectedLanguage = "hi-IN"

    companion object {
        private const val SPEECH_REQUEST = 100
        private const val MIC_PERMISSION_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 12, 25)
        window.navigationBarColor = Color.rgb(8, 12, 25)
        tts = TextToSpeech(this, this)
        buildInterface()
        addMessage("ULTRON", "System online. नमस्ते! मैं ULTRON हूँ। नीचे लिखें या mic बटन दबाकर बोलें।")
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(8, 12, 25))
        }

        val header = TextView(this).apply {
            text = "ULTRON"
            textSize = 28f
            setTextColor(Color.rgb(80, 220, 255))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(2))
        }
        root.addView(header, LinearLayout.LayoutParams(-1, -2))

        val subtitle = TextView(this).apply {
            text = "PERSONAL AI • HINDI / HINGLISH"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, dp(12))
        }
        root.addView(subtitle)

        scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(15, 22, 40))
        }
        chat = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        scroll.addView(chat)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val languageRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(4))
        }
        val hindiButton = makeButton("हिंदी") {
            selectedLanguage = "hi-IN"
            addMessage("ULTRON", "भाषा हिंदी पर सेट है।")
        }
        val englishButton = makeButton("English") {
            selectedLanguage = "en-US"
            addMessage("ULTRON", "Language set to English.")
        }
        val voiceButton = makeButton("Voice: ON") {
            speakReplies = !speakReplies
            it.text = if (speakReplies) "Voice: ON" else "Voice: OFF"
            addMessage("ULTRON", if (speakReplies) "Voice replies enabled." else "Voice replies disabled.")
        }
        languageRow.addView(hindiButton, LinearLayout.LayoutParams(0, dp(42), 1f))
        languageRow.addView(englishButton, LinearLayout.LayoutParams(0, dp(42), 1f))
        languageRow.addView(voiceButton, LinearLayout.LayoutParams(0, dp(42), 1f))
        root.addView(languageRow)

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        input = EditText(this).apply {
            hint = "Message ULTRON..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 16f
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEND
            setPadding(dp(12), 0, dp(12), 0)
            setBackgroundColor(Color.rgb(28, 38, 60))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendTypedMessage()
                    true
                } else false
            }
        }
        val micButton = makeButton("🎙") { startSpeechInput() }
        val sendButton = makeButton("SEND") { sendTypedMessage() }
        inputRow.addView(input, LinearLayout.LayoutParams(0, dp(50), 1f))
        inputRow.addView(micButton, LinearLayout.LayoutParams(dp(58), dp(50)).apply {
            leftMargin = dp(6)
        })
        inputRow.addView(sendButton, LinearLayout.LayoutParams(dp(72), dp(50)).apply {
            leftMargin = dp(6)
        })
        root.addView(inputRow)

        setContentView(root)
    }

    private fun makeButton(label: String, action: (Button) -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(0, 110, 150))
            setOnClickListener { action(this) }
        }
    }

    private fun sendTypedMessage() {
        val message = input.text.toString().trim()
        if (message.isEmpty()) return
        input.setText("")
        handleUserMessage(message)
    }

    private fun handleUserMessage(message: String) {
        addMessage("YOU", message)
        val answer = respond(message)
        addMessage("ULTRON", answer)
        if (speakReplies && ttsReady) speak(answer)
    }

    private fun respond(raw: String): String {
        val q = raw.lowercase(Locale.ROOT).trim()
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val date = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

        return when {
            listOf("hello", "hi", "hey", "नमस्ते", "हेलो", "salam", "assalam").any { q.contains(it) } ->
                if (selectedLanguage == "hi-IN") "नमस्ते, Creator! ULTRON तैयार है। बताइए, क्या करना है?"
                else "Hello, Creator. ULTRON is ready. What would you like to do?"

            q.contains("time") || q.contains("समय") || q.contains("kitne baje") || q.contains("baje hain") ->
                if (selectedLanguage == "hi-IN") "अभी समय $now है।" else "The current time is $now."

            q.contains("date") || q.contains("tareekh") || q.contains("तारीख") || q.contains("आज कौन सा दिन") ->
                if (selectedLanguage == "hi-IN") "आज $date है।" else "Today is $date."

            q.contains("your name") || q.contains("tumhara naam") || q.contains("तुम्हारा नाम") ->
                if (selectedLanguage == "hi-IN") "मेरा नाम ULTRON है। मैं आपके फोन पर चलने वाला शुरुआती assistant हूँ।"
                else "My name is ULTRON. I'm a starter assistant running on your phone."

            q.contains("help") || q.contains("madad") || q.contains("मदद") ->
                if (selectedLanguage == "hi-IN")
                    "अभी मैं greeting, time और date बता सकता हूँ। General AI answers, live weather, web search और phone controls अभी connect नहीं हैं।"
                else
                    "I can handle greetings, time and date right now. General AI answers, live weather, web search, and phone controls are not connected yet."

            q.contains("weather") || q.contains("mausam") || q.contains("मौसम") ->
                if (selectedLanguage == "hi-IN") "Live weather अभी connect नहीं है, इसलिए मैं मौसम का अनुमान नहीं लगाऊँगा।"
                else "Live weather isn't connected yet, so I won't guess the conditions."

            q.contains("who are you") || q.contains("kaun ho") || q.contains("कौन हो") ->
                if (selectedLanguage == "hi-IN") "मैं ULTRON हूँ—एक शुरुआती mobile assistant। अभी मेरा जवाब देने वाला brain basic commands तक सीमित है।"
                else "I'm ULTRON, a starter mobile assistant. My response engine is currently limited to basic commands."

            else ->
                if (selectedLanguage == "hi-IN")
                    "मैंने सुना: \"$raw\"। अभी मेरा local brain इस सवाल का जवाब नहीं दे सकता। बाद में हम इसमें AI model connect करेंगे।"
                else
                    "I heard: \"$raw\". My local starter brain can't answer that yet. We can connect an AI model later."
        }
    }

    private fun startSpeechInput() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION_REQUEST)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguage)
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (selectedLanguage == "hi-IN") "ULTRON को बोलें" else "Speak to ULTRON")
        }
        try {
            @Suppress("DEPRECATION")
            startActivityForResult(intent, SPEECH_REQUEST)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Speech recognition is not available on this phone.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Using activity result for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull()
            if (!spokenText.isNullOrBlank()) handleUserMessage(spokenText)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MIC_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechInput()
            } else {
                Toast.makeText(this, "Microphone permission is needed for voice input.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale("hi", "IN")
            val result = tts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!ttsReady) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ultron-response")
    }

    private fun addMessage(sender: String, message: String) {
        val bubble = TextView(this).apply {
            text = "$sender\n$message"
            textSize = 15f
            setTextColor(if (sender == "YOU") Color.rgb(170, 240, 255) else Color.WHITE)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(if (sender == "YOU") Color.rgb(20, 55, 75) else Color.rgb(35, 45, 68))
        }
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        }
        chat.addView(bubble, params)
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
