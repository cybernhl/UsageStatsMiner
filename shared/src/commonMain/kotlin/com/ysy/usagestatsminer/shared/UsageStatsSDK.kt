package com.ysy.usagestatsminer.shared

import com.ysy.usagestatsminer.shared.cache.Database
import com.ysy.usagestatsminer.shared.cache.DatabaseDriverFactory
import com.ysy.usagestatsminer.shared.entity.UsageEvent
import com.ysy.usagestatsminer.shared.network.UsageStatsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UsageStatsSDK(databaseDriverFactory: DatabaseDriverFactory) {

    private val database = Database(databaseDriverFactory)
    private val api = UsageStatsApi()

    suspend fun insertUsageEvents(
        events: List<UsageEvent>
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val maxTimeFromDB = database.getLatestUsageEvent()?.timestamp ?: 0L
            database.insertUsageEvents(events.filter { it.timestamp > maxTimeFromDB })
            true
        }.getOrDefault(false)
    }

    suspend fun postUsageEvents(
        events: List<UsageEvent>,
        batchSize: Int = 10
    ): Boolean = events.chunked(batchSize).all { batch ->
        runCatching {
            api.postUsageEvents(batch)
            true
        }.getOrDefault(false)
    }
}
