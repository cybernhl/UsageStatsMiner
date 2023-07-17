package com.ysy.usagestatsminer.android.ui.home

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.ysy.usagestatsminer.shared.UsageStatsSDK
import com.ysy.usagestatsminer.shared.cache.DatabaseDriverFactory
import com.ysy.usagestatsminer.shared.entity.UsageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val usageStatsManager by lazy {
        application.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    private val usageStatsSDK by lazy { UsageStatsSDK(DatabaseDriverFactory(application)) }
    private val appInfoCache = mutableMapOf<String, String>()

    suspend fun queryUsageEventsFromSystem(
        beginTime: Long,
        endTime: Long,
        filterPkgs: Set<String> = emptySet()
    ): List<UsageEvent> = withContext(Dispatchers.IO) {
        val result = mutableListOf<UsageEvent>()
        val events = usageStatsManager.queryEvents(beginTime, endTime)
        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (filterPkgs.isEmpty() || filterPkgs.contains(event.packageName)) {
                result.add(
                    UsageEvent(
                        event.timeStamp,
                        event.packageName,
                        event.packageName.toAppName(),
                        event.className,
                        event.eventType
                    ).apply {
                        eventDesc = eventType.toDesc()
                    }
                )
            }
        }
        result.asReversed() // order desc by time
    }

    private fun saveUsageEventsToUserDB(events: List<UsageEvent>) {
        viewModelScope.launch {
            usageStatsSDK.insertUsageEvents(events)
        }
    }

    private fun uploadUsageEventsToServer(events: List<UsageEvent>) {
        viewModelScope.launch {
            usageStatsSDK.postUsageEvents(events, 50)
        }
    }

    // packageName -> appName
    private fun String?.toAppName(): String {
        val key = this ?: ""
        return appInfoCache.getOrDefault(key, "").ifEmpty {
            AppUtils.getAppName(key).also { appInfoCache[key] = it }
        }
    }

    // eventType -> eventDesc
    private fun Int.toDesc() = when (this) {
        UsageEvents.Event.ACTIVITY_RESUMED -> "ACTIVITY_RESUMED"
        UsageEvents.Event.ACTIVITY_PAUSED -> "ACTIVITY_PAUSED"
        3 -> "END_OF_DAY"
        4 -> "CONTINUE_PREVIOUS_DAY"
        UsageEvents.Event.CONFIGURATION_CHANGE -> "CONFIGURATION_CHANGE"
        6 -> "SYSTEM_INTERACTION"
        UsageEvents.Event.USER_INTERACTION -> "USER_INTERACTION"
        UsageEvents.Event.SHORTCUT_INVOCATION -> "SHORTCUT_INVOCATION"
        9 -> "CHOOSER_ACTION"
        10 -> "NOTIFICATION_SEEN"
        UsageEvents.Event.STANDBY_BUCKET_CHANGED -> "STANDBY_BUCKET_CHANGED"
        12 -> "NOTIFICATION_INTERRUPTION"
        13 -> "SLICE_PINNED_PRIV"
        14 -> "SLICE_PINNED"
        UsageEvents.Event.SCREEN_INTERACTIVE -> "SCREEN_INTERACTIVE"
        UsageEvents.Event.SCREEN_NON_INTERACTIVE -> "SCREEN_NON_INTERACTIVE"
        UsageEvents.Event.KEYGUARD_SHOWN -> "KEYGUARD_SHOWN"
        UsageEvents.Event.KEYGUARD_HIDDEN -> "KEYGUARD_HIDDEN"
        UsageEvents.Event.FOREGROUND_SERVICE_START -> "FOREGROUND_SERVICE_START"
        UsageEvents.Event.FOREGROUND_SERVICE_STOP -> "FOREGROUND_SERVICE_STOP"
        21 -> "CONTINUING_FOREGROUND_SERVICE"
        22 -> "ROLLOVER_FOREGROUND_SERVICE"
        UsageEvents.Event.ACTIVITY_STOPPED -> "ACTIVITY_STOPPED"
        24 -> "ACTIVITY_DESTROYED"
        25 -> "FLUSH_TO_DISK"
        UsageEvents.Event.DEVICE_SHUTDOWN -> "DEVICE_SHUTDOWN"
        UsageEvents.Event.DEVICE_STARTUP -> "DEVICE_STARTUP"
        28 -> "USER_UNLOCKED"
        29 -> "USER_STOPPED"
        30 -> "LOCUS_ID_SET"
        31 -> "APP_COMPONENT_USED"
        else -> "UNKNOWN"
    }
}
