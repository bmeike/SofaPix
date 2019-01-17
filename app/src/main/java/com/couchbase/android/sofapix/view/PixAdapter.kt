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

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.couchbase.android.sofapix.R
import com.couchbase.android.sofapix.db.Pict
import com.couchbase.android.sofapix.db.Pix
import com.couchbase.android.sofapix.vm.PixVM

class PixAdapter(private val vm: PixVM) : RecyclerView.Adapter<PictViewHolder>() {
    private var pix: Pix? = null

    override fun getItemCount() = pix?.size ?: -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictViewHolder {
        return PictViewHolder(
            vm,
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

    fun setPix(pix: Pix?) {
        this.pix = pix
        notifyDataSetChanged()
    }
}

class PictViewHolder(private val vm: PixVM, pictView: View) : RecyclerView.ViewHolder(pictView) {
    private val ownerView: TextView = pictView.findViewById(R.id.owner)
    private val timeView: TextView = pictView.findViewById(R.id.timestamp)
    private val descView: TextView = pictView.findViewById(R.id.description)
    private var pict: Pict? = null

    fun setPict(pict: Pict) {
        this.pict = pict
        ownerView.text = pict.owner
        timeView.text = DateUtils.getRelativeTimeSpanString(pict.updated * 1000)
        descView.text = pict.description
        itemView.setOnClickListener { vm.editPict(pict) }
    }
}
