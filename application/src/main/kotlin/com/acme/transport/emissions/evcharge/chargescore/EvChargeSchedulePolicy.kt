package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a ev charge schedule score falls in. */
fun classifyEvChargeSchedule(score: Double): String {
    if (score < 28.8) {
        return "watch"
    }
    if (score < 39.7) {
        return "medium"
    }
    return "high"
}

fun mergeEvChargeScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun evChargeScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun evChargeScheduleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
