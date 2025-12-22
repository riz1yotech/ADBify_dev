package com.adbify.utils

import com.adbify.terminal.TerminalSession
import java.util.Locale

object Utilities {
    const val TRANSACTION_SIZE_LIMIT_IN_BYTES = 100 * 1024 // 100KB
    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

    fun quote(s: String): String {
        return "'${s.replace("'", "'\\''")}'"
    }

    fun getTruncatedCommandOutput(
        text: String,
        maxLength: Int,
        fromEnd: Boolean,
        onNewline: Boolean,
        addPrefix: Boolean
    ): String {
        //if (text == null) return null

        val prefix = "(truncated) "
        val adjustedMaxLength = if (addPrefix) maxLength - prefix.length else maxLength

        if (adjustedMaxLength < 0 || text.length < adjustedMaxLength) return text

        val truncatedText = if (fromEnd) {
            text.substring(0, adjustedMaxLength)
        } else {
            var cutOffIndex = text.length - adjustedMaxLength
            if (onNewline) {
                val nextNewlineIndex = text.indexOf('\n', cutOffIndex)
                if (nextNewlineIndex != -1 && nextNewlineIndex != text.length - 1) {
                    cutOffIndex = nextNewlineIndex + 1
                }
            }
            text.substring(cutOffIndex)
        }

        return if (addPrefix) prefix + truncatedText else truncatedText
    }

    fun getTerminalSessionTranscriptText(
        terminalSession: TerminalSession?,
        linesJoined: Boolean,
        trim: Boolean
    ): String? {
        val terminalEmulator = terminalSession?.emulator ?: return null
        val terminalBuffer = terminalEmulator.screen ?: return null

        var transcriptText = if (linesJoined) {
            terminalBuffer.transcriptTextWithFullLinesJoined
        } else {
            terminalBuffer.transcriptTextWithoutJoinedLines
        }

        if (trim) {
            transcriptText = transcriptText.trim()
        }

        return transcriptText
    }

    fun capitalize(string: String): String {
        return string.substring(0, 1).uppercase(Locale.US) + string.substring(1)
    }

    fun replaceSubStringsInStringArrayItems(
        array: Array<String>?,
        find: String,
        replace: String
    ) {
        if (array.isNullOrEmpty()) return

        for (i in array.indices) {
            array[i] = array[i].replace(find, replace)
        }
    }

    fun bytesToHex(bytes: ByteArray?): String {
        if (bytes == null) return ""

        val hexChars = CharArray(bytes.size * 2)
        bytes.forEachIndexed { j, byte ->
            val v = byte.toInt() and 0xFF
            hexChars[j * 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }

    fun hexToBytes(hex: String?): ByteArray? {
        if (hex == null) return null

        val len = hex.length
        val data = ByteArray(len / 2)

        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) +
                          Character.digit(hex[i + 1], 16)).toByte()
        }

        return data
    }
}

