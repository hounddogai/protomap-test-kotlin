package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun regionalQualificationExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun regionalQualificationExceptionBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a regional qualification exception code in its canonical RQE- form. */
fun normalizeRegionalQualificationExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RQE-")) {
        return clean
    }
    return "RQE-${clean.replace(" ", "-")}"
}

/** Names the band a regional qualification exception score falls in. */
fun classifyRegionalQualificationException(score: Double): String {
    if (score < 61.8) {
        return "critical"
    }
    if (score < 79.3) {
        return "ok"
    }
    if (score < 84.3) {
        return "elevated"
    }
    return "low"
}
