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
package com.couchbase.android.sofapix.vm

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Bitmap
import android.net.Uri
import com.couchbase.android.sofapix.app.Navigator
import com.couchbase.android.sofapix.db.PixStore
import com.couchbase.android.sofapix.images.ImageManager
import com.couchbase.android.sofapix.logging.LOG
import com.couchbase.android.sofapix.model.Pict
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.reactivex.disposables.Disposable
import javax.inject.Inject


private const val TAG = "PICTVM"

@Module
interface PictVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(PictVM::class)
    fun bindViewModel(vm: PictVM): ViewModel
}

class PictVM @Inject constructor(
    private val nav: Navigator,
    private val db: PixStore,
    private val imageMgr: ImageManager
) : ViewModel() {
    val pict: MutableLiveData<Pict?> = MutableLiveData()
    val image: MutableLiveData<Bitmap?> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    var pictId: String? = null

    private var loader: Disposable? = null

    fun fetchPict() {
        val pid = pictId ?: return

        loader = db.fetchPict(pid).subscribe(
            { data ->
                LOG.d(TAG, "fetched pict: ${pictId}")
                showPict(data)
            },
            { err ->
                LOG.w(TAG, "failed fetching pict: ${pictId}", err)
                showPict(null)
            })
    }

    fun fetchImageFromUri(uri: Uri) {
        loader = imageMgr.fetchImageFromUri(uri).subscribe(
            { data ->
                LOG.w(TAG, "fetched image from: ${uri}")
                showImage(data)
            },
            { err ->
                LOG.w(TAG, "failed fetching image from: ${uri}", err)
                showImage(null)
            })
    }

    @SuppressLint("CheckResult")
    fun deletePict() {
        val pid = pictId ?: return
        loader = db.deletePict(pid).subscribe { exit() }
    }

    fun updatePict(owner: String, desc: String) {
        loading.value = true

        val bmp = image.value

        if (bmp == null) {
            addOrUpdatePict(pictId, owner, desc)
        } else {
            loader = imageMgr.getImageWithThumbnail(bmp).subscribe(
                { data ->
                    LOG.w(TAG, "resized image for: ${pictId}")
                    addOrUpdatePict(pictId, owner, desc, data.thumb, data.image)
                },
                { err ->
                    LOG.w(TAG, "failed resizing image for: ${pictId}", err)
                    addOrUpdatePict(pictId, owner, desc)
                })
        }
    }

    fun exit() {
        loader?.dispose()
        loader = null

        image.value = null

        nav.mainPage()
    }

    private fun showPict(data: Pict?) {
        loader = null

        pict.value = data

        if (image.value != null) {
            showImage(image.value)
            return
        }

        val image = data?.image ?: return
        loader = imageMgr.getImageFromByteArray(image).subscribe(
            { img ->
                LOG.d(TAG, "loaded image for: ${data.id}")
                showImage(img)
            },
            { err ->
                LOG.w(TAG, "failed loading image for: ${data.id}", err)
                showImage(null)
            })
    }

    private fun showImage(data: Bitmap?) {
        loader = null
        image.value = data
    }

    private fun addOrUpdatePict(
        pictId: String?,
        owner: String,
        desc: String,
        thumb: ByteArray? = null,
        image: ByteArray? = null
    ) {
        loader = db.addOrUpdatePict(pictId, owner, desc, thumb, image).subscribe {
            loader = null
            loading.value = false
        }
    }
}
