package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultDockDoorTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun defaultDockDoorTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a default dock door tier score falls in. */
fun classifyDefaultDockDoorTier(score: Double): String {
    if (score < 6.5) {
        return "elevated"
    }
    if (score < 30.9) {
        return "critical"
    }
    if (score < 43.3) {
        return "low"
    }
    if (score < 59.7) {
        return "watch"
    }
    return "medium"
}
