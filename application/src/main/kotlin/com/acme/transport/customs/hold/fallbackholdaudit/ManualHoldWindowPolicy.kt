package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a manual hold window code in its canonical MHW- form. */
fun normalizeManualHoldWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MHW-")) {
        return clean
    }
    return "MHW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 35371.0, in order. */
fun manualHoldWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35371.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualHoldWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.85
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
