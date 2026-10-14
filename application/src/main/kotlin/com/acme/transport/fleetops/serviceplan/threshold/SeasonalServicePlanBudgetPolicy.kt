package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalServicePlanBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a seasonal service plan budget code in its canonical SSPB- form. */
fun normalizeSeasonalServicePlanBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSPB-")) {
        return clean
    }
    return "SSPB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 80441.0, in order. */
fun seasonalServicePlanBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80441.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun seasonalServicePlanBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
