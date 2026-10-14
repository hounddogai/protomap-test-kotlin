package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a lenient service area threshold code in its canonical LSAT- form. */
fun normalizeLenientServiceAreaThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSAT-")) {
        return clean
    }
    return "LSAT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 94667.0, in order. */
fun lenientServiceAreaThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94667.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientServiceAreaThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun lenientServiceAreaThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
