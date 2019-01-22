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
package com.couchbase.android.sofapix.view

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.couchbase.android.sofapix.R
import com.couchbase.android.sofapix.logging.LOG
import com.couchbase.android.sofapix.model.Pict
import com.couchbase.android.sofapix.model.Pix
import com.couchbase.android.sofapix.vm.PixVM
import io.reactivex.disposables.Disposable


private const val TAG = "PIXADAPT"

private const val MS_PER_SEC = 1000L

class PixAdapter(private val vm: PixVM, private val placeholder: Drawable) : RecyclerView.Adapter<PictViewHolder>() {
    private var pix: Pix? = null
    override fun getItemCount() = pix?.size ?: -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictViewHolder {
        return PictViewHolder(
            vm,
            placeholder,
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.row_pict, parent, false)
        )
    }

    override fun onBindViewHolder(vh: PictViewHolder, pos: Int) {
        val allPix = pix
        allPix ?: throw IllegalStateException("Attempt to bind view holder on an empty feed")

        val pict = allPix.getOrNull(pos)
        pict ?: throw IllegalStateException("Attempt to bind view holder with pos out of range: ${pos}, ${allPix.size}")

        vh.setPict(pict)
    }

    override fun onViewRecycled(vh: PictViewHolder) {
        super.onViewRecycled(vh)
        vh.cancelLoader()
    }

    fun setPix(pix: Pix?) {
        this.pix = pix
        notifyDataSetChanged()
    }
}

class PictViewHolder(
    private val vm: PixVM,
    private val placeholder: Drawable,
    pictView: View
) : RecyclerView.ViewHolder(pictView) {
    private val ownerView: TextView = pictView.findViewById(R.id.owner)
    private val timeView: TextView = pictView.findViewById(R.id.timestamp)
    private val descView: TextView = pictView.findViewById(R.id.description)
    private val thumbView: ImageView = pictView.findViewById(R.id.thumb)
    private var pict: Pict? = null
    private var loader: Disposable? = null

    fun setPict(pict: Pict) {
        cancelLoader()

        this.pict = pict

        ownerView.text = pict.owner
        timeView.text = DateUtils.getRelativeTimeSpanString(pict.updated * MS_PER_SEC)
        descView.text = pict.description

        if (pict.thumb == null) {
            showImage(null)
        } else {
            loadImage(pict.thumb)
        }

        itemView.setOnClickListener { vm.editPict(pict) }
    }

    private fun loadImage(image: ByteArray) {
        loader = vm.loadImage(image).subscribe(
            { data ->
                LOG.w(TAG, "got thumb for pict: ${pict?.id}")
                showImage(data)
            },
            { err ->
                LOG.w(TAG, "failed loading thumb for pict: ${pict?.id}", err)
                showImage(null)
            }
        )
    }

    internal fun cancelLoader() {
        val ldr = loader
        loader = null
        ldr?.dispose()
    }

    private fun showImage(image: Bitmap?) {
        loader = null
        if (image != null) {
            thumbView.setImageBitmap(image)
        } else {
            thumbView.setImageDrawable(placeholder)
        }
    }

}
