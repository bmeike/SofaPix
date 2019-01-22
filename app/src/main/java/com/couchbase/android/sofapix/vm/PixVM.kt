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
import com.couchbase.android.sofapix.app.Navigator
import com.couchbase.android.sofapix.db.PixStore
import com.couchbase.android.sofapix.images.ImageManager
import com.couchbase.android.sofapix.logging.LOG
import com.couchbase.android.sofapix.model.Pict
import com.couchbase.android.sofapix.model.Pix
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javax.inject.Inject


private const val TAG = "PixVM"

@Module
interface PixVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(PixVM::class)
    fun bindViewModel(vm: PixVM): ViewModel
}

class PixVM @Inject constructor(
    private val nav: Navigator,
    private val imageMgr: ImageManager,
    private val db: PixStore
) : ViewModel() {
    val pix: MutableLiveData<Pix> = MutableLiveData()

    // should be cancelled if the MutableData loses all observers
    private var loader: Disposable? = null

    fun fetchPix() {
        loader = db.fetchPix().subscribe(
            { data ->
                LOG.d(TAG, "fetched pix: ${data.size}")
                showPix(data)
            },
            { e ->
                LOG.e(TAG, "failed fetching pix", e)
                showPix(null)
            })
    }

    fun loadImage(image: ByteArray): Single<Bitmap> = imageMgr.getImageFromByteArray(image)

    fun editPict(pict: Pict?) {
        nav.detailPage(pict)
    }

    private fun showPix(data: Pix?) {
        loader = null
        pix.value = data
    }
}
