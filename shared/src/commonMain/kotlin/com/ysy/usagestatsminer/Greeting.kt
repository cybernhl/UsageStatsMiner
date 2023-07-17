package com.ysy.usagestatsminer

class Greeting {

    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
