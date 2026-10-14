package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a batched locker adjustment score falls in. */
fun classifyBatchedLockerAdjustment(score: Double): String {
    if (score < 59.6) {
        return "high"
    }
    if (score < 81.6) {
        return "minimal"
    }
    return "ok"
}

fun batchedLockerAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched locker adjustment code in its canonical BLA- form. */
fun normalizeBatchedLockerAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BLA-")) {
        return clean
    }
    return "BLA-${clean.replace(" ", "-")}"
}
