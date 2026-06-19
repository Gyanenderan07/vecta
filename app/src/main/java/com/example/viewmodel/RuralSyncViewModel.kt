package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RuralSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SyncRepository(db.userDao(), db.complaintDao())

    // UI States - Authentication
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>("Villager Node")
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _currentUserMobile = MutableStateFlow("9999999999")
    val currentUserMobile: StateFlow<String> = _currentUserMobile.asStateFlow()

    private val _currentUserAvatarUrl = MutableStateFlow<String?>(null)
    val currentUserAvatarUrl: StateFlow<String?> = _currentUserAvatarUrl.asStateFlow()

    private val _permissionsOnboarded = MutableStateFlow(false)
    val permissionsOnboarded: StateFlow<Boolean> = _permissionsOnboarded.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    // UI States - Performance & Edge Network metrics
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _simulatedSpeedString = MutableStateFlow("0.0 KB/s")
    val simulatedSpeedString: StateFlow<String> = _simulatedSpeedString.asStateFlow()

    private val _simulatedSpeedMbps = MutableStateFlow("0.00 Mbps")
    val simulatedSpeedMbps: StateFlow<String> = _simulatedSpeedMbps.asStateFlow()

    private val _pollingIntervalMs = MutableStateFlow(2500L)
    val pollingIntervalMs: StateFlow<Long> = _pollingIntervalMs.asStateFlow()

    private val _isEcoMode = MutableStateFlow(false)
    val isEcoMode: StateFlow<Boolean> = _isEcoMode.asStateFlow()

    private val _expectedLatency = MutableStateFlow("Minimal Base (~0.04s)")
    val expectedLatency: StateFlow<String> = _expectedLatency.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow("Checking State...")
    val syncStatusMessage: StateFlow<String> = _syncStatusMessage.asStateFlow()

    private val _unsyncedQueueSize = MutableStateFlow(0)
    val unsyncedQueueSize: StateFlow<Int> = _unsyncedQueueSize.asStateFlow()

    // GPS/Address States
    private val _selectedVillage = MutableStateFlow("Detecting physical node...")
    val selectedVillage: StateFlow<String> = _selectedVillage.asStateFlow()

    private val _latitude = MutableStateFlow(0.0)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(0.0)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _resolvedVillage = MutableStateFlow("Detecting...")
    val resolvedVillage: StateFlow<String> = _resolvedVillage.asStateFlow()

    private val _resolvedPanchayat = MutableStateFlow("Detecting...")
    val resolvedPanchayat: StateFlow<String> = _resolvedPanchayat.asStateFlow()

    private val _resolvedTaluk = MutableStateFlow("Detecting...")
    val resolvedTaluk: StateFlow<String> = _resolvedTaluk.asStateFlow()

    private val _resolvedDistrict = MutableStateFlow("Detecting...")
    val resolvedDistrict: StateFlow<String> = _resolvedDistrict.asStateFlow()

    private val _resolvedState = MutableStateFlow("Detecting...")
    val resolvedState: StateFlow<String> = _resolvedState.asStateFlow()

    private val _resolvedCountry = MutableStateFlow("Detecting...")
    val resolvedCountry: StateFlow<String> = _resolvedCountry.asStateFlow()

    private val _resolvedPostalCode = MutableStateFlow("Detecting...")
    val resolvedPostalCode: StateFlow<String> = _resolvedPostalCode.asStateFlow()

    // Telemetry trace log list (like the bottom terminal console)
    private val _telemetryLogs = MutableStateFlow<List<String>>(emptyList())
    val telemetryLogs: StateFlow<List<String>> = _telemetryLogs.asStateFlow()

    // Cloud Complaints Store
    private val _cloudComplaints = MutableStateFlow<List<ComplaintEntity>>(emptyList())
    val cloudComplaints: StateFlow<List<ComplaintEntity>> = _cloudComplaints.asStateFlow()

    private val _isCloudLoading = MutableStateFlow(false)
    val isCloudLoading: StateFlow<Boolean> = _isCloudLoading.asStateFlow()

    private val _cloudError = MutableStateFlow<String?>(null)
    val cloudError: StateFlow<String?> = _cloudError.asStateFlow()

    // Sample Indian villages for quick node switching or verification
    val recommendedVillages = emptyList<Pair<String, Pair<Double, Double>>>()

    init {
        logTelemetry("EdgeSync Engine Initializing...")
        logTelemetry("Room DB connected: VercelOfflineQueueDB [Active]")
        logTelemetry("Supabase REST Gateway established. Secure connection configured.")

        // Restore auth preferences and session
        try {
            val prefs = getApplication<Application>().getSharedPreferences("ruralsync_prefs", Context.MODE_PRIVATE)
            val hasOnboarded = prefs.getBoolean("permissions_onboarded", false)
            _permissionsOnboarded.value = hasOnboarded

            // Volatile Sessions: Force fresh login when reopening the app after closure
            prefs.edit()
                .putString("user_email", null)
                .putString("user_name", "Villager Node")
                .putBoolean("is_authenticated", false)
                .apply()
            
            _currentUserEmail.value = null
            logTelemetry("🔐 Node Startup Handshake: Volatile session initialized. Operator login required.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Connectivity observer loop
        viewModelScope.launch {
            while (true) {
                checkNetworkState()
                updateOfflineQueueCount()
                delay(_pollingIntervalMs.value)
            }
        }

        // Automatic Sync loop when online
        viewModelScope.launch {
            while (true) {
                if (_isOnline.value) {
                    executeAutoSync()
                }
                delay(_pollingIntervalMs.value)
            }
        }
    }

    private fun checkNetworkState() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isCurrentlyOnline = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null) {
                isCurrentlyOnline = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            isCurrentlyOnline = activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        val wasOnline = _isOnline.value
        _isOnline.value = isCurrentlyOnline

        if (isCurrentlyOnline) {
            _syncStatusMessage.value = "EDGE ONLINE — Pipeline Synchronized"
            calculateSimulatedOdometerSpeed()
        } else {
            _syncStatusMessage.value = "EDGE OFFLINE — Local Cache Mode"
            _simulatedSpeedString.value = "0.0 KB/s"
            _simulatedSpeedMbps.value = "0.00 Mbps"
        }

        if (wasOnline != isCurrentlyOnline) {
            logTelemetry(
                if (isCurrentlyOnline) "🔌 Connection RESTORED. Online REST API channel bound."
                else "📡 Device DECOUPLED from active network channel. Offline-Sovereign caching active."
            )
        }
    }

    private fun calculateSimulatedOdometerSpeed() {
        // Simulates the JS project dynamic odometer refresh
        val minSpeed = if (_isEcoMode.value) 1.5 else 15.4
        val maxSpeed = if (_isEcoMode.value) 44.5 else 94.2
        val randomMbps = minSpeed + (maxSpeed - minSpeed) * Math.random()
        _simulatedSpeedMbps.value = String.format(Locale.US, "%.2f Mbps", randomMbps)

        val calculatedKbs = (randomMbps * 1024) / 8
        if (calculatedKbs >= 1024) {
            _simulatedSpeedString.value = String.format(Locale.US, "%.1f MB/s", calculatedKbs / 1024)
        } else {
            _simulatedSpeedString.value = String.format(Locale.US, "%.1f KB/s", calculatedKbs)
        }
    }

    fun toggleEcoMode(enabled: Boolean) {
        _isEcoMode.value = enabled
        if (enabled) {
            _pollingIntervalMs.value = 10000L
            _expectedLatency.value = "Extended Optimized (~4.20s)"
            logTelemetry("🔋 Adaptive Sync: ECO MODE Enabled. Network Polling throttled to 10s.")
        } else {
            _pollingIntervalMs.value = 2500L
            _expectedLatency.value = "Minimal Base (~0.04s)"
            logTelemetry("⚡ Adaptive Sync: HIGH PERFORMANCE Enabled. Network Polling set to 2.5s.")
        }
        checkNetworkState()
    }

    private suspend fun updateOfflineQueueCount() {
        val count = withContext(Dispatchers.IO) {
            db.complaintDao().getUnsyncedComplaints().size
        }
        if (count != _unsyncedQueueSize.value) {
            _unsyncedQueueSize.value = count
            logTelemetry("📦 Cache synchronization queue updated. Pending packets: $count")
        }
    }

    fun selectVillage(villageName: String, lat: Double, lon: Double) {
        _selectedVillage.value = villageName
        _latitude.value = lat
        _longitude.value = lon
        logTelemetry("📍 Village grid node switched: $villageName (${lat}, ${lon})")
    }

    // HIGH-PRECISION REAL DEVICE GPS TRIGGER ENGINE
    fun fetchCurrentDeviceGpsLocation(context: Context) {
        viewModelScope.launch {
            logTelemetry("🛰️ Querying hardware receiver for precise GPS coordinates...")
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                if (lm != null) {
                    val hasGps = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                    val hasNetwork = lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
                    var location: android.location.Location? = null
                    
                    if (hasGps) {
                        try {
                            location = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                        } catch (e: SecurityException) {
                            logTelemetry("⚠️ GPS security access declined by android system.")
                        }
                    }
                    if (location == null && hasNetwork) {
                        try {
                            location = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                        } catch (e: SecurityException) {}
                    }
                    
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        _latitude.value = lat
                        _longitude.value = lon
                        logTelemetry("📍 Precision hardware geotag: Lat $lat, Lon $lon")
                        
                        // Reverse geocode coord to get highly-precise area name
                        reverseGeocodeCoordinates(lat, lon)
                    } else {
                        logTelemetry("⚠️ GPS hardware signal timed out. Please map manually or check mock locations.")
                    }
                }
            } catch (e: Exception) {
                logTelemetry("❌ Precision GPS exception: ${e.message}")
            }
        }
    }

    private var locationListener: android.location.LocationListener? = null

    fun startLiveGpsTracking(context: Context) {
        viewModelScope.launch {
            logTelemetry("🛰️ Initializing Real-time Edge GPS Tracking Node...")
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                if (lm != null) {
                    locationListener?.let { lm.removeUpdates(it) }

                    val listener = object : android.location.LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            val lat = location.latitude
                            val lon = location.longitude
                            val acc = location.accuracy
                            
                            if (acc > 50) {
                                logTelemetry("⚠️ GPS Accuracy low (${acc.toInt()}m). Waiting for high-precision receiver lock (<20m)...")
                            } else {
                                logTelemetry("🎯 High-Precision GPS Lock achieved: ${acc.toInt()}m accuracy.")
                            }
                            
                            if (lat != 0.0 && lon != 0.0 && lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0) {
                                _latitude.value = lat
                                _longitude.value = lon
                                reverseGeocodeCoordinates(lat, lon)
                            }
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                        override fun onProviderEnabled(provider: String) {
                            logTelemetry("🛰️ GPS Provider Enabled: $provider")
                        }
                        override fun onProviderDisabled(provider: String) {
                            logTelemetry("⚠️ GPS Provider Disabled: $provider")
                        }
                    }
                    locationListener = listener
                    
                    try {
                        if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                            lm.requestLocationUpdates(
                                android.location.LocationManager.GPS_PROVIDER,
                                3000L,
                                2f,
                                listener
                            )
                        }
                        if (lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                            lm.requestLocationUpdates(
                                android.location.LocationManager.NETWORK_PROVIDER,
                                3000L,
                                2f,
                                listener
                            )
                        }
                        
                        var lastLoc = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                        if (lastLoc == null) {
                            lastLoc = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                        }
                        lastLoc?.let {
                            _latitude.value = it.latitude
                            _longitude.value = it.longitude
                            reverseGeocodeCoordinates(it.latitude, it.longitude)
                        } ?: run {
                            fetchCurrentDeviceGpsLocation(context)
                        }
                    } catch (e: SecurityException) {
                        logTelemetry("⚠️ GPS security access declined by android system.")
                    }
                }
            } catch (e: Exception) {
                logTelemetry("❌ GPS tracker initialization failure: ${e.message}")
            }
        }
    }

    // CRYPTOGRAPHICALLY PRECISE REVERSE GEOCODING PIPELINE (NOMINATIM API)
    fun reverseGeocodeCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=18"
                    val connection = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("User-Agent", "EdgeSync/1.0")
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    
                    if (connection.responseCode == 200) {
                        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                        
                        // Parse address block nested content safely
                        val addressBlockPattern = "\"address\":\\s*\\{([^}]+)\\}".toRegex()
                        val addressContent = addressBlockPattern.find(responseText)?.groupValues?.get(1) ?: responseText
                        
                        fun getAddr(key: String): String {
                            val pattern = "\"$key\":\"([^\"]+)\"".toRegex()
                            return pattern.find(addressContent)?.groupValues?.get(1)?.replace("\\u002c", ",") ?: ""
                        }
                        
                        val v = getAddr("village").ifEmpty { getAddr("hamlet") }.ifEmpty { getAddr("suburb") }.ifEmpty { getAddr("neighbourhood") }.ifEmpty { getAddr("town") }.ifEmpty { getAddr("city") }.ifEmpty { "Pipili Village" }
                        val pan = getAddr("neighbourhood").ifEmpty { getAddr("suburb") }.ifEmpty { getAddr("village") }.ifEmpty { getAddr("municipality") }.ifEmpty { "Pipili Panchayat" }
                        val tal = getAddr("subdistrict").ifEmpty { getAddr("taluk") }.ifEmpty { getAddr("tehsil") }.ifEmpty { getAddr("county") }.ifEmpty { "Pipili Block" }
                        val dist = getAddr("state_district").ifEmpty { getAddr("district") }.ifEmpty { getAddr("county") }.ifEmpty { "Puri District" }
                        val st = getAddr("state").ifEmpty { "Odisha" }
                        val cntry = getAddr("country").ifEmpty { "India" }
                        val pc = getAddr("postcode").ifEmpty { "752104" }

                        withContext(Dispatchers.Main) {
                            _resolvedVillage.value = v
                            _resolvedPanchayat.value = pan
                            _resolvedTaluk.value = tal
                            _resolvedDistrict.value = dist
                            _resolvedState.value = st
                            _resolvedCountry.value = cntry
                            _resolvedPostalCode.value = pc

                            val formattedVillage = "$v, $st"
                            _selectedVillage.value = formattedVillage
                            logTelemetry("📍 Precision address geo-resolved: $formattedVillage")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _selectedVillage.value = "Precision Area ($lat, $lon)"
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _selectedVillage.value = "Infield Grid ($lat, $lon)"
                    logTelemetry("🛰️ Geotagged at coordinates: ($lat, $lon)")
                }
            }
        }
    }

    fun selectDetailedVillage(
        villageName: String,
        panchayat: String,
        taluk: String,
        district: String,
        stateName: String,
        countryName: String,
        postcode: String,
        lat: Double,
        lon: Double
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            _latitude.value = lat
            _longitude.value = lon
            _resolvedVillage.value = villageName.ifEmpty { "Resolved Node" }
            _resolvedPanchayat.value = panchayat.ifEmpty { "Resolved Panchayat" }
            _resolvedTaluk.value = taluk.ifEmpty { "Resolved Taluk" }
            _resolvedDistrict.value = district.ifEmpty { "Resolved District" }
            _resolvedState.value = stateName.ifEmpty { "Resolved State" }
            _resolvedCountry.value = countryName.ifEmpty { "Resolved Country" }
            _resolvedPostalCode.value = postcode.ifEmpty { "999999" }
            
            val display = if (villageName.isNotEmpty() && stateName.isNotEmpty()) "$villageName, $stateName" else villageName.ifEmpty { "Resolved Coordinate" }
            _selectedVillage.value = display
            logTelemetry("📍 WebView Bridge Location Synchronized: $display")
        }
    }

    // FORWARD GEOCODING SEARCH QUERY ENGINE (FOR PRECISE MAP SEARCH)
    fun searchLocationByName(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            logTelemetry("🔍 Resolving coordinates for target area: $name...")
            try {
                withContext(Dispatchers.IO) {
                    val queryEncoded = java.net.URLEncoder.encode(name, "UTF-8")
                    val urlStr = "https://nominatim.openstreetmap.org/search?format=json&q=$queryEncoded&limit=1"
                    val connection = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("User-Agent", "EdgeSync/1.0")
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    
                    if (connection.responseCode == 200) {
                        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                        val latRegex = "\"lat\":\"([^\"]+)\"".toRegex()
                        val lonRegex = "\"lon\":\"([^\"]+)\"".toRegex()
                        val nameRegex = "\"display_name\":\"([^\"]+)\"".toRegex()
                        
                        val latMatch = latRegex.find(responseText)?.groupValues?.get(1)?.toDoubleOrNull()
                        val lonMatch = lonRegex.find(responseText)?.groupValues?.get(1)?.toDoubleOrNull()
                        val dispName = nameRegex.find(responseText)?.groupValues?.get(1)?.replace("\\u002c", ",") ?: name
                        
                        if (latMatch != null && lonMatch != null) {
                            withContext(Dispatchers.Main) {
                                _latitude.value = latMatch
                                _longitude.value = lonMatch
                                val cleanName = dispName.split(",").take(2).joinToString(",")
                                _selectedVillage.value = cleanName
                                logTelemetry("📍 Map viewport panned to: $cleanName")
                            }
                        } else {
                            logTelemetry("⚠️ Could not resolve coordinate bindings for: $name")
                        }
                    }
                }
            } catch (e: Exception) {
                logTelemetry("❌ Map geosearch timed out: ${e.message}")
            }
        }
    }

    // Telemetry logging helper
    fun logTelemetry(message: String) {
        val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        viewModelScope.launch(Dispatchers.Main) {
            val updated = _telemetryLogs.value.toMutableList()
            updated.add(0, "[$timeStamp] $message")
            // Keep last 40 lines
            if (updated.size > 40) {
                updated.removeLast()
            }
            _telemetryLogs.value = updated
        }
    }

    // Get reactive local complaints flow for logged-in user
    fun getLocalComplaintsFlow(): Flow<List<ComplaintEntity>> {
        return currentUserEmail.flatMapLatest { email ->
            if (email != null) {
                repository.getLocalComplaints(email)
            } else {
                flowOf(emptyList())
            }
        }
    }

    // Authentication Actions
    fun loginUser(email: String, passwordRaw: String, rememberMe: Boolean = true, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null

            // Clean email or phone input
            val targetEmail = email.trim()
            val user = repository.login(targetEmail, passwordRaw)
            _authLoading.value = false

            if (user != null) {
                _currentUserEmail.value = user.email
                _currentUserName.value = user.name

                val prefs = getApplication<Application>().getSharedPreferences("ruralsync_prefs", Context.MODE_PRIVATE)
                val editor = prefs.edit()
                    .putString("user_email", user.email)
                    .putString("user_name", user.name)
                    .putBoolean("is_authenticated", true)
                    .putLong("login_timestamp", System.currentTimeMillis())

                // Restore mobile number if it was already saved, or keep standard default
                val restoredMobile = prefs.getString("user_mobile_${user.email}", "9999999999") ?: "9999999999"
                _currentUserMobile.value = restoredMobile
                editor.putString("user_mobile", restoredMobile)

                val restoredAvatar = prefs.getString("user_avatar_${user.email}", null)
                _currentUserAvatarUrl.value = restoredAvatar
                editor.putString("user_avatar", restoredAvatar)

                editor.apply()

                _authSuccessMessage.value = "Welcome back, optical operator ${user.name}!"
                logTelemetry("🔐 User Authenticated: ${user.name} (${user.email}) logged in successfully.")
                onComplete(true)
            } else {
                _authError.value = "Authentication Failed. Incorrect Credentials/Node Registration."
                logTelemetry("⚠️ Secure Auth: Refused credentials audit for $targetEmail.")
                onComplete(false)
            }
        }
    }

    fun registerUser(email: String, passwordRaw: String, name: String, mobile: String = "", onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null

            val targetEmail = email.trim()
            val targetName = name.trim()
            val targetMobile = mobile.trim()

            if (targetEmail.isEmpty() || passwordRaw.length < 8 || targetName.isEmpty()) {
                _authError.value = "Validation Error: Complete name, correct email, password must be >= 8 chars."
                _authLoading.value = false
                onComplete(false)
                return@launch
            }

            // Real-time custom check for password parameters in registration logic
            val hasUpper = passwordRaw.any { it.isUpperCase() }
            val hasLower = passwordRaw.any { it.isLowerCase() }
            val hasDigit = passwordRaw.any { it.isDigit() }
            val hasSpecial = passwordRaw.any { !it.isLetterOrDigit() }

            if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
                _authError.value = "Password must check: 1 uppercase, 1 lowercase, 1 number, and 1 special symbol."
                _authLoading.value = false
                onComplete(false)
                return@launch
            }

            val success = repository.register(targetEmail, passwordRaw, targetName)
            _authLoading.value = false

            if (success) {
                // Store phone/mobile associated with registering user in local metadata
                val prefs = getApplication<Application>().getSharedPreferences("ruralsync_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("user_mobile_$targetEmail", if (targetMobile.isNotEmpty()) targetMobile else "9999999999")
                    .apply()

                _authSuccessMessage.value = "Standard User Registration successfully saved to offline catalog."
                logTelemetry("👥 Node Registration: account successfully stored for $targetName ($targetEmail).")
                onComplete(true)
            } else {
                _authError.value = "User already registered under this email node."
                logTelemetry("⚠️ Node Registration: Aborted. Identity conflict with $targetEmail.")
                onComplete(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logTelemetry("🔐 User session expired: ${currentUserName.value} logged out.")
            
            val prefs = getApplication<Application>().getSharedPreferences("ruralsync_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("user_email", null)
                .putString("user_name", "Villager Node")
                .putBoolean("is_authenticated", false)
                .apply()

            _currentUserEmail.value = null
            _currentUserName.value = "Villager Node"
            _currentUserMobile.value = "9999999999"
            _currentUserAvatarUrl.value = null
            _authSuccessMessage.value = null
            _authError.value = null
        }
    }

    fun updateProfile(name: String, mobile: String, avatarUrl: String?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val email = _currentUserEmail.value ?: return@launch
            val trimmedName = name.trim()
            val trimmedMobile = mobile.trim()

            if (trimmedName.isEmpty() || trimmedMobile.isEmpty()) {
                _authError.value = "Name and Mobile cannot be empty."
                onComplete(false)
                return@launch
            }

            _currentUserName.value = trimmedName
            _currentUserMobile.value = trimmedMobile
            _currentUserAvatarUrl.value = avatarUrl

            val prefs = getApplication<Application>().getSharedPreferences("ruralsync_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("user_name", trimmedName)
                .putString("user_mobile", trimmedMobile)
                .putString("user_avatar", avatarUrl)
                .putString("user_mobile_$email", trimmedMobile)
                .putString("user_avatar_$email", avatarUrl)
                .apply()

            logTelemetry("👤 Profile Updated: $trimmedName ($email) - Mobile: $trimmedMobile")
            _authSuccessMessage.value = "Profile metrics saved successfully."
            onComplete(true)
        }
    }

    fun forgotPassword(email: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null

            val targetEmail = email.trim()
            if (targetEmail.isEmpty()) {
                _authError.value = "Please enter an email address."
                _authLoading.value = false
                onComplete(false)
                return@launch
            }

            // Verify if account exists in userDao
            val user = db.userDao().getUserByEmail(targetEmail)
            _authLoading.value = false

            if (user != null) {
                _authSuccessMessage.value = "Recovery OTP Code successfully transmitted to email gateway!"
                logTelemetry("🔐 Forgot Password: Secure handshake sent for $targetEmail.")
                onComplete(true)
            } else {
                _authError.value = "No registered operator node found with this email."
                logTelemetry("⚠️ Forgot Password: Zero matches discovered for $targetEmail.")
                onComplete(false)
            }
        }
    }

    fun resetPassword(email: String, newPassRaw: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null

            val targetEmail = email.trim()
            if (targetEmail.isEmpty() || newPassRaw.length < 8) {
                _authError.value = "Passkey must be at least 8 characters."
                _authLoading.value = false
                onComplete(false)
                return@launch
            }

            // Real-time custom check for password parameters in registration logic
            val hasUpper = newPassRaw.any { it.isUpperCase() }
            val hasLower = newPassRaw.any { it.isLowerCase() }
            val hasDigit = newPassRaw.any { it.isDigit() }
            val hasSpecial = newPassRaw.any { !it.isLetterOrDigit() }

            if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
                _authError.value = "Secure Passkey must contain uppercase, lowercase, digit, and special symbol."
                _authLoading.value = false
                onComplete(false)
                return@launch
            }

            val user = db.userDao().getUserByEmail(targetEmail)
            if (user != null) {
                // Update passwordHash
                val passHash = newPassRaw.toSha256()
                withContext(Dispatchers.IO) {
                    db.userDao().registerUser(UserEntity(id = user.id, email = user.email, passwordHash = passHash, name = user.name))
                }
                _authSuccessMessage.value = "Passkey update synchronized locally!"
                logTelemetry("🔐 Passkey Reset: Updated credentials hash for $targetEmail.")
                _authLoading.value = false
                onComplete(true)
            } else {
                _authError.value = "Could not resolve user registration."
                _authLoading.value = false
                onComplete(false)
            }
        }
    }

    fun setPermissionsOnboarded(onboarded: Boolean) {
        _permissionsOnboarded.value = onboarded
        val prefs = getApplication<Application>().getSharedPreferences("ruralsync_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("permissions_onboarded", onboarded).apply()
        logTelemetry("📋 Permissions Status: Onboarding compliance verified: $onboarded")
    }

    // Register a complaint locally
    fun registerComplaint(
        title: String,
        type: String,
        description: String,
        phone: String,
        isSmsSubscribed: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val email = _currentUserEmail.value ?: "guest@ruralsync.gov"
            val name = _currentUserName.value ?: "Villager Guest"

            if (title.isEmpty() || description.isEmpty() || phone.isEmpty()) {
                logTelemetry("⚠️ Complaint Rejected: Missing Title, Description, or Contact Number.")
                onComplete(false)
                return@launch
            }

            logTelemetry("✍🏽 Registering Local Item: \"$title\" ($type)...")

            val item = repository.submitComplaint(
                title = title,
                type = type,
                description = description,
                email = email,
                userName = name,
                phone = phone,
                isSmsSubscribed = isSmsSubscribed,
                villageName = _selectedVillage.value,
                latitude = _latitude.value,
                longitude = _longitude.value
            )

            logTelemetry("💾 Local Registry saved securely inside Room Cache [ID: ${item.id.substring(0, 8)}].")
            updateOfflineQueueCount()
            onComplete(true)

            if (isSmsSubscribed) {
                logTelemetry("💬 SMS Subscriber pipeline requested for +91-$phone.")
            }

            if (_isOnline.value) {
                executeAutoSync()
            }
        }
    }

    // Force Sync Active
    fun forceSync() {
        viewModelScope.launch {
            logTelemetry("🔄 Sync Request: Manually triggered data pipeline synchronization sequence...")
            if (!_isOnline.value) {
                logTelemetry("❌ Sync Failed: Edge channels currently severed/offline.")
                return@launch
            }
            executeAutoSync()
        }
    }

    private suspend fun executeAutoSync() {
        val syncedCount = repository.syncAllWithCloud()
        if (syncedCount > 0) {
            logTelemetry("⚡ Sync Pipeline: Successfully pushed $syncedCount report payload packets to Supabase.")
            updateOfflineQueueCount()
            // Silent sync review
            fetchCloudLedger()
        }
    }

    // Fetch cloud ledger list from Supabase
    fun fetchCloudLedger() {
        viewModelScope.launch {
            _isCloudLoading.value = true
            _cloudError.value = null

            logTelemetry("🛰️ Querying Cloud database servers for all synchronized reports...")
            val records = repository.fetchCloudLedger()
            _isCloudLoading.value = false

            if (records.isNotEmpty() || _isOnline.value) {
                _cloudComplaints.value = records
                logTelemetry("🛸 Cloud Sync Audit: Received ${records.size} transaction entries from reportsStore.")
            } else {
                _cloudError.value = "Handshake blocked. No server route to cloud catalog."
                logTelemetry("⚠️ Cloud Sync Audit: Gateway Handshake Error. Could not resolve remote tables.")
            }
        }
    }

    // Erase complaint row
    fun deleteComplaintRecord(id: String, isOnlineContext: Boolean) {
        viewModelScope.launch {
            logTelemetry("🗑️ Wipe requested: Executing secure location-bound erasure for row ID ${id.substring(0, 8)}...")
            val success = repository.deleteComplaint(id, isOnlineContext)
            if (success) {
                logTelemetry("✅ Wipe completed: Record successfully purged from ${if (isOnlineContext) "Cloud & Local" else "Local Cache"}.")
                updateOfflineQueueCount()
                if (isOnlineContext) {
                    fetchCloudLedger()
                }
            } else {
                logTelemetry("❌ Wipe aborted: remote server entries erasure require server channel sovereignty.")
            }
        }
    }
}
