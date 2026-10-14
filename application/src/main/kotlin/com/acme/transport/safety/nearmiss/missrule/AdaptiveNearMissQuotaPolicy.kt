package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 23371.0, in order. */
fun adaptiveNearMissQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23371.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveNearMissQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a adaptive near miss quota code in its canonical ANMQ- form. */
fun normalizeAdaptiveNearMissQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ANMQ-")) {
        return clean
    }
    return "ANMQ-${clean.replace(" ", "-")}"
}
