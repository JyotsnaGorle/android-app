/*
 * This file is part of FreedomBox.
 *
 * FreedomBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FreedomBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FreedomBox. If not, see <http://www.gnu.org/licenses/>.
 */

package org.freedombox.freedombox.views.fragments

import android.content.Context.NSD_SERVICE
import android.content.SharedPreferences
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import org.freedombox.freedombox.R
import org.freedombox.freedombox.components.AppComponent
import javax.inject.Inject

class DiscoveryFragment : BaseFragment() {
    val TAG = "DISCOVERY_FRAGMENT"

    private val SERVICE = "_freedombox._tcp"

    @Inject lateinit var sharedPreferences: SharedPreferences

    lateinit var nsdManager: NsdManager

    private lateinit var discoveryListener: FBXDiscoveryListener

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        nsdManager = activity.getSystemService(NSD_SERVICE) as NsdManager

        discoveryListener = FBXDiscoveryListener()

        nsdManager.discoverServices(SERVICE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    companion object {
        fun new(args: Bundle): DiscoveryFragment {
            val fragment = DiscoveryFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_discovery

    override fun injectFragment(appComponent: AppComponent) = appComponent.inject(this)

    override fun onDestroy() {
        nsdManager.stopServiceDiscovery(discoveryListener)

        super.onDestroy()
    }

    inner class FBXResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed" + errorCode)
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. " + serviceInfo)

            Log.d(TAG, serviceInfo.port.toString())
            Log.d(TAG, serviceInfo.host.toString())
        }
    }

    inner class FBXDiscoveryListener : NsdManager.DiscoveryListener {
        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, serviceInfo.serviceType)
            Log.d(TAG, serviceInfo.serviceName)

            nsdManager.resolveService(serviceInfo, FBXResolveListener())
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode)

            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode)

            nsdManager.stopServiceDiscovery(this)
        }

        override fun onDiscoveryStarted(serviceType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: " + serviceType)
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "service lost" + serviceInfo)
        }
    }
}
