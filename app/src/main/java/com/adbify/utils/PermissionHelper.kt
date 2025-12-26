package com.adbify.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/**
 * Utility class to handle storage permissions for Android devices
 * Supports both Android 11+ (MANAGE_EXTERNAL_STORAGE) and older versions
 */
class PermissionHelper(private val activity: AppCompatActivity) {

    private var manageStoragePermissionLauncher: ActivityResultLauncher<Intent>? = null
    private var storagePermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    /**
     * Initialize permission launchers
     * Must be called before onCreate() completes
     */
    fun registerPermissionLaunchers(
        onStorageGranted: () -> Unit = {},
        onStorageDenied: () -> Unit = {}
    ) {
        // Permission launcher for Android 11+
        manageStoragePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(activity, "Storage permission granted!", Toast.LENGTH_SHORT).show()
                    onStorageGranted()
                } else {
                    Toast.makeText(activity, "Storage permission denied", Toast.LENGTH_SHORT).show()
                    onStorageDenied()
                }
            }
        }

        // Permission launcher for Android 10 and below
        storagePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                Toast.makeText(activity, "Storage permissions granted!", Toast.LENGTH_SHORT).show()
                onStorageGranted()
            } else {
                Toast.makeText(activity, "Storage permissions denied", Toast.LENGTH_SHORT).show()
                onStorageDenied()
            }
        }
    }

    /**
     * Check if storage permissions are granted
     */
    fun hasStoragePermissions() = PermissionHelper.hasStoragePermissions(activity)

    /**
     * Request storage permissions based on Android version
     */
    fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Request MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = "package:${activity.packageName}".toUri()
                    manageStoragePermissionLauncher?.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStoragePermissionLauncher?.launch(intent)
                }
            }
        } else {
            // Android 10 and below - Request READ/WRITE_EXTERNAL_STORAGE
            val permissionsNeeded = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permissionsNeeded.isNotEmpty()) storagePermissionLauncher?.launch(permissionsNeeded.toTypedArray())

        }
    }

    companion object {
        /**
         * Static method to check storage permissions without instance
         */
        fun hasStoragePermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ - Check MANAGE_EXTERNAL_STORAGE
                Environment.isExternalStorageManager()
            } else {
                // Android 10 and below - Check READ/WRITE_EXTERNAL_STORAGE
                val readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                val writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                readPermission && writePermission
            }
        }
    }
}

