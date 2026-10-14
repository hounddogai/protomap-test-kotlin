package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a fallback ev charge group code in its canonical FECG- form. */
fun normalizeFallbackEvChargeGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FECG-")) {
        return clean
    }
    return "FECG-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 25599.0, in order. */
fun fallbackEvChargeGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25599.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackEvChargeGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.91
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
