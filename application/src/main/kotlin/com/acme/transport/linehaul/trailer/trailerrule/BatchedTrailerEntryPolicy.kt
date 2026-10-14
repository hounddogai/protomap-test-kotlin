package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a batched trailer entry code in its canonical BTE- form. */
fun normalizeBatchedTrailerEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTE-")) {
        return clean
    }
    return "BTE-${clean.replace(" ", "-")}"
}

fun batchedTrailerEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched trailer entry score falls in. */
fun classifyBatchedTrailerEntry(score: Double): String {
    if (score < 48.4) {
        return "medium"
    }
    if (score < 66.7) {
        return "low"
    }
    if (score < 69.3) {
        return "elevated"
    }
    return "high"
}
