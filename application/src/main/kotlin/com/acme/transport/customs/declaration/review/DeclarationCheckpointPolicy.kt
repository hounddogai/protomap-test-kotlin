package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun declarationCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun declarationCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a declaration checkpoint score falls in. */
fun classifyDeclarationCheckpoint(score: Double): String {
    if (score < 48.3) {
        return "high"
    }
    if (score < 51.3) {
        return "medium"
    }
    if (score < 73.0) {
        return "elevated"
    }
    if (score < 96.2) {
        return "critical"
    }
    return "ok"
}
