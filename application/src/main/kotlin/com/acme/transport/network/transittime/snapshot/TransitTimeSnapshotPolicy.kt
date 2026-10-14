package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun transitTimeSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeTransitTimeSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
