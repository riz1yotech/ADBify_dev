package com.adbify.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import java.io.*

object FileUtils {

    fun copyUriToPath(context: Context, uri: Uri, dest: String): String? {
        var filename = "${getFileBaseName(uri.path)}.${getFileExtension(uri.path)}"

        try {
            val docFile = DocumentFile.fromSingleUri(context, uri)
            docFile?.name?.let { filename = it }
        } catch (e: Exception) {
            // ignore
        }

        makeDir(dest)
        val destFile = File(dest, filename)
        createNewFile(destFile.absolutePath)

        var bos: BufferedOutputStream? = null
        var bis: BufferedInputStream? = null
        return try {
            bos = BufferedOutputStream(FileOutputStream(destFile))
            bis = BufferedInputStream(context.contentResolver.openInputStream(uri))
            val buffer = ByteArray(65536)
            var numBytes: Int
            while (bis.read(buffer).also { numBytes = it } != -1) {
                bos.write(buffer, 0, numBytes)
            }
            destFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            AndroidUtilities.closeQuietly(bos)
            AndroidUtilities.closeQuietly(bis)
        }
    }

    fun relativePath(file: String, dir: String): String {
        return File(dir).toURI().relativize(File(file).toURI()).path
    }

    fun relativePath(file: File, dir: File): String {
        return dir.toURI().relativize(file.toURI()).path
    }

    fun readFile(path: String): String {
        if (!isExistFile(path) || !isFile(path)) {
            return ""
        }

        val file = File(path)
        return try {
            file.readText()
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    fun createNewFile(path: String) {
        val lastSep = path.lastIndexOf(File.separator)
        if (lastSep > 0) {
            val dirPath = path.substring(0, lastSep)
            makeDir(dirPath)
        }
        val file = File(path)
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun copyFile(source: InputStream, destPath: String) {
        createNewFile(destPath)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(destPath, false)
            val buff = ByteArray(1024)
            var length: Int
            while (source.read(buff).also { length = it } > 0) {
                fos.write(buff, 0, length)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun copyFile(sourcePath: String, destPath: String) {
        if (!isExistFile(sourcePath)) return

        createNewFile(destPath)
        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            fis = FileInputStream(sourcePath)
            fos = FileOutputStream(destPath, false)
            val buff = ByteArray(1024)
            var length: Int
            while (fis.read(buff).also { length = it } > 0) {
                fos.write(buff, 0, length)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fis?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (!file.exists()) return

        if (file.isFile) {
            file.delete()
            return
        }

        file.listFiles()?.forEach { subFile ->
            if (subFile.isDirectory) {
                deleteFile(subFile.absolutePath)
            }
            if (subFile.isFile) {
                subFile.delete()
            }
        }
        file.delete()
    }

    fun getFileExtension(filename: String?): String? {
        if (filename == null) return null

        val name = File(filename).name
        val extensionPosition = name.lastIndexOf('.')
        return if (extensionPosition < 0) {
            ""
        } else {
            name.substring(extensionPosition + 1)
        }
    }

    fun getFileBaseName(filename: String?): String? {
        if (filename == null) return null

        val name = File(filename).name
        val extensionPosition = name.lastIndexOf('.')
        return if (extensionPosition < 0) {
            name
        } else {
            name.substring(0, extensionPosition)
        }
    }

    fun canReadFile(path: String): Boolean {
        return File(path).canRead()
    }

    fun canWriteFile(path: String): Boolean {
        return File(path).canWrite()
    }

    fun canExecuteFile(path: String): Boolean {
        return File(path).canExecute()
    }

    fun isExistFile(path: String): Boolean {
        return File(path).exists()
    }

    fun makeDir(path: String) {
        if (!isExistFile(path)) {
            File(path).mkdirs()
        }
    }

    fun listDir(path: String, list: ArrayList<String>?) {
        val dir = File(path)
        if (!dir.exists() || dir.isFile) return

        val listFiles = dir.listFiles()
        if (listFiles == null || listFiles.isEmpty()) return
        if (list == null) return

        list.clear()
        listFiles.forEach { file ->
            list.add(file.absolutePath)
        }
    }

    fun isDirectory(path: String): Boolean {
        if (!isExistFile(path)) return false
        return File(path).isDirectory
    }

    fun isFile(path: String): Boolean {
        if (!isExistFile(path)) return false
        return File(path).isFile
    }

    fun getFileLength(path: String): Long {
        if (!isExistFile(path)) return 0
        return File(path).length()
    }

    fun getPublicDir(type: String): String {
        return Environment.getExternalStoragePublicDirectory(type).absolutePath
    }

    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                when {
                    isExternalStorageDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":")
                        val type = split[0]
                        if ("primary".equals(type, ignoreCase = true)) {
                            return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                        }
                    }

                    isDownloadsDocument(uri) -> {
                        val id = DocumentsContract.getDocumentId(uri)
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            id.toLong()
                        )
                        return getDataColumn(context, contentUri, null, null)
                    }

                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":")
                        val type = split[0]

                        val contentUri = when (type) {
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            else -> null
                        }

                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        } catch (ignored: Exception) {
            // ignore
        }
        return null
    }

    fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        if (uri == null) return null

        val column = "_data"
        val projection = arrayOf(column)

        return try {
            @SuppressLint("Recycle")
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                val value = cursor.getString(columnIndex)
                if (value.startsWith("content://") ||
                    (!value.startsWith("/") && !value.startsWith("file://"))
                ) {
                    return null
                }
                value
            } else {
                null
            }
        } catch (ignore: Exception) {
            null
        }
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}

