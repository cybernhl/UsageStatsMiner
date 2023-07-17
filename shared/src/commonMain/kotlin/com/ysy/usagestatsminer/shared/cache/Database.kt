package com.ysy.usagestatsminer.shared.cache

import com.ysy.usagestatsminer.shared.entity.UsageEvent

internal class Database(databaseDriverFactory: DatabaseDriverFactory) {

    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun insertUsageEvents(events: List<UsageEvent>) {
        dbQuery.transaction {
            events.forEach { event ->
                dbQuery.insertUsageEvent(
                    event.timestamp,
                    event.packageName,
                    event.appName,
                    event.className,
                    event.eventType.toLong()
                )
            }
        }
    }

    internal fun clearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllUsageEvents()
        }
    }

    internal fun getAllUsageEvents(): List<UsageEvent> =
        dbQuery.selectAllUsageEvents { timestamp, packageName, appName, className, eventType ->
            UsageEvent(timestamp, packageName, appName, className, eventType.toInt())
        }.executeAsList()

    internal fun getLatestUsageEvent(): UsageEvent? =
        dbQuery.selectLatestUsageEvent { timestamp, packageName, appName, className, eventType ->
            UsageEvent(timestamp, packageName, appName, className, eventType.toInt())
        }.executeAsOneOrNull()
}
