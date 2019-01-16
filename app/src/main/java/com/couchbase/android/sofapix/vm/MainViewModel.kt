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
import com.couchbase.android.sofapix.db.Pict
import com.couchbase.android.sofapix.db.Pix
import com.couchbase.android.sofapix.logging.LOG
import com.couchbase.android.sofapix.time.CLOCK
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject


private const val TAG = "MainVM"

class MainViewModel @Inject constructor() : ViewModel() {
    val pix: MutableLiveData<Pix> = MutableLiveData()

    fun addPicture() {
        LOG.i(TAG, "add a picture")
    }

    fun getPix() {
        pix.value = listOf(Pict("louvre", "mona lisa", CLOCK.now(), null))
    }
}

@Module
interface MainViewModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindViewModel(vm: MainViewModel): ViewModel
}
