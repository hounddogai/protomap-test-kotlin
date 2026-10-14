package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalZoneQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a regional zone quota code in its canonical RZQ- form. */
fun normalizeRegionalZoneQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RZQ-")) {
        return clean
    }
    return "RZQ-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 12861.0, in order. */
fun regionalZoneQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12861.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
