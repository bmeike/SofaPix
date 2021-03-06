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
package com.couchbase.android.sofapix.time

import android.os.SystemClock
import org.threeten.bp.Instant


const val MS_PER_SEC = 1000L

var CLOCK: Clock = StandardClock
    private set(clock) {
        field = clock
    }

// Allow unit tests to control time.
fun SET_CLOCK(clock: Clock) {
    CLOCK = clock
}

interface Clock {
    fun getElapsedTime(): Long
    fun now(): Instant
}

object StandardClock : Clock {
    override fun getElapsedTime() = SystemClock.elapsedRealtime()
    override fun now(): Instant = Instant.now()
}
