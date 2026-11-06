package com.example.cloudalbum.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

data class CompressedImage(val bytes: ByteArray, val width: Int, val height: Int)

object ImageCompressor {

    /** Gọi trên Dispatchers.Default — decode bitmap rất nặng. */
    fun compress(context: Context, uri: Uri, maxSize: Int = 2048, quality: Int = 85): CompressedImage {
        val resolver = context.contentResolver

        // 1. Đọc kích thước gốc
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val srcW = bounds.outWidth
        val srcH = bounds.outHeight
        require(srcW > 0 && srcH > 0) { "Không đọc được ảnh (định dạng không hỗ trợ?)" }

        // 2. Decode bản thu nhỏ thô bằng inSampleSize (luỹ thừa của 2)
        var sample = 1
        while (max(srcW, srcH) / (sample * 2) >= maxSize) sample *= 2
        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("Không decode được ảnh")

        // 3. Góc xoay theo EXIF
        val rotation = resolver.openInputStream(uri)?.use { input ->
            when (ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        // 4. Thu nhỏ chính xác + xoay trong một lần
        val scale = min(1f, maxSize.toFloat() / max(bitmap.width, bitmap.height))
        val matrix = Matrix().apply {
            if (scale < 1f) postScale(scale, scale)
            if (rotation != 0f) postRotate(rotation)
        }
        if (!matrix.isIdentity) {
            val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (transformed !== bitmap) bitmap.recycle()
            bitmap = transformed
        }

        // 5. Nén JPEG
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        val result = CompressedImage(out.toByteArray(), bitmap.width, bitmap.height)
        bitmap.recycle()
        return result
    }
}