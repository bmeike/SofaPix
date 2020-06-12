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

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton


@Module
object SchedulerModule {
    private val main = AndroidSchedulers.mainThread()
    private val database = Schedulers.from(Executors.newSingleThreadExecutor())
    private val worker = Schedulers.single()

    @Provides
    @Singleton
    @Named("main")
    fun main(): Scheduler = main

    @Provides
    @Singleton
    @Named("database")
    fun database(): Scheduler = database

    @Provides
    @Singleton
    @Named("worker")
    fun worker(): Scheduler = worker
}
