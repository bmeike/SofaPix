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
package com.couchbase.android.sofapix.logging


var LOG: Logger = NullLogger
    private set(logger) {
        field = logger
    }

fun SET_LOGGER(logger: Logger) {
    LOG = logger
}

interface Logger {
    fun e(tag: String, message: String)
    fun e(tag: String, message: String, ex: Throwable)
    fun w(tag: String, message: String)
    fun w(tag: String, message: String, ex: Throwable)
    fun i(tag: String, message: String)
    fun i(tag: String, message: String, ex: Throwable)
    fun d(tag: String, message: String)
}

object NullLogger : Logger {
    override fun e(tag: String, message: String) = Unit
    override fun e(tag: String, message: String, ex: Throwable) = Unit
    override fun w(tag: String, message: String) = Unit
    override fun w(tag: String, message: String, ex: Throwable) = Unit
    override fun i(tag: String, message: String) = Unit
    override fun i(tag: String, message: String, ex: Throwable) = Unit
    override fun d(tag: String, message: String) = Unit
}
