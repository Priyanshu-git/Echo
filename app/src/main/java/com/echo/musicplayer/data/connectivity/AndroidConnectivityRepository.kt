package com.echo.musicplayer.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.echo.musicplayer.domain.model.ConnectivityStatus
import com.echo.musicplayer.domain.repository.ConnectivityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidConnectivityRepository @Inject constructor(
    @ApplicationContext context: Context,
) : ConnectivityRepository {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    override val status: Flow<ConnectivityStatus> = callbackFlow {
        fun sendCurrentStatus() {
            trySend(connectivityManager.currentStatus())
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = sendCurrentStatus()
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = sendCurrentStatus()
            override fun onLost(network: Network) = sendCurrentStatus()
            override fun onUnavailable() = sendCurrentStatus()
        }

        sendCurrentStatus()
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    private fun ConnectivityManager.currentStatus(): ConnectivityStatus {
        val capabilities = getNetworkCapabilities(activeNetwork)
        val isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return if (isOnline) ConnectivityStatus.Online else ConnectivityStatus.Offline
    }
}
