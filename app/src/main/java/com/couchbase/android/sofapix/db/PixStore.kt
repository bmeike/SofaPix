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

import com.couchbase.android.sofapix.app.SofaPix
import com.couchbase.android.sofapix.logging.LOG
import com.couchbase.android.sofapix.model.Pict
import com.couchbase.android.sofapix.model.Pix
import com.couchbase.android.sofapix.time.CLOCK
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.couchbase.lite.Document
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import dagger.Binds
import dagger.Module
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import kotlinx.coroutines.rx2.rxMaybe
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject
import javax.inject.Named


const val TAG = "DB"

private const val DB_NAME = "pixdb"
private const val PROP_ID = "_id"
private const val PROP_OWNER = "owner"
private const val PROP_DESCRIPTION = "description"
private const val PROP_UPDATED = "updated"
private const val PROP_PICTURE = "picture"


interface PixStore {
    fun fetchPix(): Single<Pix>
    fun fetchPict(pictId: String): Maybe<Pict>
    fun addOrUpdatePict(pictId: String?, owner: String, desc: String): Pict?
    fun deletePict(pictId: String): Pict?
}

class CouchbasePixStore @Inject constructor(
    private val app: SofaPix,
    @Named("database") dbScheduler: Scheduler
) : PixStore {
    private val dbDispatcher = dbScheduler.asCoroutineDispatcher()
    private val db by lazy { Database(DB_NAME, DatabaseConfiguration(app)) }

    override fun fetchPix(): Single<Pix> = GlobalScope.rxSingle(dbDispatcher) {
        try {
            return@rxSingle QueryBuilder
                .select(SelectResult.expression(Meta.id).`as`(PROP_ID), SelectResult.all())
                .from(DataSource.database(db))
                .execute()
                .map { row -> pictFromResult(row) }
        } catch (e: CouchbaseLiteException) {
            LOG.e(TAG, "failed fetching pix", e)
        }

        return@rxSingle emptyList<Pict>()
    }

    override fun fetchPict(pictId: String): Maybe<Pict> = GlobalScope.rxMaybe(dbDispatcher) {
        try {
            return@rxMaybe pictFromDoc(getPictById(pictId))
        } catch (e: CouchbaseLiteException) {
            LOG.e(TAG, "failed fetching pict", e)
        }

        return@rxMaybe null
    }

    override fun addOrUpdatePict(pictId: String?, owner: String, desc: String): Pict? {
        return if (pictId == null) {
            addPict(owner, desc)
        } else {
            updatePict(pictId, owner, desc)
        }
    }

    override fun deletePict(pictId: String): Pict? {
        try {
            val doc = getPictById(pictId)
            doc ?: return null
            db.delete(doc)
            return pictFromDoc(doc)
        } catch (e: CouchbaseLiteException) {
            LOG.e(TAG, "failed deleting pict", e)
        }

        return null
    }

    private fun addPict(owner: String, desc: String): Pict? {
        val doc = MutableDocument()
        doc.setString(PROP_OWNER, owner)
        doc.setString(PROP_DESCRIPTION, desc)
        doc.setLong(PROP_UPDATED, CLOCK.now().epochSecond)
        return saveDocument(doc)
    }

    private fun updatePict(pictId: String, owner: String, desc: String): Pict? {
        val doc = getPictById(pictId).toMutable()
        doc ?: return null

        var updated = false

        if (doc.getString(PROP_OWNER) != owner) {
            doc.setString(PROP_OWNER, owner)
            updated = true
        }

        if (doc.getString(PROP_DESCRIPTION) != desc) {
            doc.setString(PROP_DESCRIPTION, desc)
            updated = true
        }

        if (!updated) {
            return pictFromDoc(doc)
        }

        doc.setLong(PROP_UPDATED, CLOCK.now().epochSecond)
        return saveDocument(doc)
    }

    private fun saveDocument(doc: MutableDocument): Pict? {
        try {
            db.save(doc)
            return pictFromDoc(doc)
        } catch (e: CouchbaseLiteException) {
            LOG.e(TAG, "failed saving pict", e)
        }

        return null
    }

    private fun pictFromDoc(doc: Document): Pict = Pict(
        doc.id,
        doc.getString(PROP_OWNER),
        doc.getString(PROP_DESCRIPTION),
        doc.getLong(PROP_UPDATED)
    )

    private fun pictFromResult(result: Result): Pict {
        val props = result.getDictionary(DB_NAME)
        return Pict(
            result.getString(PROP_ID),
            props.getString(PROP_OWNER),
            props.getString(PROP_DESCRIPTION),
            props.getLong(PROP_UPDATED)
        )
    }

    private fun getPictById(pictId: String) = db.getDocument(pictId)
}


@Module
interface StoreModule {
    @Binds
    fun bindsPixStore(store: CouchbasePixStore): PixStore
}
