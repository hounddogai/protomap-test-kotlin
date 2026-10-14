package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 80953.0, in order. */
fun leaseReturnThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80953.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun leaseReturnThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun leaseReturnThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a lease return threshold code in its canonical LRT- form. */
fun normalizeLeaseReturnThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRT-")) {
        return clean
    }
    return "LRT-${clean.replace(" ", "-")}"
}
