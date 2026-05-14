package com.namma.hasiru.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatDate(millis: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

fun daysAgo(millis: Long): Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - millis).coerceAtLeast(0)

fun scoreForStatus(statusName: String): Int = when (statusName) {
    "PLANTED" -> 25
    "SPROUTED" -> 55
    "GROWING" -> 75
    "HEALTHY" -> 95
    else -> 0
}

fun createCameraUri(context: Context): Uri {
    val file = File.createTempFile(
        "PLANTATION_${System.currentTimeMillis()}_",
        ".jpg",
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun compressImage(context: Context, source: Uri): String {
    val inputBytes = context.contentResolver.openInputStream(source)?.use { it.readBytes() }
        ?: return source.toString()
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size, bounds)
    var sample = 1
    while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2
    val bitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return source.toString()
    val file = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "hasiru_${SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())}.jpg"
    )
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 76, out)
    }
    if (!bitmap.isRecycled) bitmap.recycle()
    return Uri.fromFile(file).toString()
}

fun resolveAddress(context: Context, lat: Double, lng: Double): String = try {
    @Suppress("DEPRECATION")
    val address = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)?.firstOrNull()
    listOfNotNull(address?.subLocality, address?.locality, address?.adminArea)
        .filter { it.isNotBlank() }
        .joinToString(", ")
        .ifBlank { "Pinned location" }
} catch (_: Exception) {
    "Pinned location"
}
