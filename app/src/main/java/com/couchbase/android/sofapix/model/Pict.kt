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
package com.couchbase.android.sofapix.model

import com.couchbase.android.sofapix.time.CLOCK


typealias Pix = List<Pict>

data class Pict(
    val id: String? = null,
    val owner: String = "",
    val description: String = "",
    val updated: Long = CLOCK.now().epochSecond,
    val image: ByteArray? = null,
    val thumb: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pict

        if (id != other.id) return false
        if (owner != other.owner) return false
        if (description != other.description) return false
        if (updated != other.updated) return false
        if (thumb !== other.thumb) return false
        if (image !== other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + owner.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + updated.hashCode()
        return result
    }
}
