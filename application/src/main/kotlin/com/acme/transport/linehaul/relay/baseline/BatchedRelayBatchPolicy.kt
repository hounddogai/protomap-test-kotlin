package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a batched relay batch code in its canonical BRB- form. */
fun normalizeBatchedRelayBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRB-")) {
        return clean
    }
    return "BRB-${clean.replace(" ", "-")}"
}

fun batchedRelayBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedRelayBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
