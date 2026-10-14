package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a fallback inspection allocation code in its canonical FIA- form. */
fun normalizeFallbackInspectionAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIA-")) {
        return clean
    }
    return "FIA-${clean.replace(" ", "-")}"
}

fun fallbackInspectionAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback inspection allocation score falls in. */
fun classifyFallbackInspectionAllocation(score: Double): String {
    if (score < 39.2) {
        return "medium"
    }
    if (score < 52.8) {
        return "minimal"
    }
    if (score < 67.2) {
        return "low"
    }
    if (score < 91.9) {
        return "elevated"
    }
    return "critical"
}

fun fallbackInspectionAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
