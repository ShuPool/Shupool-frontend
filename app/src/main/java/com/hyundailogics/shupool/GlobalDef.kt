package com.hyundailogics.shupool

import android.util.Log
import com.kakaomobility.knsdk.KNCarFuel
import com.kakaomobility.knsdk.KNCarType
import java.io.File
import java.io.FileFilter
import java.lang.Boolean
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.to

internal fun KN_SAMPLE_DEBUG_MODE() = Boolean.parseBoolean("true")

internal fun KNSampleLog(aMsg: String) {
    if (KN_SAMPLE_DEBUG_MODE()) {
        Log.e("bongu[KNSample] =", aMsg)
    }
}

fun carTypeWithIdx(idx: String) = KNCarType.values().find { carType -> carType.value == idx.toInt() } ?: KNCarType.KNCarType_1

fun fuelTypeWithIdx(idx: String) = KNCarFuel.values().find { fuleType -> fuleType.value == idx.toInt()} ?: KNCarFuel.KNCarFuel_Gasoline

fun KNToDistStr(aDist: Int): String {
    return when {
        aDist < 1000 -> String.format("%dm", aDist)
        aDist < 10000 -> String.format("%d.%dkm", aDist / 1000, aDist / 100 % 10)
        else -> String.format("%dkm", aDist / 1000)
    }
}

fun KNToTimeStr(aTime: Int): String {
    val time = (aTime / 60)

    return when {
        time < 60 -> String.format("%d분", time)
        else -> String.format("%d시간 %d분", time / 60, time % 60)
    }
}

fun getDirFiles(path: String): Pair<List<String>?, List<String>?> {
    var retFileName: List<String>? = null
    var retFileDate: List<String>? = null

    val dir = File(path)
    if (dir.exists() && dir.isDirectory) {
        val files = dir.listFiles(FileFilter {
            !it.name.contains("nomedia") && it.isFile
        })
        files?.sortWith(compareBy { -it.lastModified() })
        if (!files.isNullOrEmpty()) {
            retFileName = List(files.size) { i -> files[i].name }
            retFileDate = List(files.size) { i -> "${files[i].name} (${SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault()).format(Date(files[i].lastModified()))})" }
        }
    }

    return retFileName to retFileDate
}
