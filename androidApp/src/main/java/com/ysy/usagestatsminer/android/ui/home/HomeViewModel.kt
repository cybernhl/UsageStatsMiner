package com.ysy.usagestatsminer.android.ui.home

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils
import com.ysy.usagestatsminer.android.ui.model.HomeListItem
import com.ysy.usagestatsminer.shared.UsageStatsSDK
import com.ysy.usagestatsminer.shared.cache.DatabaseDriverFactory
import com.ysy.usagestatsminer.shared.entity.UsageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val usageEventsLD = MutableLiveData<List<HomeListItem>>()
    val eventDescs = arrayOf(
        "ACTIVITY_RESUMED",
        "ACTIVITY_PAUSED",
        "END_OF_DAY",
        "CONTINUE_PREVIOUS_DAY",
        "CONFIGURATION_CHANGE",
        "SYSTEM_INTERACTION",
        "USER_INTERACTION",
        "SHORTCUT_INVOCATION",
        "CHOOSER_ACTION",
        "NOTIFICATION_SEEN",
        "STANDBY_BUCKET_CHANGED",
        "NOTIFICATION_INTERRUPTION",
        "SLICE_PINNED_PRIV",
        "SLICE_PINNED",
        "SCREEN_INTERACTIVE",
        "SCREEN_NON_INTERACTIVE",
        "KEYGUARD_SHOWN",
        "KEYGUARD_HIDDEN",
        "FOREGROUND_SERVICE_START",
        "FOREGROUND_SERVICE_STOP",
        "CONTINUING_FOREGROUND_SERVICE",
        "ROLLOVER_FOREGROUND_SERVICE",
        "ACTIVITY_STOPPED",
        "ACTIVITY_DESTROYED",
        "FLUSH_TO_DISK",
        "DEVICE_SHUTDOWN",
        "DEVICE_STARTUP",
        "USER_UNLOCKED",
        "USER_STOPPED",
        "LOCUS_ID_SET",
        "APP_COMPONENT_USED"
    )
    val filterEventTypes = mutableMapOf<Int, Boolean>()

    var appNames: List<String> = emptyList()
    val appPkgs by lazy {
        AppUtils.getAppsInfo()
            .filterNot { it.isSystem }
            .sortedBy { it.name }
            .map { it.packageName }
            .toMutableList().apply {
                add(0, "android")
            }.also {
                appNames = it.map { s -> s.toAppName() }
            }
    }
    val filterPkgs = mutableMapOf<String, Boolean>()

    private val usageStatsManager by lazy {
        application.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    private val usageStatsSDK by lazy { UsageStatsSDK(DatabaseDriverFactory(application)) }
    private val appInfoCache = mutableMapOf<String, String>()

    fun queryUsageEventsFromSystem(
        beginTime: Long,
        endTime: Long
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (filterPkgs.isEmpty()) {
            appPkgs.forEach { filterPkgs[it] = true }
        }
        val result = mutableListOf<UsageEvent>()
        val events = usageStatsManager.queryEvents(beginTime, endTime)
        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (!filterEventTypes.getOrDefault(event.eventType - 1, true)) continue
            if (filterPkgs.getOrDefault(event.packageName, false)) {
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
        // order desc by time
        usageEventsLD.postValue(result.asReversed().map {
            HomeListItem(
                pkgName = it.packageName,
                text = "${it.timestamp.toDateTime()} [${it.appName}] ${it.packageName}/${
                    it.className?.replaceFirst(it.packageName, "")
                } -> ${it.eventType.toDesc()}"
            )
        })
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

    // timestamp -> dateTimeStr
    private fun Long.toDateTime() = if (this == 0L) "--" else TimeUtils.date2String(Date(this))

    // packageName -> appName
    fun String?.toAppName(): String {
        val key = this ?: ""
        return appInfoCache.getOrDefault(key, "").ifEmpty {
            AppUtils.getAppName(key).also { appInfoCache[key] = it }
        }
    }

    // eventType -> eventDesc
    private fun Int.toDesc() = if (this in 1..31) eventDescs[this - 1] else "UNKNOWN"
}
