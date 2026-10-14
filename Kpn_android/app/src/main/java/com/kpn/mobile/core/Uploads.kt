package com.kpn.mobile.core

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.ByteArrayInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.io.ByteArrayOutputStream

internal suspend fun uploadPart(context: Context, uri: Uri, field: String, photo: Boolean = false,maxBytes:Int=20*1024*1024): MultipartBody.Part = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val bytes = resolver.openInputStream(uri)?.use { input ->
        val out = ByteArrayOutputStream(); val buffer = ByteArray(8192)
        while (true) { val read = input.read(buffer); if (read < 0) break; out.write(buffer, 0, read); require(out.size() <= maxBytes) { "Choose a file smaller than ${maxBytes/(1024*1024)} MB." } }
        out.toByteArray()
    } ?: error("Unable to read the selected file.")
    var content = bytes
    var type = resolver.getType(uri) ?: "application/octet-stream"
    val displayName=runCatching{resolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{cursor->if(cursor.moveToFirst())cursor.getString(0) else null}}.getOrNull()
    var filename=displayName?.substringAfterLast('/')?.substringAfterLast('\\')?.replace(Regex("[^a-zA-Z0-9._-]"),"_")?.take(100)?.takeIf{it.isNotBlank()} ?: "attachment.${MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: "bin"}"
    if (photo) {
        require(type.startsWith("image/")) { "Choose an image for your profile photo." }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }; BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Unable to read this image." }
        var sample = 1; while (maxOf(bounds.outWidth,bounds.outHeight)/sample > 1600) sample *= 2
        var bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size,BitmapFactory.Options().apply { inSampleSize = sample }) ?: error("Unable to decode this image.")
        val orientation=runCatching{ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)}.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val transform=Matrix().apply{when(orientation){ExifInterface.ORIENTATION_FLIP_HORIZONTAL->setScale(-1f,1f);ExifInterface.ORIENTATION_ROTATE_180->setRotate(180f);ExifInterface.ORIENTATION_FLIP_VERTICAL->setScale(1f,-1f);ExifInterface.ORIENTATION_TRANSPOSE->{setRotate(90f);postScale(-1f,1f)};ExifInterface.ORIENTATION_ROTATE_90->setRotate(90f);ExifInterface.ORIENTATION_TRANSVERSE->{setRotate(-90f);postScale(-1f,1f)};ExifInterface.ORIENTATION_ROTATE_270->setRotate(-90f)}}
        if(!transform.isIdentity){val rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,transform,true);if(rotated!==bitmap)bitmap.recycle();bitmap=rotated}
        var quality = 90
        do { val out = ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG,quality,out); content = out.toByteArray(); quality -= 10 } while (content.size > 400 * 1024 && quality >= 30)
        bitmap.recycle(); require(content.size <= 400 * 1024) { "Choose a smaller photo. The KPN limit is 400 KB." }
        type = "image/jpeg"; filename = "profile.jpg"
    }
    MultipartBody.Part.createFormData(field, filename, content.toRequestBody(type.toMediaType()))
}
