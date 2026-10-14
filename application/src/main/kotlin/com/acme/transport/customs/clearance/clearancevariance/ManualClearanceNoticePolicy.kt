package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun manualClearanceNoticeBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeManualClearanceNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualClearanceNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a manual clearance notice score falls in. */
fun classifyManualClearanceNotice(score: Double): String {
    if (score < 25.6) {
        return "medium"
    }
    if (score < 45.9) {
        return "critical"
    }
    if (score < 69.7) {
        return "minimal"
    }
    if (score < 90.2) {
        return "ok"
    }
    return "high"
}
