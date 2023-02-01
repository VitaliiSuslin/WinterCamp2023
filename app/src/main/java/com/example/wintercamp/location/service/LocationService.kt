package com.example.wintercamp.location.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.wintercamp.Injector
import com.example.wintercamp.MyApplication

class LocationService : Service() {
    companion object {
        const val ACTION_START = "start"
        const val ACTION_RESTART = "restart"

        private const val SATELLITE_COUNT_MINIMUM = 4
        private const val DEFAULT_TIMEOUT_SATELLITES = 10000
        private const val DEFAULT_UPDATE_INTERVAL = 10000L
        private const val DEFAULT_UPDATE_DIST = 15f

        fun start(context: Context) {
            context.startService(
                Intent(
                    context,
                    LocationService::class.java
                ).setAction(ACTION_START)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LocationService::class.java))
        }
    }

    private val apiServiceBinder: ApiServiceBinder by lazy { ApiServiceBinder() }

    private var locationManager: LocationManager? = null
    private var fixSatelliteCount = 0
    private var lostFixSatellitesTime: Long = 0

    override fun onBind(p0: Intent?): IBinder {
        return apiServiceBinder.apply { setupIntent(p0) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            when (intent.action) {
                ACTION_START -> {
                    startTracking()
                    Toast.makeText(applicationContext, "Start service", Toast.LENGTH_SHORT).show()
                }
                ACTION_RESTART -> restartService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }


    private val providersChangedReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //Update GPS statuses
            }
        }
    }

    private val gnssStatusCallBack by lazy {
        object : GnssStatus.Callback() {

            override fun onStarted() {
                super.onStarted()
                Log.i("LocationSer", "GNSS System has been started")
            }

            override fun onStopped() {
                super.onStopped()
                Log.i("LocationSer", "GNSS System has been stopped")
                fixSatelliteCount = 0
            }

            @SuppressLint("MissingPermission")
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)
                fixSatelliteCount = 0
                fixSatelliteCount = status.satelliteCount

                if (fixSatelliteCount < SATELLITE_COUNT_MINIMUM && lostFixSatellitesTime == 0L) {
                    lostFixSatellitesTime = System.currentTimeMillis()

                    locationProcessor().pushLocationStatus(LocationStatus.LOST)
                    Log.i(
                        "LocationSer",
                        "Satellites lost, waiting " + DEFAULT_TIMEOUT_SATELLITES / 1000 + " seconds"
                    )
                } else {
                    locationProcessor().pushLocationStatus(LocationStatus.FOUND)
                }
            }
        }
    }

    private val locationListener by lazy {
        LocationListener {
            if (fixSatelliteCount >= SATELLITE_COUNT_MINIMUM) {
                locationProcessor().pushLocation(it)
            }
        }
    }

    private fun stopService() {
        stopTracking()
        Toast.makeText(applicationContext, "Stop service", Toast.LENGTH_SHORT).show()
    }

    private fun restartService() {
        stopTracking()
        startTracking()
        Toast.makeText(applicationContext, "Service is restarted!", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        registerReceiver(
            providersChangedReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )

        if (hasPermissions() && locationManager?.allProviders?.contains(LocationManager.GPS_PROVIDER) == true) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                DEFAULT_UPDATE_INTERVAL,
                DEFAULT_UPDATE_DIST,
                locationListener
            )
        }
        if (hasPermissions()) {
            locationManager?.registerGnssStatusCallback(gnssStatusCallBack, null)
        }
    }

    private fun stopTracking() {
        unregisterReceiver(providersChangedReceiver)
        if (hasPermissions()) {
            locationManager?.removeUpdates(locationListener)
        }
        locationManager = null
    }

    private fun hasPermissions(): Boolean {
        return isPermissionFineLocationGranted(this)
                || isPermissionCoarseLocationGranted(this)
    }

    private fun isPermissionFineLocationGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isPermissionCoarseLocationGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun locationProcessor() = Injector.locationProcessor

    inner class ApiServiceBinder : Binder() {
        var intent: Intent? = null
            private set

        fun setupIntent(intent: Intent?) {
            this.intent = intent
        }
    }

    enum class LocationStatus {
        FOUND, LOST
    }
}