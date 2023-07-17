package com.ysy.usagestatsminer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
