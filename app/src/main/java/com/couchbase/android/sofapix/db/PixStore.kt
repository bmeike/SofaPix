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

import com.couchbase.android.sofapix.logging.LOG
import com.couchbase.android.sofapix.model.Pict
import com.couchbase.android.sofapix.model.Pix
import com.couchbase.android.sofapix.time.CLOCK
import com.couchbase.lite.AbstractReplicator
import com.couchbase.lite.BasicAuthenticator
import com.couchbase.lite.Blob
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.Replicator
import com.couchbase.lite.ReplicatorConfiguration
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import com.couchbase.lite.URLEndpoint
import dagger.Binds
import dagger.Module
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxMaybe
import kotlinx.coroutines.rx2.rxSingle
import java.net.URI
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


private const val TAG = "DB"

private const val SYNC_GATEWAY_URI = "ws://10.0.2.2:4984/travel-sample"

private const val DB_NAME = "sofapix"
private const val PROP_ID = "_id"
private const val PROP_OWNER = "owner"
private const val PROP_SHARES = "shares"
private const val PROP_DESCRIPTION = "description"
private const val PROP_UPDATED = "updated"
private const val PROP_THUMBNAIL = "thumb"
private const val PROP_IMAGE = "image"

private const val MIME_PNG = "image/png"


@Module
interface PixStoreModule {
    @Binds
    fun bindsPixStore(store: CouchbasePixStore): PixStore
}

interface PixStore {
    fun fetchPix(): Single<Pix>
    fun fetchPict(pictId: String): Maybe<Pict>
    fun deletePict(pictId: String): Completable
    fun addOrUpdatePict(pictId: String?, owner: String, desc: String, thumb: ByteArray?, image: ByteArray?): Completable
}

@Singleton
class CouchbasePixStore @Inject constructor(
    @Named("database") dbScheduler: Scheduler,
    @Named("main") private val mainScheduler: Scheduler
) : PixStore {
    private val dbDispatcher = dbScheduler.asCoroutineDispatcher()
    private val database: Database? = null

    override fun fetchPix(): Single<Pix> = GlobalScope.rxSingle(dbDispatcher) {
        val db: Database = database ?: return@rxSingle emptyList<Pict>()

        return@rxSingle QueryBuilder
            .select(
                SelectResult.expression(Meta.id).`as`(PROP_ID),
                SelectResult.expression(Expression.property(PROP_OWNER)),
                SelectResult.expression(Expression.property(PROP_DESCRIPTION)),
                SelectResult.expression(Expression.property(PROP_UPDATED)),
                SelectResult.expression(Expression.property(PROP_THUMBNAIL))
            )
            .from(DataSource.database(db))
            .execute()
            .map { row -> row.toPict() }
    }.observeOn(mainScheduler)


    override fun fetchPict(pictId: String): Maybe<Pict> = GlobalScope.rxMaybe(dbDispatcher) {
        return@rxMaybe getPictById(pictId).toPict()
    }.observeOn(mainScheduler)

    override fun addOrUpdatePict(
        pictId: String?, owner: String, desc: String, thumb: ByteArray?, image: ByteArray?
    ): Completable = GlobalScope.rxCompletable(dbDispatcher) {
        if (pictId == null) {
            addPict(owner, desc, thumb, image)
        } else {
            updatePict(pictId, owner, desc, thumb, image)
        }
    }.observeOn(mainScheduler)

    override fun deletePict(pictId: String): Completable = GlobalScope.rxCompletable(dbDispatcher) {
        database?.delete(getPictById(pictId))
    }.observeOn(mainScheduler)

    private fun addPict(owner: String, desc: String, thumb: ByteArray?, image: ByteArray?): Pict? {
        val doc = MutableDocument()
        doc.setString(PROP_OWNER, owner)
        doc.setString(PROP_DESCRIPTION, desc)
        doc.setLong(PROP_UPDATED, CLOCK.now().epochSecond)

        if (thumb != null) {
            doc.setBlob(PROP_THUMBNAIL, Blob(PROP_THUMBNAIL, thumb))
        }

        if (image != null) {
            doc.setBlob(PROP_IMAGE, Blob(PROP_IMAGE, image))
        }

        return saveDocument(doc)
    }

    private fun updatePict(pictId: String, owner: String, desc: String, thumb: ByteArray?, image: ByteArray?): Pict? {
        val doc = getPictById(pictId).toMutable()

        if (!doc.updatePict(owner, desc, thumb, image)) {
            return doc.toPict()
        }

        doc.setLong(PROP_UPDATED, CLOCK.now().epochSecond)

        return saveDocument(doc)
    }

    private fun saveDocument(doc: MutableDocument): Pict? {
        val db: Database = database ?: return null
        db.save(doc)
        return doc.toPict()
    }

    private fun getPictById(pictId: String) = database?.getDocument(pictId)
        ?: throw IllegalStateException("no current database")

    private fun startPushAndPullReplicationForCurrentUser(username: String, password: String) {
        val db: Database = database ?: return

        val config = ReplicatorConfiguration(db, URLEndpoint(URI(SYNC_GATEWAY_URI)))
        config.replicatorType = ReplicatorTypeHelper.getReplicatorTypeFor(true, true)
        config.isContinuous = true
        config.authenticator = BasicAuthenticator(username, password)

        val replicator = Replicator(config)
        replicator.addChangeListener { change ->
            when (change.replicator.status.activityLevel) {
                AbstractReplicator.ActivityLevel.IDLE -> LOG.d(TAG, "sync complete")
                AbstractReplicator.ActivityLevel.STOPPED -> LOG.d(TAG, "sync stopped")
                AbstractReplicator.ActivityLevel.OFFLINE -> LOG.d(TAG, "sync offline")
                else -> LOG.d(TAG, "syncing...")
            }
        }

        replicator.start()
    }
}

fun Result.toPict(): Pict {
    return Pict(
        id = getString(PROP_ID),
        owner = getString(PROP_OWNER)!!,
        description = getString(PROP_DESCRIPTION)!!,
        updated = getLong(PROP_UPDATED),
        thumb = getBlob(PROP_THUMBNAIL)?.content
    )
}

fun Document.toPict(): Pict {
    return Pict(
        id = id,
        owner = getString(PROP_OWNER) ?: "",
        description = getString(PROP_DESCRIPTION) ?: "",
        updated = getLong(PROP_UPDATED),
        thumb = getBlob(PROP_THUMBNAIL)?.content,
        image = getBlob(PROP_IMAGE)?.content
    )
}

fun MutableDocument.updatePict(owner: String, desc: String, thumb: ByteArray?, image: ByteArray?): Boolean {
    var updated = false

    if (this.getString(PROP_OWNER) != owner) {
        this.setString(PROP_OWNER, owner)
        updated = true
    }

    if (this.getString(PROP_DESCRIPTION) != desc) {
        this.setString(PROP_DESCRIPTION, desc)
        updated = true
    }

    if (thumb != null) {
        this.setBlob(PROP_THUMBNAIL, Blob(PROP_THUMBNAIL, thumb))
        updated = true
    }

    if (image != null) {
        this.setBlob(PROP_IMAGE, Blob(PROP_IMAGE, image))
        updated = true
    }

    return updated
}
