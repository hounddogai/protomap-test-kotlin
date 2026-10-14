package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tiered cutoff reservation code in its canonical TCR- form. */
fun normalizeTieredCutoffReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCR-")) {
        return clean
    }
    return "TCR-${clean.replace(" ", "-")}"
}

fun tieredCutoffReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun tieredCutoffReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 93700.0, in order. */
fun tieredCutoffReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93700.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
