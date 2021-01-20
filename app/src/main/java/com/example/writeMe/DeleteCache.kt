package com.example.writeMe

import android.app.Application
import java.io.File

object DeleteCache {
    fun deleteCache(application: Application) {
        val cacheDir = application.cacheDir
        deleteDir(cacheDir)
    }

    private fun deleteDir(cacheDir: File?): Boolean {
        if (cacheDir != null && cacheDir.isDirectory) {
            val children = cacheDir.list()
            children.forEach {
                val success = deleteDir(File(cacheDir,it))
                if (!success) return false
            }
            return cacheDir.delete()
        }else if (cacheDir != null && cacheDir.isFile) {
            return cacheDir.delete()
        }else {
            return false
        }
    }
}