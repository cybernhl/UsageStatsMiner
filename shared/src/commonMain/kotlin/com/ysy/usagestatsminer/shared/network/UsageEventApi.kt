package com.ysy.usagestatsminer.shared.network

import com.ysy.usagestatsminer.shared.entity.UsageEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class UsageStatsApi {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun postUsageEvents(events: List<UsageEvent>) {
        httpClient.post("https://xxx.com/usagestats/events") {
            this.setBody(events)
        }
    }
}
