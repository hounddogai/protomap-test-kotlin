package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun scheduledTrailerYardEntryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun scheduledTrailerYardEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a scheduled trailer yard entry score falls in. */
fun classifyScheduledTrailerYardEntry(score: Double): String {
    if (score < 4.4) {
        return "watch"
    }
    if (score < 14.0) {
        return "minimal"
    }
    if (score < 21.0) {
        return "elevated"
    }
    return "low"
}

fun mergeScheduledTrailerYardEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
