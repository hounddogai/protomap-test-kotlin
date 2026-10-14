package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a manual qualification target code in its canonical MQT- form. */
fun normalizeManualQualificationTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MQT-")) {
        return clean
    }
    return "MQT-${clean.replace(" ", "-")}"
}

fun manualQualificationTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a manual qualification target score falls in. */
fun classifyManualQualificationTarget(score: Double): String {
    if (score < 28.5) {
        return "low"
    }
    if (score < 50.7) {
        return "high"
    }
    if (score < 98.6) {
        return "ok"
    }
    return "medium"
}
