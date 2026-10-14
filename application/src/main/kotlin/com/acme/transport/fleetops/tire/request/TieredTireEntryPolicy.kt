package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun tieredTireEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun mergeTieredTireEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
