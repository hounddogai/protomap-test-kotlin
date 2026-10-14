package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lenientInspectionMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientInspectionMappingBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a lenient inspection mapping code in its canonical LIM- form. */
fun normalizeLenientInspectionMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LIM-")) {
        return clean
    }
    return "LIM-${clean.replace(" ", "-")}"
}

/** Names the band a lenient inspection mapping score falls in. */
fun classifyLenientInspectionMapping(score: Double): String {
    if (score < 36.4) {
        return "ok"
    }
    if (score < 39.5) {
        return "critical"
    }
    if (score < 67.1) {
        return "medium"
    }
    if (score < 80.6) {
        return "watch"
    }
    return "high"
}
