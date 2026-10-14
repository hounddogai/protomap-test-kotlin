package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientNearMissPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 57664.0, in order. */
fun lenientNearMissPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57664.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lenient near miss policy code in its canonical LNMP- form. */
fun normalizeLenientNearMissPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNMP-")) {
        return clean
    }
    return "LNMP-${clean.replace(" ", "-")}"
}

fun lenientNearMissPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
