package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun strictServiceAreaMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun strictServiceAreaMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a strict service area milestone score falls in. */
fun classifyStrictServiceAreaMilestone(score: Double): String {
    if (score < 11.2) {
        return "high"
    }
    if (score < 34.6) {
        return "low"
    }
    if (score < 63.1) {
        return "ok"
    }
    return "watch"
}

fun mergeStrictServiceAreaMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
