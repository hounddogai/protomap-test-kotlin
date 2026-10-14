package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCameraEventRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event rollup score falls in. */
fun classifyCameraEventRollup(score: Double): String {
    if (score < 20.9) {
        return "low"
    }
    if (score < 45.9) {
        return "minimal"
    }
    return "high"
}
