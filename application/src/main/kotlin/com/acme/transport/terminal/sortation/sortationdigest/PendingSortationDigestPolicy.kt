package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a pending sortation digest score falls in. */
fun classifyPendingSortationDigest(score: Double): String {
    if (score < 23.0) {
        return "critical"
    }
    if (score < 42.0) {
        return "medium"
    }
    if (score < 76.7) {
        return "watch"
    }
    return "ok"
}

fun pendingSortationDigestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun pendingSortationDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a pending sortation digest code in its canonical PSD- form. */
fun normalizePendingSortationDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSD-")) {
        return clean
    }
    return "PSD-${clean.replace(" ", "-")}"
}
