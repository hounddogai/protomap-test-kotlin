package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun legacyNearMissGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun legacyNearMissGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a legacy near miss group score falls in. */
fun classifyLegacyNearMissGroup(score: Double): String {
    if (score < 12.7) {
        return "critical"
    }
    if (score < 87.7) {
        return "minimal"
    }
    return "ok"
}

fun mergeLegacyNearMissGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
