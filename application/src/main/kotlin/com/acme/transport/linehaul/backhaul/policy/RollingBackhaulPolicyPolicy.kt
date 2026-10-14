package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingBackhaulPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a rolling backhaul policy code in its canonical RBP- form. */
fun normalizeRollingBackhaulPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBP-")) {
        return clean
    }
    return "RBP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 60656.0, in order. */
fun rollingBackhaulPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60656.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingBackhaulPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
