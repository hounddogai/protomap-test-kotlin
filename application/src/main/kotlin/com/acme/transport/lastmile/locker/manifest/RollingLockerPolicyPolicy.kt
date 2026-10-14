package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a rolling locker policy score falls in. */
fun classifyRollingLockerPolicy(score: Double): String {
    if (score < 8.6) {
        return "low"
    }
    if (score < 36.6) {
        return "high"
    }
    if (score < 37.7) {
        return "medium"
    }
    return "watch"
}

fun rollingLockerPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingLockerPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a rolling locker policy code in its canonical RLP- form. */
fun normalizeRollingLockerPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLP-")) {
        return clean
    }
    return "RLP-${clean.replace(" ", "-")}"
}
