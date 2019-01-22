//
// Copyright (c) 2019 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.android.sofapix.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.couchbase.android.sofapix.app.SofaPix
import dagger.Binds
import dagger.Module
import io.reactivex.Scheduler
import io.reactivex.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import kotlinx.coroutines.rx2.rxSingle
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


private const val THUMB_SIZE = 128F

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun Bitmap.fitToSquare(size: Float): Bitmap {
    val h = this.height.toFloat()
    val w = this.width.toFloat()

    val scale = size / Math.max(h, w)

    return if (scale >= 1F) {
        Bitmap.createBitmap(this)
    } else {
        Bitmap.createScaledBitmap(this, Math.round(w * scale), Math.round(h * scale), true)
    }
}

class ImageWithThumbnail(
    val image: ByteArray? = null,
    val thumb: ByteArray? = null
)

interface ImageManager {
    fun fetchImageFromUri(imageUri: Uri): Single<Bitmap>
    fun getImageFromByteArray(image: ByteArray): Single<Bitmap>
    fun getImageWithThumbnail(image: Bitmap): Single<ImageWithThumbnail>
}

@Singleton
class BitmapImageManager @Inject constructor(
    private val app: SofaPix,
    @Named("worker") private val workScheduler: Scheduler,
    @Named("main") private val mainScheduler: Scheduler
) : ImageManager {
    private val workDispatcher = workScheduler.asCoroutineDispatcher()

    override fun fetchImageFromUri(imageUri: Uri): Single<Bitmap> = GlobalScope.rxSingle(workDispatcher) {
        return@rxSingle BitmapFactory.decodeStream(app.contentResolver.openInputStream(imageUri))
    }.observeOn(mainScheduler)

    override fun getImageFromByteArray(image: ByteArray): Single<Bitmap> = GlobalScope.rxSingle(workDispatcher) {
        return@rxSingle BitmapFactory.decodeByteArray(image, 0, image.size)
    }.observeOn(mainScheduler)

    override fun getImageWithThumbnail(image: Bitmap): Single<ImageWithThumbnail> =
        GlobalScope.rxSingle(workDispatcher) {
            return@rxSingle ImageWithThumbnail(image.fitToSquare(THUMB_SIZE).toByteArray(), image.toByteArray())
        }.observeOn(mainScheduler)
}

@Module
interface ImageMgrModule {
    @Binds
    fun bindsImageManager(store: BitmapImageManager): ImageManager
}
