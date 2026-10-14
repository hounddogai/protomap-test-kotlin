package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a inspection mapping code in its canonical IM- form. */
fun normalizeInspectionMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IM-")) {
        return clean
    }
    return "IM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 20080.0, in order. */
fun inspectionMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20080.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a inspection mapping score falls in. */
fun classifyInspectionMapping(score: Double): String {
    if (score < 81.1) {
        return "low"
    }
    if (score < 96.0) {
        return "minimal"
    }
    return "elevated"
}
