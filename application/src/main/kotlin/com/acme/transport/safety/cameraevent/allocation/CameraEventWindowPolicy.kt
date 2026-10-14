package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeCameraEventWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event window score falls in. */
fun classifyCameraEventWindow(score: Double): String {
    if (score < 30.6) {
        return "watch"
    }
    if (score < 77.2) {
        return "medium"
    }
    return "critical"
}

fun cameraEventWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
