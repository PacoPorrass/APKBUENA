package com.empresa.vaultdrive.core.session

import android.app.Activity
import android.content.Context
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.empresa.vaultdrive.core.security.Prefs
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class AuthResult {
    data class Success(val token: String) : AuthResult()
    data class Error(val msg: String) : AuthResult()
    object Cancelled : AuthResult()
}

object TokenManager {

    // ✅ SOLO USER.READ (evita admin consent)
    val SCOPES = arrayOf(
    "User.Read",
"Files.ReadWrite",
"Files.Read.All",
"Sites.Read.All"
)

    private var msalApp: ISingleAccountPublicClientApplication? = null

    fun init(context: Context, onReady: () -> Unit) {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            com.empresa.vaultdrive.R.raw.msal_config,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {

                override fun onCreated(app: ISingleAccountPublicClientApplication) {
                    msalApp = app
                    onReady()
                }

                override fun onError(e: MsalException) {
                    msalApp = null
                    onReady()
                }
            }
        )
    }

    suspend fun signIn(activity: Activity): AuthResult =
        suspendCancellableCoroutine { cont ->

            val app = msalApp
            if (app == null) {
                cont.resume(AuthResult.Error("MSAL no inicializado"))
                return@suspendCancellableCoroutine
            }

            app.signIn(activity, null, SCOPES, object : AuthenticationCallback {

                override fun onSuccess(result: IAuthenticationResult) {
                    Prefs.token = result.accessToken
                    Prefs.tokenExpiry = result.expiresOn.time
                    Prefs.userName = result.account?.username ?: ""
                    cont.resume(AuthResult.Success(result.accessToken))
                }

                override fun onError(e: MsalException) {
                    cont.resume(AuthResult.Error(e.localizedMessage ?: "Error MSAL"))
                }

                override fun onCancel() {
                    cont.resume(AuthResult.Cancelled)
                }
            })
        }

    suspend fun refreshSilently(): String? =
        suspendCancellableCoroutine { cont ->

            val app = msalApp
            if (app == null) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            app.getCurrentAccountAsync(object :
                ISingleAccountPublicClientApplication.CurrentAccountCallback {

                override fun onAccountLoaded(account: IAccount?) {
                    if (account == null) {
                        cont.resume(null)
                        return
                    }

                    app.acquireTokenSilentAsync(
                        AcquireTokenSilentParameters.Builder()
                            .forAccount(account)
                            .fromAuthority(account.authority)
                            .withScopes(SCOPES.toList())
                            .withCallback(object : SilentAuthenticationCallback {

                                override fun onSuccess(result: IAuthenticationResult) {
                                    Prefs.token = result.accessToken
                                    Prefs.tokenExpiry = result.expiresOn.time
                                    cont.resume(result.accessToken)
                                }

                                override fun onError(e: MsalException) {
                                    cont.resume(null)
                                }
                            })
                            .build()
                    )
                }

                override fun onAccountChanged(
                    previousAccount: IAccount?,
                    currentAccount: IAccount?
                ) {
                    cont.resume(null)
                }

                override fun onError(e: MsalException) {
                    cont.resume(null)
                }
            })
        }

    fun signOut(onDone: () -> Unit) {
        val app = msalApp
        if (app == null) {
            Prefs.clear()
            onDone()
            return
        }

        app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {

            override fun onSignOut() {
                Prefs.clear()
                onDone()
            }

            override fun onError(e: MsalException) {
                Prefs.clear()
                onDone()
            }
        })
    }
}
