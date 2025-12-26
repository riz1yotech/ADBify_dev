package com.adbify.terminal

import android.annotation.SuppressLint
import android.content.Intent
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.adbify.MainActivity
import com.adbify.R
import com.adbify.utils.AndroidUtilities
import com.adbify.utils.Utilities

class TerminalViewClient(private val activity: MainActivity) : TerminalViewClientBase() {

    private var terminalCursorBlinkerStateAlreadySet = false

    fun onCreate() {
        with(activity.terminalView) {
            setTextSize(TerminalSettingsHelper.getFontSize(activity))
            keepScreenOn = TerminalSettingsHelper.shouldKeepScreenOn(activity)
        }
    }

    fun onStart() {
        activity.terminalView.setIsTerminalViewKeyLoggingEnabled(false)
    }

    fun onResume() {
        terminalCursorBlinkerStateAlreadySet = false
        if (activity.terminalView.mEmulator != null) {
            setTerminalCursorBlinkerState(true)
            terminalCursorBlinkerStateAlreadySet = true
        }
    }

    fun onStop() {
        setTerminalCursorBlinkerState(false)
    }

    override fun onEmulatorSet() {
        if (!terminalCursorBlinkerStateAlreadySet) {
            setTerminalCursorBlinkerState(true)
            terminalCursorBlinkerStateAlreadySet = true
        }
    }

    override fun onScale(scale: Float): Float {
        if (scale < 0.9f || scale > 1.1f) {
            val increase = scale > 1.0f
            changeFontSize(increase)
            return 1.0f
        }
        return scale
    }

    override fun onSingleTapUp(e: MotionEvent) {
        val term = activity.currentSession?.emulator ?: return
        if (!term.isMouseTrackingActive && !e.isFromSource(InputDevice.SOURCE_MOUSE)) {
            AndroidUtilities.showSoftKeyboard(activity.terminalView)
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean = false

    override fun shouldEnforceCharBasedInput(): Boolean = true

    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false

    override fun isTerminalViewSelected(): Boolean = true

    override fun copyModeChanged(copyMode: Boolean) {
        // No implementation needed
    }

    @SuppressLint("RtlHardcoded")
    override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && !session.isRunning) {
            if (!activity.isFinishing) activity.finish()
            return true
        }
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && activity.terminalView.mEmulator == null) {
            if (!activity.isFinishing) activity.finish()
            return true
        }
        return false
    }

    override fun readControlKey(): Boolean = false

    override fun readAltKey(): Boolean = false

    override fun readShiftKey(): Boolean = false

    override fun readFnKey(): Boolean = false

    override fun onLongPress(event: MotionEvent): Boolean = false

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean = false

    fun changeFontSize(increase: Boolean) {
        TerminalSettingsHelper.changeFontSize(activity, increase)
        activity.terminalView.setTextSize(TerminalSettingsHelper.getFontSize(activity))
    }

    fun setTerminalCursorBlinkerState(start: Boolean) {
        if (start) {
            if (activity.terminalView.setTerminalCursorBlinkerRate(0)) {
                activity.terminalView.setTerminalCursorBlinkerState(true, true)
            }
        } else {
            activity.terminalView.setTerminalCursorBlinkerState(false, true)
        }
    }

    fun shareSessionTranscript() {
        val session = activity.currentSession ?: return
        var transcriptText = Utilities.getTerminalSessionTranscriptText(session, linesJoined = false, trim = true) ?: return

        transcriptText = Utilities.getTruncatedCommandOutput(
            text = transcriptText,
            maxLength = Utilities.TRANSACTION_SIZE_LIMIT_IN_BYTES,
            fromEnd = false,
            onNewline = true,
            addPrefix = false
        ).trim()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, transcriptText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, activity.getString(R.string.title_share_transcript_with))
        activity.startActivity(shareIntent)
    }
}

