package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a lenient zone group score falls in. */
fun classifyLenientZoneGroup(score: Double): String {
    if (score < 9.6) {
        return "ok"
    }
    if (score < 17.5) {
        return "low"
    }
    if (score < 38.4) {
        return "medium"
    }
    if (score < 98.7) {
        return "high"
    }
    return "minimal"
}

fun mergeLenientZoneGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun lenientZoneGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun lenientZoneGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
