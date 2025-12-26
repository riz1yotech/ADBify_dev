package com.adbify

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import com.adbify.databinding.DialogAboutBinding
import com.adbify.terminal.TerminalSession
import com.adbify.utils.AppIconCache
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rikka.html.text.toHtml


/* created by @Riz1 on 26/12/2025 */

object Dialogs {
    fun showAboutDialog(context: Context) {
        //val binding = DialogAboutBinding.inflate(layoutInflater, null, false)
        val binding = DialogAboutBinding.inflate(android.view.LayoutInflater.from(context), null, false)
        binding.desInfo.movementMethod = LinkMovementMethod.getInstance()
        binding.desInfo.text = context.getString(
            R.string.about_view_source_code,
            "<b><a href=\"https://github.com/RohitVerma882/Adbify\">GitHub</a></b>"
        ).toHtml()
        binding.icon.setImageBitmap(
            AppIconCache.getOrLoadBitmap(
                context,
                context.applicationInfo,
                android.os.Process.myUid() / 100000,
                context.resources.getDimensionPixelOffset(R.dimen.default_app_icon_size)
            )
        )
        binding.versionName.text = context.getString(R.string.app_version)
        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .show()
    }

    fun showKillSessionDialog(context: Context, session: TerminalSession) {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.title_confirm_kill_process)
            .setPositiveButton(R.string.yes) { _, _ ->
                session.finishIfRunning()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
    }

    fun showFloatingOptionsMenu(anchorView: View, action: (command: String) -> Unit) {
        val commands = listOf(
            "adb devices",
            "adb kill-server",
            "adb start-server",
            "adb connect localhost:5555",
            "adb reboot",
            "adb reboot bootloader",
            "adb reboot recovery",
            "adb shell ",
        )

        val popup = androidx.appcompat.widget.PopupMenu(anchorView.context, anchorView)
        commands.forEachIndexed { index, command ->
            popup.menu.add(0, index, index, "${index+1}. $command")
        }

        popup.setOnMenuItemClickListener { menuItem ->
            action(commands[menuItem.itemId])
            true
        }

        popup.show()
    }
}