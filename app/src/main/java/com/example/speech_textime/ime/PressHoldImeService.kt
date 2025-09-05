package com.example.speech_textime.ime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.speech_textime.`object`.ApiConstants
import com.example.speech_textime.R
import com.example.speech_textime.audio.PressHoldRecorder
import com.example.speech_textime.net.GroqWhisperApi
import com.example.speech_textime.net.WhisperRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class PressHoldImeService : InputMethodService(), View.OnTouchListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var btn: ImageButton
    private lateinit var btnText: TextView
    private lateinit var progress: ProgressBar

    private lateinit var recorder: PressHoldRecorder
    private var currentFile: File? = null
    private var transcribeJob: Job? = null

    // ✅ Use ApiConstants instead of BuildConfig
    private val apiKey by lazy { ApiConstants.GROQ_API_KEY }
    private val whisper by lazy { WhisperRepo(GroqWhisperApi.create(apiKey)) }

    override fun onCreateInputView(): View {
        val v = LayoutInflater.from(this).inflate(R.layout.ime_one_button, null)
        btn = v.findViewById(R.id.btnHold)
        btnText = v.findViewById(R.id.btn_hold_text)
        progress = v.findViewById(R.id.progress)
        btn.setOnTouchListener(this)
        recorder = PressHoldRecorder(this)
        return v
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        btn.isEnabled = hasMicPermission()
        btnText.text = if (btn.isEnabled) "Hold to Speak" else "Grant Mic Permission"
        progress.visibility = View.GONE
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestMicPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v?.id != R.id.btnHold) return false

        if (!hasMicPermission()) {
            requestMicPermission()
            return true
        }

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startRecordingUi()
                currentFile = recorder.start()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val file = recorder.stop()
                if (file == null) {
                    resetIdle("Hold to Speak")
                    return true
                }

                startProcessingUi()
                transcribeJob?.cancel()

                transcribeJob = serviceScope.launch {
                    try {
                        val text = whisper.transcribeFile(file)
                        insertAtCursor(text)
                        resetDone("Inserted ✓")
                    } catch (e: CancellationException) {
                        resetIdle("Hold to Speak")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        resetError("Try Again")
                    } finally {
                        file.delete()
                        currentFile = null
                    }
                }
                return true
            }
        }
        return false
    }

    private fun insertAtCursor(text: String) {
        val ic = currentInputConnection
        if (ic == null) {
            Log.e("IME", "InputConnection is null - cannot insert text")
            return
        }

        if (text.isNotBlank()) {
            Log.d("IME", "Inserting text at cursor: '$text'")
            ic.commitText(text, 1)
        } else {
            Log.w("IME", "Attempted to insert blank text")
        }
    }

    private fun startRecordingUi() {
        btn.isEnabled = true
        btn.setBackgroundResource(R.drawable.bg_listining)
        btn.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).start()
        btnText.text = "Recording… (release to stop)"
        progress.visibility = View.GONE
    }

    private fun startProcessingUi() {
        btn.isEnabled = false
        btnText.text = "Transcribing…"
        progress.visibility = View.VISIBLE
    }

    private fun resetIdle(text: String) {
        btn.setBackgroundResource(R.drawable.btn_bg)
        btn.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        btnText.text = text
        progress.visibility = View.GONE
        btn.isEnabled = true
    }

    private fun resetDone(label: String) {
        btn.isEnabled = true
        btnText.text = label
        progress.visibility = View.GONE
        btn.postDelayed({ resetIdle("Hold to Speak") }, 1200)
    }

    private fun resetError(message: String) {
        btn.isEnabled = true
        btn.setBackgroundResource(R.drawable.btn_bg)
        btnText.text = message
        progress.visibility = View.GONE
        btn.postDelayed({ resetIdle("Hold to Speak") }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        transcribeJob?.cancel()
        recorder.discard()
        serviceScope.cancel()
    }
}