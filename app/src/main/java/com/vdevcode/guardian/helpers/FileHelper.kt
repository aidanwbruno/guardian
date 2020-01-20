package com.vdevcode.guardion.helpers

import android.content.Context

import android.widget.ImageView
import com.vdevcode.guardian.helpers.Guardian
import com.vdevcode.guardian.helpers.Helper

import java.io.File

object FileHelper {

    const val HISTORY_PATH = "histories"
    const val FILE_PROVIDER = "com.vdevcode.guardian.fileprovider"
    //const val EPISODE_PATH = "s% histories"

    fun createFilesDir(context: Context, dir: String): File? {
        val dir = File(context.filesDir, dir)
        if (!dir.exists()) {
            dir.mkdirs();
            Helper.LogE("Diretorio criado com sucess: $dir")
        }
        return dir
    }

    fun getFileName(fullPath: String?): String {
        fullPath?.let {
            val idex = fullPath.lastIndexOf("/")
            val name = fullPath.substring(idex + 1, fullPath.length)
            // Helper.LogE("Diretorio criado com sucess: $name")
            return name
        }
        return ""
    }

    fun getFilePath(fullPath: String): String {
        val idex = fullPath.lastIndexOf("/")
        val path = fullPath.substring(0, idex + 1)
        // Helper.LogE("Diretorio criado com sucess: $path")
        return path
    }

    fun getFullFilePath(context: Context, filePath: String): String {
        return "${context.filesDir}${File.separator + filePath}"
    }

    fun fileExists(context: Context, fullPath: String): Boolean {
        if (fullPath.isBlank()) return false
        val file = File(getFullFilePath(context, fullPath))
        val exists = file.exists()
        return exists
    }

    fun deleteFile(file: File) {
        if (file.exists()) {
            val del = file.delete()
            if (del) {
                Guardian.toast("Arquivo deletado")
            }
        }
    }

    fun createTempFile(fileName: String) {
        //File.createTempFile()
    }


}
