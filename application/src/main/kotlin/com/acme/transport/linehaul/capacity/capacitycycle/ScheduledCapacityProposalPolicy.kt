package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledCapacityProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun scheduledCapacityProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a scheduled capacity proposal code in its canonical SCP- form. */
fun normalizeScheduledCapacityProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCP-")) {
        return clean
    }
    return "SCP-${clean.replace(" ", "-")}"
}
