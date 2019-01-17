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
import com.couchbase.android.sofapix.Navigator
import com.couchbase.android.sofapix.db.Pict
import com.couchbase.android.sofapix.db.Pix
import com.couchbase.android.sofapix.db.PixStore
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject


private const val TAG = "PixVM"

class PixVM @Inject constructor(private val nav: Navigator, private val db: PixStore) : ViewModel() {
    val pix: MutableLiveData<Pix> = MutableLiveData()

    fun fetchPix() {
        pix.value = db.fetchPix()
    }

    fun editPict(pict: Pict?) {
        nav.detailPage(pict)
    }
}

@Module
interface PixVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(PixVM::class)
    fun bindViewModel(vm: PixVM): ViewModel
}
