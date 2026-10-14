package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun pendingAvailabilityReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun mergePendingAvailabilityReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
