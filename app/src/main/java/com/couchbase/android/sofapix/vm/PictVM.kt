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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
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

class PictVM @Inject constructor(
    private val nav: Navigator,
    private val db: PixStore,
    private val imageMgr: ImageManager
) : ViewModel() {
    val pict: MutableLiveData<Pict?> = MutableLiveData()
    val image: MutableLiveData<Bitmap?> = MutableLiveData()
    var pictId: String? = null

    // should be cancelled if the MutableDatas lose all observers
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

    fun updatePict(owner: String, desc: String) {
        val bmp = image.value

        if (bmp == null) {
            db.addOrUpdatePict(pictId, owner, desc)
        } else {
            loader = imageMgr.getImageWithThumbnail(bmp).subscribe(
                { data ->
                    LOG.w(TAG, "resized image for: ${pictId}")
                    db.addOrUpdatePict(pictId, owner, desc, data.thumb, data.image)
                },
                { err ->
                    LOG.w(TAG, "failed resizing image for: ${pictId}", err)
                    db.addOrUpdatePict(pictId, owner, desc)
                })
        }
        exit()
    }

    fun deletePict() {
        val pid = pictId ?: return
        db.deletePict(pid)
        exit()
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
                LOG.d(TAG, "loaded image for : ${data.id}")
                showImage(img)
            },
            { err ->
                LOG.w(TAG, "failed fetching image for : ${data.id}", err)
                showImage(null)
            })
    }

    private fun showImage(data: Bitmap?) {
        loader = null
        image.value = data
    }

    private fun exit() {
        image.value = null
        nav.mainPage()
    }
}

@Module
interface PictVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(PictVM::class)
    fun bindViewModel(vm: PictVM): ViewModel
}
