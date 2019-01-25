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

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.couchbase.android.sofapix.DetailActivity
import com.couchbase.android.sofapix.LoginActivity
import com.couchbase.android.sofapix.MainActivity
import com.couchbase.android.sofapix.app.NavModule
import com.couchbase.android.sofapix.app.ViewScope
import com.couchbase.android.sofapix.auth.AuthModule
import com.couchbase.android.sofapix.db.PixStoreModule
import com.couchbase.android.sofapix.images.ImageMgrModule
import dagger.MapKey
import dagger.Subcomponent
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass


@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@ViewScope
@Subcomponent(
    modules = [
        NavModule::class,
        AuthModule::class,
        ImageMgrModule::class,
        PixStoreModule::class,
        PixVMModule::class,
        PictVMModule::class,
        LoginVMModule::class]
)
interface ViewModelFactory {
    fun inject(act: MainActivity)
    fun inject(act: DetailActivity)
    fun inject(act: LoginActivity)
}

class VMFactory @Inject constructor(
    private val providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val provider = providers[modelClass]
            ?: providers.asIterable()
                .firstOrNull { modelClass.isAssignableFrom(it.key) }
                ?.value
            ?: throw IllegalArgumentException("Unknown model class: ${modelClass}")

        @Suppress("UNCHECKED_CAST")
        return provider.get() as T
    }
}
