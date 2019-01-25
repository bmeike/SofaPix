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
package com.couchbase.android.sofapix.app

import android.annotation.SuppressLint
import android.app.Application
import com.couchbase.android.sofapix.R
import com.couchbase.android.sofapix.vm.ViewModelFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton


@Singleton
@Component(modules = [SchedulerModule::class])
interface AppFactory {
    @Component.Builder
    interface Builder {
        fun build(): AppFactory

        @BindsInstance
        fun app(sofaPix: SofaPix): Builder

        @BindsInstance
        fun accountType(@Named("accountType")accountType: String): Builder
    }

    fun vmFactory(): ViewModelFactory
}

var APP: AppFactory? = null
    private set(appFactory) {
        field = appFactory
    }

@SuppressLint("Registered")
open class SofaPix : Application() {
    override fun onCreate() {
        super.onCreate()

        APP = DaggerAppFactory.builder()
            .app(this)
            .accountType(getString(R.string.account_type))
            .build()
    }
}
