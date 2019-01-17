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
package com.couchbase.android.sofapix.db

import com.couchbase.android.sofapix.time.CLOCK
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject


private val pix = listOf(
    Pict("feed1", "Louvre", "Waterlilies", CLOCK.now().epochSecond, null),
    Pict("feed2", "Prado", "The Dog", CLOCK.now().epochSecond, null),
    Pict("feed3", "Rijksmuseum", "The Milkmaid", CLOCK.now().epochSecond, null)
)

data class Pict(val id: String, val owner: String, val description: String, val updated: Long, val photo: Any?)

typealias Pix = List<Pict>

abstract class CouchbaseResultObserver<T>(private val compositeDisposable: CompositeDisposable) : Observer<T> {

    final override fun onComplete() {}

    final override fun onSubscribe(d: Disposable) {
        compositeDisposable.add(d)
    }

    final override fun onNext(t: T) {
        onSuccess(t)
    }

    final override fun onError(e: Throwable) {
        onFailure(e)
    }

    abstract fun onSuccess(data: T)
    abstract fun onFailure(e: Throwable)
}

class PixStore @Inject constructor() {
    fun fetchPix(): Observable<Pix> = Observable.just(pix)
    fun fetchPict(pictId: String): Observable<Pict?> = Observable.just(pix.firstOrNull { pict -> pict.id == pictId })
    fun updatePict(pictId: String?, owner: String, desc: String) = Unit
    fun deletePict(pictId: String?) = Unit
}