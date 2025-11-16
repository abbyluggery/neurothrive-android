package com.neurothrive.assistant.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurothrive.assistant.auth.OAuthManager
import com.neurothrive.assistant.sync.SyncManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class OAuthActivity : ComponentActivity() {

    private val viewModel: OAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NeuroThriveTheme {
                OAuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        setResult(RESULT_OK)
                        finish()
                    },
                    onAuthFailed = {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OAuthScreen(
    viewModel: OAuthViewModel,
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (authState) {
            is AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Authenticating...")
                    }
                }
            }
            is AuthState.Success -> {
                LaunchedEffect(Unit) {
                    onAuthSuccess()
                }
            }
            is AuthState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Authentication Failed",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (authState as AuthState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAuthFailed) {
                            Text("Close")
                        }
                    }
                }
            }
            is AuthState.Idle -> {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    url: String
                                ): Boolean {
                                    if (url.startsWith(OAuthManager.REDIRECT_URI)) {
                                        val uri = Uri.parse(url)
                                        val code = uri.getQueryParameter("code")

                                        if (code != null) {
                                            viewModel.exchangeCodeForToken(code)
                                        } else {
                                            val error = uri.getQueryParameter("error")
                                            viewModel.handleError(error ?: "Unknown error")
                                        }
                                        return true
                                    }
                                    return false
                                }
                            }

                            loadUrl(viewModel.getAuthorizationUrl())
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val oAuthManager: OAuthManager,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun getAuthorizationUrl(): String {
        return oAuthManager.getAuthorizationUrl()
    }

    fun exchangeCodeForToken(code: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = oAuthManager.exchangeCodeForToken(code)

            if (result.isSuccess) {
                Timber.i("OAuth authentication successful")
                // Schedule periodic sync
                syncManager.schedulePeriodicSync()
                _authState.value = AuthState.Success
            } else {
                val error = result.exceptionOrNull()?.message ?: "Authentication failed"
                Timber.e("OAuth authentication failed: $error")
                _authState.value = AuthState.Error(error)
            }
        }
    }

    fun handleError(error: String) {
        _authState.value = AuthState.Error(error)
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
