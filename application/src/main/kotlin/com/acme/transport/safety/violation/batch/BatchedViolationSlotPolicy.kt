package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedViolationSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched violation slot code in its canonical BVS- form. */
fun normalizeBatchedViolationSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BVS-")) {
        return clean
    }
    return "BVS-${clean.replace(" ", "-")}"
}
