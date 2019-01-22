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

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import com.couchbase.android.sofapix.app.APP
import com.couchbase.android.sofapix.logging.LOG
import com.couchbase.android.sofapix.model.Pict
import com.couchbase.android.sofapix.time.MS_PER_SEC
import com.couchbase.android.sofapix.vm.PictVM
import com.couchbase.android.sofapix.vm.VMFactory
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.content_detail.delete
import kotlinx.android.synthetic.main.content_detail.description
import kotlinx.android.synthetic.main.content_detail.image
import kotlinx.android.synthetic.main.content_detail.owner
import kotlinx.android.synthetic.main.content_detail.update
import kotlinx.android.synthetic.main.content_detail.updated
import javax.inject.Inject


private const val TAG = "DETS"

private const val ACTION_CHOOSE_IMAGE = 7001

const val PARAM_PICT_ID = "sofapix.PICT_ID"


class DetailActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: VMFactory

    private lateinit var viewModel: PictVM

    private var progressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        APP?.vmFactory()?.inject(this)

        setContentView(R.layout.activity_detail)

        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory).get(PictVM::class.java)
        viewModel.pictId = intent.getStringExtra(PARAM_PICT_ID)

        viewModel.pict.observe(this, Observer { pict -> showPict(pict) })
        viewModel.image.observe(this, Observer { image -> showImage(image) })

        image.setOnClickListener { getCameraRollPhoto() }

        delete.setOnClickListener { viewModel.deletePict() }

        update.setOnClickListener { updatePict(owner.text.toString(), description.text.toString()) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPict()
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            LOG.w(TAG, "Request to external activity failed: $requestCode, $resultCode")
            return
        }

        when (requestCode) {
            ACTION_CHOOSE_IMAGE -> {
                val uri = data?.data
                uri ?: return
                viewModel.fetchImageFromUri(uri)
            }
            else -> LOG.w(TAG, "Unrecognized request: ${requestCode}")
        }
    }

    private fun getCameraRollPhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.get_pict_from_camera_roll)),
            ACTION_CHOOSE_IMAGE
        )
    }

    private fun showPict(pict: Pict?) {
        owner.setText(pict?.owner)
        description.setText(pict?.description)
        updated.text = if (pict == null) {
            null
        } else {
            DateUtils.getRelativeTimeSpanString(pict.updated * MS_PER_SEC)
        }
    }

    private fun updatePict(owner: String, desc: String) {
        showLoadingDialog()
        viewModel.updatePict(owner, desc)
        viewModel.loading.observe(this, Observer { loading ->
            if (loading?.not() == true) {
                hideLoadingDialog()
                viewModel.exit()
            }
        })
    }

    private fun showImage(bitmap: Bitmap?) {
        image.setImageBitmap(bitmap)
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog.show(
            this,
            getString(R.string.just_a_moment),
            getString(R.string.loading_photo),
            false,
            false
        )
    }

    private fun hideLoadingDialog() {
        progressDialog?.dismiss()
    }
}
