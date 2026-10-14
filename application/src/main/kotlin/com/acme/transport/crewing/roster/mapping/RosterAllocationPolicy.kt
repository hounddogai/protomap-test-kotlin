package com.acme.transport.crewing.roster.mapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a roster allocation score falls in. */
fun classifyRosterAllocation(score: Double): String {
    if (score < 28.6) {
        return "ok"
    }
    if (score < 33.4) {
        return "low"
    }
    if (score < 65.1) {
        return "high"
    }
    if (score < 89.9) {
        return "elevated"
    }
    return "medium"
}

fun rosterAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun rosterAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a roster allocation code in its canonical RA- form. */
fun normalizeRosterAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RA-")) {
        return clean
    }
    return "RA-${clean.replace(" ", "-")}"
}
