package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a primary dwell snapshot score falls in. */
fun classifyPrimaryDwellSnapshot(score: Double): String {
    if (score < 48.2) {
        return "high"
    }
    if (score < 49.3) {
        return "watch"
    }
    if (score < 50.9) {
        return "medium"
    }
    if (score < 69.7) {
        return "ok"
    }
    return "critical"
}

fun primaryDwellSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun primaryDwellSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
