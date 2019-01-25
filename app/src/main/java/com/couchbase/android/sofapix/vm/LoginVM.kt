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
package com.couchbase.android.sofapix.vm

import android.accounts.Account
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.couchbase.android.sofapix.app.Navigator
import com.couchbase.android.sofapix.auth.Accounts
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named


private const val TAG = "LOGIN"

@Module
interface LoginVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(LoginVM::class)
    fun bindViewModel(vm: LoginVM): ViewModel
}

class LoginVM @Inject constructor(
    private val nav: Navigator,
    @Named("main") private val mainScheduler: Scheduler,
    private val accounts: Accounts
) : ViewModel() {
    val account: MutableLiveData<Account> = MutableLiveData()
    val error: MutableLiveData<String?> = MutableLiveData()

    private var loader: Disposable? = null

    fun login(username: String, password: String) {
        loader = accounts.createAccount(username, password).subscribe(
            { newAccount -> account.value = newAccount },
            { err -> error.value = err.localizedMessage })
    }
}
