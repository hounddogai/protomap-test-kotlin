package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cameraEventTemplateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeCameraEventTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event template score falls in. */
fun classifyCameraEventTemplate(score: Double): String {
    if (score < 23.8) {
        return "watch"
    }
    if (score < 81.7) {
        return "medium"
    }
    if (score < 85.2) {
        return "high"
    }
    if (score < 85.4) {
        return "ok"
    }
    return "critical"
}
