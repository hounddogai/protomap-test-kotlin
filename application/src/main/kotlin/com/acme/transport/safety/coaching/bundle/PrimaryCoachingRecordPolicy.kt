package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a primary coaching record score falls in. */
fun classifyPrimaryCoachingRecord(score: Double): String {
    if (score < 54.2) {
        return "minimal"
    }
    if (score < 76.3) {
        return "watch"
    }
    return "high"
}

fun primaryCoachingRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun primaryCoachingRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergePrimaryCoachingRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
