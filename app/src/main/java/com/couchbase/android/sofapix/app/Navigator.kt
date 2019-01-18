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

import android.content.Intent
import com.couchbase.android.sofapix.DetailActivity
import com.couchbase.android.sofapix.MainActivity
import com.couchbase.android.sofapix.PARAM_PICT_ID
import com.couchbase.android.sofapix.model.Pict
import dagger.Binds
import dagger.Module
import javax.inject.Inject
import javax.inject.Singleton


interface Navigator {
    fun mainPage()
    fun detailPage(pict: Pict?)
}

@Singleton
class SofaPixNavigator @Inject constructor(private val app: SofaPix) : Navigator {
    override fun mainPage() {
        val intent = Intent(app, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        app.startActivity(Intent(app, MainActivity::class.java))
    }

    override fun detailPage(pict: Pict?) {
        val intent = Intent(app, DetailActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        if (pict != null) {
            intent.putExtra(PARAM_PICT_ID, pict.id)
        }
        app.startActivity(intent)
    }
}

@Module
interface NavModule {
    @Binds
    fun bindsNavigator(nav: SofaPixNavigator): Navigator
}
