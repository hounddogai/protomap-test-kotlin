package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a adaptive cutoff variance code in its canonical ACV- form. */
fun normalizeAdaptiveCutoffVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACV-")) {
        return clean
    }
    return "ACV-${clean.replace(" ", "-")}"
}

fun adaptiveCutoffVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 24887.0, in order. */
fun adaptiveCutoffVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24887.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveCutoffVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
