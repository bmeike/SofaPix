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
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.couchbase.android.sofapix.db.Pict
import com.couchbase.android.sofapix.view.PixAdapter
import com.couchbase.android.sofapix.vm.DaggerModelComponent
import com.couchbase.android.sofapix.vm.MainViewModel
import com.couchbase.android.sofapix.vm.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.content_main.pixList
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: PixAdapter

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

        DaggerModelComponent.create().inject(this)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory).get(MainViewModel::class.java)

        pixList.hasFixedSize()
        pixList.layoutManager = LinearLayoutManager(this)
        adapter = PixAdapter()
        pixList.adapter = adapter

        viewModel.pix.observe(this, Observer<List<Pict>> { pix -> adapter.setPix(pix) })

        fab.setOnClickListener { v ->
            Snackbar.make(v, R.string.add_picture_desc, Snackbar.LENGTH_LONG)
                .setAction(R.string.add_picture, { viewModel.addPicture() }).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPix()
    }
}
