package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeRollingRosterEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rollingRosterEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a rolling roster entry score falls in. */
fun classifyRollingRosterEntry(score: Double): String {
    if (score < 47.4) {
        return "watch"
    }
    if (score < 56.5) {
        return "medium"
    }
    return "critical"
}
