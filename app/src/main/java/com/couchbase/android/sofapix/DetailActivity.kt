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
package com.couchbase.android.sofapix

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import com.couchbase.android.sofapix.app.APP
import com.couchbase.android.sofapix.db.Pict
import com.couchbase.android.sofapix.vm.PictVM
import com.couchbase.android.sofapix.vm.VMFactory
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.content_detail.delete
import kotlinx.android.synthetic.main.content_detail.description
import kotlinx.android.synthetic.main.content_detail.owner
import kotlinx.android.synthetic.main.content_detail.update
import kotlinx.android.synthetic.main.content_detail.updated
import javax.inject.Inject


const val PARAM_PICT_ID = "sofapix.PICT_ID"


class DetailActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: VMFactory

    private lateinit var viewModel: PictVM

    private var pictId: String? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        APP?.vmFactory()?.inject(this)

        pictId = intent.getStringExtra(PARAM_PICT_ID)

        setContentView(R.layout.activity_detail)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory).get(PictVM::class.java)

        viewModel.pict.observe(this, Observer<Pict?> { pict -> setPict(pict) })

        delete.setOnClickListener { viewModel.deletePict(pictId) }

        update.setOnClickListener { viewModel.updatePict(pictId, owner.text.toString(), description.text.toString()) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPict(pictId)
    }

    private fun setPict(pict: Pict?) {
        owner.setText(pict?.owner)
        description.setText(pict?.description)
        updated.text = if (pict == null) {
            null
        } else {
            DateUtils.getRelativeTimeSpanString(pict.updated * 1000)
        }
    }
}
