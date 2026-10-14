package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a manual camera event bundle score falls in. */
fun classifyManualCameraEventBundle(score: Double): String {
    if (score < 20.7) {
        return "high"
    }
    if (score < 49.0) {
        return "watch"
    }
    return "low"
}

fun manualCameraEventBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun mergeManualCameraEventBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
