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
import com.couchbase.android.sofapix.app.Navigator
import com.couchbase.android.sofapix.db.PixStore
import com.couchbase.android.sofapix.model.Pict
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named


class PictVM @Inject constructor(
    @Named("main") private val mainScheduler: Scheduler,
    private val nav: Navigator,
    private val db: PixStore
) : ViewModel() {
    val pict: MutableLiveData<Pict?> = MutableLiveData()

    fun fetchPict(pictId: String?) {
        if (pictId == null) {
            pict.value = null
            return
        }

        db.fetchPict(pictId)
            .observeOn(mainScheduler)
            .subscribe(
                { data -> pict.value = data },
                { },
                { pict.value = null } )
    }

    fun updatePict(pictId: String?, owner: String, desc: String) {
        db.addOrUpdatePict(pictId, owner, desc)
        nav.mainPage()
    }

    fun deletePict(pictId: String?) {
        pictId ?: return
        db.deletePict(pictId)
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
