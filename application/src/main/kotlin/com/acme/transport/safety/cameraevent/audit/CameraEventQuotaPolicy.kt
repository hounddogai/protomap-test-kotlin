package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun cameraEventQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeCameraEventQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event quota score falls in. */
fun classifyCameraEventQuota(score: Double): String {
    if (score < 11.0) {
        return "low"
    }
    if (score < 17.3) {
        return "medium"
    }
    if (score < 39.2) {
        return "ok"
    }
    if (score < 81.3) {
        return "high"
    }
    return "watch"
}
