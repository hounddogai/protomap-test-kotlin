package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun legacyCoachingWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 46131.0, in order. */
fun legacyCoachingWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46131.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy coaching window code in its canonical LCW- form. */
fun normalizeLegacyCoachingWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCW-")) {
        return clean
    }
    return "LCW-${clean.replace(" ", "-")}"
}
