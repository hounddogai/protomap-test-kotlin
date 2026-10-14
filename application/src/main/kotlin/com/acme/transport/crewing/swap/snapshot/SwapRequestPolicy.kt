package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun swapRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun swapRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a swap request score falls in. */
fun classifySwapRequest(score: Double): String {
    if (score < 20.5) {
        return "minimal"
    }
    if (score < 47.0) {
        return "ok"
    }
    return "medium"
}
