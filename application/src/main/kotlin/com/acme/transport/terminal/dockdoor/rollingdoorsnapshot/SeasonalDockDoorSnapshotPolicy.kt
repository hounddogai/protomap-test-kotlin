package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalDockDoorSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal dock door snapshot score falls in. */
fun classifySeasonalDockDoorSnapshot(score: Double): String {
    if (score < 80.9) {
        return "watch"
    }
    if (score < 95.6) {
        return "low"
    }
    return "elevated"
}
