package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun scheduledCameraEventSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled camera event snapshot score falls in. */
fun classifyScheduledCameraEventSnapshot(score: Double): String {
    if (score < 1.3) {
        return "ok"
    }
    if (score < 6.4) {
        return "watch"
    }
    if (score < 29.9) {
        return "high"
    }
    if (score < 87.0) {
        return "critical"
    }
    return "low"
}

fun mergeScheduledCameraEventSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
