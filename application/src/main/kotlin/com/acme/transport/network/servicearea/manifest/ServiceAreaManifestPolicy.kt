package com.acme.transport.network.servicearea.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun serviceAreaManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a service area manifest score falls in. */
fun classifyServiceAreaManifest(score: Double): String {
    if (score < 79.1) {
        return "low"
    }
    if (score < 99.1) {
        return "high"
    }
    return "critical"
}
