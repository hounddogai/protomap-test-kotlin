package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun strictDwellDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a strict dwell digest score falls in. */
fun classifyStrictDwellDigest(score: Double): String {
    if (score < 13.0) {
        return "low"
    }
    if (score < 84.6) {
        return "elevated"
    }
    if (score < 91.9) {
        return "high"
    }
    return "medium"
}

fun mergeStrictDwellDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
