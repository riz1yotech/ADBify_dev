package com.adbify.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.adbify.MainActivity

class TerminalSessionActivityClient(private val activity: MainActivity) : TerminalSessionClientBase() {

    var currentTerminalSession: TerminalSession? = null
        set(value) {
            if (value == null) return

            val current = activity.terminalView.currentSession
            if (current != null && current.mHandle == value.mHandle) {
                return
            }

            activity.terminalView.attachSession(value)
            activity.terminalView.onScreenUpdated()
            field = value
        }
        get() {
            val service = activity.terminalService ?: return null
            return service.getOrCreateTerminalSession()
        }

    fun onCreate() {}

    fun onStart() {
        if (activity.terminalService != null) {
            currentTerminalSession = currentTerminalSession
        }
    }

    fun onResume() {}

    fun onStop() {}

    override fun onTextChanged(changedSession: TerminalSession) {
        if (!activity.isVisible) return
        activity.terminalView.onScreenUpdated()
    }

    override fun onTitleChanged(updatedSession: TerminalSession) {}

    override fun onSessionFinished(finishedSession: TerminalSession) {
        activity.terminalService?.actionStopService()
        if (!activity.isFinishing) activity.finish()
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
        if (!activity.isVisible) return

        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData(null, arrayOf("text/plain"), ClipData.Item(text))
        )
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        if (!activity.isVisible) return

        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip

        if (clipData != null) {
            val paste = clipData.getItemAt(0).coerceToText(activity)
            if (!paste.isNullOrEmpty()) {
                activity.terminalView.mEmulator.paste(paste.toString())
            }
        }
    }

    override fun onBell(session: TerminalSession) {}

    override fun onColorsChanged(changedSession: TerminalSession) {}

    override fun onTerminalCursorStateChange(state: Boolean) {
        if (state && !activity.isVisible) {
            return
        }
        activity.terminalView.setTerminalCursorBlinkerState(state, false)
    }

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}

    override fun getTerminalCursorStyle(): Int {
        return TerminalEmulator.DEFAULT_TERMINAL_CURSOR_STYLE
    }
}

