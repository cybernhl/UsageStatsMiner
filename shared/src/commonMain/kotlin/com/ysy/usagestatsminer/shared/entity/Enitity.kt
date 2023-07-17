package com.ysy.usagestatsminer.shared.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsageEvent(
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("app_name")
    val appName: String?,
    @SerialName("class_name")
    val className: String?,
    @SerialName("event_type")
    val eventType: Int
) {
    @SerialName("event_desc")
    var eventDesc: String? = null
}
