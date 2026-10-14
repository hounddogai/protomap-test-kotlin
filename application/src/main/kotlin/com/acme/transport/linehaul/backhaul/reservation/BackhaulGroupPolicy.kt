package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeBackhaulGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun backhaulGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a backhaul group score falls in. */
fun classifyBackhaulGroup(score: Double): String {
    if (score < 14.6) {
        return "low"
    }
    if (score < 30.6) {
        return "elevated"
    }
    if (score < 57.3) {
        return "critical"
    }
    return "watch"
}

fun backhaulGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
