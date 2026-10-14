package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalServiceAreaThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun regionalServiceAreaThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 20693.0, in order. */
fun regionalServiceAreaThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20693.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional service area threshold code in its canonical RSAT- form. */
fun normalizeRegionalServiceAreaThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RSAT-")) {
        return clean
    }
    return "RSAT-${clean.replace(" ", "-")}"
}
