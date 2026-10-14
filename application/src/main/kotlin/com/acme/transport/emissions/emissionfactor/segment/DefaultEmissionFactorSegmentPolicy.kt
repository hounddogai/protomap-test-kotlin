package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a default emission factor segment score falls in. */
fun classifyDefaultEmissionFactorSegment(score: Double): String {
    if (score < 25.8) {
        return "minimal"
    }
    if (score < 57.3) {
        return "watch"
    }
    return "ok"
}

/** Puts a default emission factor segment code in its canonical DEFS- form. */
fun normalizeDefaultEmissionFactorSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DEFS-")) {
        return clean
    }
    return "DEFS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 2752.0, in order. */
fun defaultEmissionFactorSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2752.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultEmissionFactorSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
