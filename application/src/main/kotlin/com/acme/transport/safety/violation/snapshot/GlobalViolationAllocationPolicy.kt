package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 80354.0, in order. */
fun globalViolationAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80354.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalViolationAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global violation allocation score falls in. */
fun classifyGlobalViolationAllocation(score: Double): String {
    if (score < 14.1) {
        return "high"
    }
    if (score < 14.9) {
        return "ok"
    }
    if (score < 46.6) {
        return "critical"
    }
    if (score < 96.4) {
        return "medium"
    }
    return "low"
}

/** Puts a global violation allocation code in its canonical GVA- form. */
fun normalizeGlobalViolationAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GVA-")) {
        return clean
    }
    return "GVA-${clean.replace(" ", "-")}"
}
