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
package com.couchbase.android.sofapix.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.couchbase.android.sofapix.LoginActivity
import com.couchbase.android.sofapix.R


const val KEY_TOKEN_TYPE = "sofapix.auth.TOKEN_TYPE"
const val KEY_FEATURES = "sofapix.auth.FEATURES"
const val KEY_OPTIONS = "sofapix.auth.OPTIONS"

class AuthenticatorService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return SofaPixAuthenticator(this).iBinder
    }
}

class SofaPixAuthenticator(private val ctxt: Context) : AbstractAccountAuthenticator(ctxt) {
    override fun getAuthTokenLabel(authTokenType: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        response ?: throw IllegalStateException("Null response object")

        val reply = Bundle()

        val at = ctxt.getString(R.string.account_type)
        reply.putString(AccountManager.KEY_ACCOUNT_TYPE, at)

        if (at != accountType) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1)
            reply.putString(AccountManager.KEY_ERROR_MESSAGE, ctxt.getString(R.string.unrecognized_account_type))
            return reply
        }

        val intent = Intent(ctxt, LoginActivity::class.java)
            .putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        if (authTokenType != null) {
            intent.putExtra(KEY_TOKEN_TYPE, authTokenType)
        }
        if (requiredFeatures != null) {
            intent.putExtra(KEY_FEATURES, requiredFeatures)
        }
        if (options != null) {
            intent.putExtra(KEY_OPTIONS, options)
        }

        reply.putParcelable(AccountManager.KEY_INTENT, intent)

        return reply
    }
}

