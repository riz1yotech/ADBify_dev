package com.adbify

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.adbify.app.AppBarActivity
import com.adbify.databinding.ActivityMainBinding
import com.adbify.terminal.TerminalService
import com.adbify.terminal.TerminalService.LocalBinder
import com.adbify.terminal.TerminalSession
import com.adbify.terminal.TerminalSessionActivityClient
import com.adbify.terminal.TerminalSettingsHelper.setKeepScreenOn
import com.adbify.terminal.TerminalSettingsHelper.shouldKeepScreenOn
import com.adbify.terminal.TerminalViewClient
import com.adbify.terminal.view.TerminalView
import com.adbify.utils.FileUtils
import com.adbify.utils.GetContentContract
import com.adbify.utils.MoshiUtil
import com.adbify.utils.PermissionHelper
import com.adbify.utils.Utilities
import com.adbify.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.core.util.ContextUtils
import java.io.File

class MainActivity : AppBarActivity(), ServiceConnection {
    private lateinit var binding: ActivityMainBinding

    var terminalService: TerminalService? = null
    private var terminalSessionClient: TerminalSessionActivityClient? = null
    private var terminalViewClient: TerminalViewClient? = null

    private val adbCacheDir by lazy { ContextUtils.getExternalCacheFile(this, "adb_files_cache") }

    val terminalView: TerminalView get() = binding.terminalView

    val currentSession: TerminalSession? get() = binding.terminalView.currentSession

    @JvmField
    var isVisible = false
    private var isOnResumeAfterOnCreate = false
    private var isActivityRecreated = false
    private var isInvalidState = false

    private lateinit var permissionHelper: PermissionHelper

    private var filePicker = registerForActivityResult(GetContentContract()) {
        handleFileUri(this, it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isOnResumeAfterOnCreate = true
        if (savedInstanceState != null) isActivityRecreated = savedInstanceState.getBoolean(ARG_ACTIVITY_RECREATED, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getFullStoragePermission()
        setTerminalViewAndClients()
        setupCommandButtons()
        initTerminalServiceConnection()

        Utilities.adbPortTest()
    }

    private fun setupCommandButtons() {
        binding.btnCommands.setOnClickListener {
            Dialogs.showFloatingOptionsMenu(it) { command ->
                binding.terminalView.currentSession?.emulator?.paste(command)
            }
        }
        binding.btnRun.setOnClickListener { binding.terminalView.currentSession?.write("\r") }

        binding.btnLeft.setOnClickListener { binding.terminalView.handleKeyCode(KEYCODE_DPAD_LEFT, 0) }
        binding.btnRight.setOnClickListener { binding.terminalView.handleKeyCode(KEYCODE_DPAD_RIGHT, 0) }
        binding.btnUp.setOnClickListener { binding.terminalView.handleKeyCode(KEYCODE_DPAD_UP, 0) }
        binding.btnDown.setOnClickListener { binding.terminalView.handleKeyCode(KEYCODE_DPAD_DOWN, 0) }
    }

    private fun initTerminalServiceConnection() {
        try {
            val serviceIntent = Intent(this, TerminalService::class.java)
            startService(serviceIntent)
            if (!bindService(serviceIntent, this, 0))
                throw RuntimeException("bindService() failed")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(R.string.terminal_service_start_error)
            isInvalidState = true
            return
        }
    }

    private fun getFullStoragePermission() {
        permissionHelper = PermissionHelper(this)
        permissionHelper.registerPermissionLaunchers()
        permissionHelper.requestStoragePermissions()
    }

    private fun setTerminalViewAndClients() {
        TerminalViewClient(this).apply {
            binding.terminalView.setTerminalViewClient(this)
            binding.terminalView.post { binding.terminalView.setTypeface(Typeface.MONOSPACE) }
            terminalViewClient = this
            terminalViewClient?.onCreate()
        }

        terminalSessionClient = TerminalSessionActivityClient(this)
        terminalSessionClient?.onCreate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ARG_ACTIVITY_RECREATED, true)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        terminalService = (service as LocalBinder).service
        if (terminalService != null) {
            terminalService!!.setTerminalSessionClient(terminalSessionClient)
            terminalSessionClient?.currentTerminalSession = terminalSessionClient?.currentTerminalSession
        } else isInvalidState = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if (!isFinishing) finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val keepScreenOn = menu.findItem(R.id.action_keep_screen_on)
        keepScreenOn.isChecked = shouldKeepScreenOn(this@MainActivity)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_attachment -> {
                pickAFile()
                return true
            }

            R.id.action_share_transcript -> {
                terminalViewClient?.shareSessionTranscript()
                return true
            }

            R.id.action_kill -> {
                currentSession?.let { Dialogs.showKillSessionDialog(this, it) }
                return true
            }

            R.id.action_keep_screen_on -> {
                toggleKeepScreenOn()
                item.isChecked = shouldKeepScreenOn(this@MainActivity)
                return true
            }

            R.id.action_about -> {
                Dialogs.showAboutDialog(this)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isInvalidState) return
        isVisible = true
        terminalSessionClient?.onStart()
        terminalViewClient?.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (isInvalidState) return
        terminalSessionClient?.onResume()
        terminalViewClient?.onResume()
        isOnResumeAfterOnCreate = false
    }

    override fun onStop() {
        super.onStop()
        if (isInvalidState) return
        isVisible = false
        terminalSessionClient?.onStop()
        terminalViewClient?.onStop()
    }

    override fun onDestroy() {
        if (isInvalidState) return
        if (terminalService != null) {
            terminalService!!.unsetTerminalSessionClient()
            terminalService = null
        }
        try {
            unbindService(this)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        moveTaskToBack(true)
        super.onBackPressed()
    }

    private fun toggleKeepScreenOn() {
        if (binding.terminalView.keepScreenOn) {
            binding.terminalView.keepScreenOn = false
            setKeepScreenOn(this, false)
        } else {
            binding.terminalView.keepScreenOn = true
            setKeepScreenOn(this, true)
        }
    }


    private fun pickAFile() {
        if (permissionHelper.hasStoragePermissions()) filePicker.launch("*/*")
        else permissionHelper.requestStoragePermissions()
    }

    private fun handleFileUri(context: Context, uri: Uri?) {
        if (uri == null) {
            showToast(R.string.file_attach_failed)
            return
        }

        fun addToTerminal(line: String) = currentSession?.emulator?.paste(Utilities.quote(line))

        val path = FileUtils.getPath(this@MainActivity, uri)
        if (path != null && File(path).canRead()) {
            val finalPath = path.removePrefix("file:")
            addToTerminal(finalPath)
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                if (adbCacheDir.exists()) adbCacheDir.deleteRecursively()
                if (!adbCacheDir.exists()) adbCacheDir.mkdirs()
                FileUtils.copyUriToPath(context, uri, adbCacheDir.absolutePath)?.let {
                    withContext(Dispatchers.Main) { addToTerminal(it) }
                }
            }
        }
    }

    companion object {
        private const val ARG_ACTIVITY_RECREATED = "activity_recreated"

        fun newInstance(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }
    }
}