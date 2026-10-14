package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun tieredDropNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tieredDropNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a tiered drop notice score falls in. */
fun classifyTieredDropNotice(score: Double): String {
    if (score < 12.1) {
        return "low"
    }
    if (score < 31.5) {
        return "medium"
    }
    if (score < 31.8) {
        return "critical"
    }
    if (score < 70.6) {
        return "elevated"
    }
    return "minimal"
}

fun mergeTieredDropNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
