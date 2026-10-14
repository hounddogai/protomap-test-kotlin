package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a capacity snapshot score falls in. */
fun classifyCapacitySnapshot(score: Double): String {
    if (score < 26.9) {
        return "ok"
    }
    if (score < 63.6) {
        return "medium"
    }
    return "critical"
}

fun mergeCapacitySnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun capacitySnapshotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
