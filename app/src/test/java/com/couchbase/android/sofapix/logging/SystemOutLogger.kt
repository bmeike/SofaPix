//
//
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


object SystemOutLogger : Logger {
    override fun e(tag: String, message: String) = print("E", tag, message)

    override fun e(tag: String, message: String, ex: Throwable) {
        print("E", tag, message)
        ex.printStackTrace()
    }

    override fun w(tag: String, message: String) = print("W", tag, message)

    override fun w(tag: String, message: String, ex: Throwable) {
        print("W", tag, message)
        ex.printStackTrace()
    }

    override fun i(tag: String, message: String) = print("I", tag, message)

    override fun i(tag: String, message: String, ex: Throwable) {
        print("I", tag, message)
        ex.printStackTrace()
    }

    override fun d(tag: String, message: String) = print("D", tag, message)

    fun print(sev: String, tag: String, message: String) = println("$sev: @$tag $message")
}
