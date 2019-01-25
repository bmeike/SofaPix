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

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Dialog
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.couchbase.android.sofapix.app.APP
import com.couchbase.android.sofapix.auth.KEY_FEATURES
import com.couchbase.android.sofapix.auth.KEY_OPTIONS
import com.couchbase.android.sofapix.auth.KEY_TOKEN_TYPE
import com.couchbase.android.sofapix.vm.LoginVM
import com.couchbase.android.sofapix.vm.VMFactory
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.content_login.login
import kotlinx.android.synthetic.main.content_login.password
import kotlinx.android.synthetic.main.content_login.username
import javax.inject.Inject


class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: VMFactory

    private lateinit var viewModel: LoginVM

    private var response: AccountAuthenticatorResponse? = null
    private var tokenType: String? = null
    private var requiredFeatures: Array<out String>? = null
    private var options: Bundle? = null
    private var progressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        APP?.vmFactory()?.inject(this)

        setContentView(R.layout.activity_login)

        setSupportActionBar(toolbar)

        val i = intent
        response = i.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        tokenType = i.getStringExtra(KEY_TOKEN_TYPE)
        requiredFeatures = i.getStringArrayExtra(KEY_FEATURES)
        options = i.getBundleExtra(KEY_OPTIONS)

        viewModel = ViewModelProviders.of(this, vmFactory).get(LoginVM::class.java)

        viewModel.error.observe(this, Observer { msg -> showError(msg) })
        viewModel.account.observe(this, Observer { account -> loggedIn(account) })

        login.setOnClickListener {
            showLoadingDialog()
            viewModel.login(username.text.toString(), password.text.toString())
        }
    }

    override fun finish() {
        val resp = response
        if (resp != null) {
            resp.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            response = null
        }
        super.finish()
    }

    private fun showError(msg: String?) {
        hideLoadingDialog()
        username.setText("")
        password.setText("")
        Toast.makeText(this, "Login failed: ${msg}", Toast.LENGTH_LONG).show()
    }

    private fun loggedIn(account: Account?) {
        account ?: return

        val reply = Bundle()
        reply.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
        reply.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
        val resp = response
        if (resp != null) {
            resp.onResult(reply)
            response = null
        }

        hideLoadingDialog()

        finish()
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog.show(
            this,
            getString(R.string.just_a_moment),
            getString(R.string.logging_in),
            false,
            false
        )
    }

    private fun hideLoadingDialog() {
        progressDialog?.dismiss()
    }
}
