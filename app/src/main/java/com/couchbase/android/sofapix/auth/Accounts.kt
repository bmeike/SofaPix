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

import android.accounts.Account
import android.accounts.AccountManager
import com.couchbase.android.sofapix.app.SofaPix
import dagger.Binds
import dagger.Module
import io.reactivex.Scheduler
import io.reactivex.Single
import kotlinx.coroutines.rx2.asCoroutineDispatcher
import kotlinx.coroutines.rx2.rxSingle
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


private const val SYNC_GATEWAY_TOKEN_TYPE = "sofapix.SYNC_GATEWAY_TOKEN"

@Module
interface AuthModule {
    @Binds
    fun bindsPixStore(store: AndroidAccounts): Accounts
}

interface Accounts {
    fun createAccount(username: String, password: String): Single<Account>
}

@Singleton
class AndroidAccounts @Inject constructor(
    private val app: SofaPix,
    @Named("worker") workScheduler: Scheduler,
    @Named("main") private val mainScheduler: Scheduler,
    @Named("accountType") private val accountType: String
) : Accounts {
    private val accountManager = AccountManager.get(app)
    private val workDispatcher = workScheduler.asCoroutineDispatcher()

    override fun createAccount(username: String, password: String) =
        rxSingle(workDispatcher) {
            var account = findAccount(username)
            if (account == null) {
                account = Account(username, accountType)
                accountManager.addAccountExplicitly(account, null, null)
            }
            accountManager.setPassword(account, null)
            accountManager.setAuthToken(account, SYNC_GATEWAY_TOKEN_TYPE, UUID.randomUUID().toString())

            return@rxSingle account!!
        }.observeOn(mainScheduler)

    private fun findAccount(username: String): Account? {
        return accountManager.getAccountsByType(accountType).firstOrNull { account -> account.name == username }
    }
}
