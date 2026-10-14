package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cameraEventManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun cameraEventManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a camera event manifest score falls in. */
fun classifyCameraEventManifest(score: Double): String {
    if (score < 33.9) {
        return "ok"
    }
    if (score < 60.9) {
        return "watch"
    }
    return "critical"
}
