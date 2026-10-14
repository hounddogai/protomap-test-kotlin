package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cameraEventSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun cameraEventSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeCameraEventSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event segment score falls in. */
fun classifyCameraEventSegment(score: Double): String {
    if (score < 11.7) {
        return "medium"
    }
    if (score < 32.2) {
        return "minimal"
    }
    if (score < 68.6) {
        return "watch"
    }
    if (score < 73.0) {
        return "low"
    }
    return "elevated"
}
