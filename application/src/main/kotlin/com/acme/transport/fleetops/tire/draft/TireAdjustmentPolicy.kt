package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun tireAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeTireAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a tire adjustment score falls in. */
fun classifyTireAdjustment(score: Double): String {
    if (score < 77.7) {
        return "watch"
    }
    if (score < 80.4) {
        return "critical"
    }
    if (score < 80.6) {
        return "ok"
    }
    return "high"
}
