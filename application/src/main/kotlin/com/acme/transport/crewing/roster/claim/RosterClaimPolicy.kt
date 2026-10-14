package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun rosterClaimBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a roster claim score falls in. */
fun classifyRosterClaim(score: Double): String {
    if (score < 9.7) {
        return "high"
    }
    if (score < 11.7) {
        return "medium"
    }
    if (score < 78.4) {
        return "ok"
    }
    if (score < 83.4) {
        return "low"
    }
    return "minimal"
}

fun mergeRosterClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rosterClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
