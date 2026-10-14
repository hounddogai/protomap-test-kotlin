package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledDockDoorRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled dock door record score falls in. */
fun classifyScheduledDockDoorRecord(score: Double): String {
    if (score < 71.8) {
        return "critical"
    }
    if (score < 75.0) {
        return "high"
    }
    if (score < 81.9) {
        return "minimal"
    }
    return "ok"
}
