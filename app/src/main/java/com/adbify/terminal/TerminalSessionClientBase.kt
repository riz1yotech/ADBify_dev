package com.adbify.terminal

open class TerminalSessionClientBase : TerminalSessionClient {

    override fun onTextChanged(changedSession: TerminalSession) {}

    override fun onTitleChanged(updatedSession: TerminalSession) {}

    override fun onSessionFinished(finishedSession: TerminalSession) {}

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {}

    override fun onPasteTextFromClipboard(session: TerminalSession?) {}

    override fun onBell(session: TerminalSession) {}

    override fun onColorsChanged(changedSession: TerminalSession) {}

    override fun onTerminalCursorStateChange(state: Boolean) {}

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}

    override fun getTerminalCursorStyle(): Int? = null

    override fun logError(tag: String, message: String) {}

    override fun logWarn(tag: String, message: String) {}

    override fun logInfo(tag: String, message: String) {}

    override fun logDebug(tag: String, message: String) {}

    override fun logVerbose(tag: String, message: String) {}

    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {}

    override fun logStackTrace(tag: String, e: Exception) {}
}

