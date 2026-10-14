package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 99859.0, in order. */
fun sortationVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99859.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun sortationVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a sortation variance code in its canonical SV- form. */
fun normalizeSortationVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SV-")) {
        return clean
    }
    return "SV-${clean.replace(" ", "-")}"
}
