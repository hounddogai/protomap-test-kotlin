package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a manual locker preference score falls in. */
fun classifyManualLockerPreference(score: Double): String {
    if (score < 16.5) {
        return "ok"
    }
    if (score < 19.8) {
        return "watch"
    }
    if (score < 51.9) {
        return "critical"
    }
    return "elevated"
}

fun mergeManualLockerPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualLockerPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
