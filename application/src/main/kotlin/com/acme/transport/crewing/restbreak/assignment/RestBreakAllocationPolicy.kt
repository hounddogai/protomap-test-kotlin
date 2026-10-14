package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a rest break allocation score falls in. */
fun classifyRestBreakAllocation(score: Double): String {
    if (score < 61.8) {
        return "low"
    }
    if (score < 64.3) {
        return "minimal"
    }
    return "medium"
}

fun restBreakAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a rest break allocation code in its canonical RBA- form. */
fun normalizeRestBreakAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBA-")) {
        return clean
    }
    return "RBA-${clean.replace(" ", "-")}"
}
