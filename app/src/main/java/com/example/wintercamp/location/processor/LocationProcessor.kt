package com.example.wintercamp.location.processor

import android.location.Location
import com.example.wintercamp.location.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

class LocationProcessor {

    private val _locationStatusState = MutableStateFlow(LocationService.LocationStatus.LOST)
    val locationStatusState = _locationStatusState.asStateFlow()

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState = _locationState.filterNotNull()

    fun pushLocationStatus(status: LocationService.LocationStatus) {
        _locationStatusState.value = status
    }

    fun pushLocation(location: Location) {
        _locationState.value = location
    }
}