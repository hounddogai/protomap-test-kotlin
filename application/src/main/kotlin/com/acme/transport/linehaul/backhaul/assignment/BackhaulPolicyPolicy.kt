package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 86668.0, in order. */
fun backhaulPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86668.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun backhaulPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a backhaul policy code in its canonical BP- form. */
fun normalizeBackhaulPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BP-")) {
        return clean
    }
    return "BP-${clean.replace(" ", "-")}"
}

fun backhaulPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
