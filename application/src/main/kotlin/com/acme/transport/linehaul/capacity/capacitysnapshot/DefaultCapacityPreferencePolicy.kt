package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun defaultCapacityPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a default capacity preference score falls in. */
fun classifyDefaultCapacityPreference(score: Double): String {
    if (score < 68.6) {
        return "ok"
    }
    if (score < 74.8) {
        return "medium"
    }
    return "elevated"
}

fun mergeDefaultCapacityPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun defaultCapacityPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
