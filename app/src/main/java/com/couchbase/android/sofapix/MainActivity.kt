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
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.couchbase.android.sofapix.app.APP
import com.couchbase.android.sofapix.model.Pix
import com.couchbase.android.sofapix.view.PixAdapter
import com.couchbase.android.sofapix.vm.PixVM
import com.couchbase.android.sofapix.vm.VMFactory
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.content_main.pixList
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: VMFactory

    private lateinit var viewModel: PixVM
    private lateinit var adapter: PixAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        APP?.vmFactory()?.inject(this)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory).get(PixVM::class.java)

        pixList.hasFixedSize()
        pixList.layoutManager = LinearLayoutManager(this)
        adapter = PixAdapter(viewModel, ContextCompat.getDrawable(this, R.mipmap.ic_launcher)!!)
        pixList.adapter = adapter

        viewModel.pix.observe(this, Observer<Pix> { pix -> adapter.setPix(pix) })

        fab.setOnClickListener { viewModel.editPict(null) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPix()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_login -> {
                viewModel.login()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
