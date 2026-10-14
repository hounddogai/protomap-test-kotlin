package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingHoldTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 98850.0, in order. */
fun pendingHoldTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98850.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending hold tier code in its canonical PHT- form. */
fun normalizePendingHoldTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PHT-")) {
        return clean
    }
    return "PHT-${clean.replace(" ", "-")}"
}
