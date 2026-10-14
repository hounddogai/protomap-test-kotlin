package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a ev charge group code in its canonical ECG- form. */
fun normalizeEvChargeGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECG-")) {
        return clean
    }
    return "ECG-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 2025.0, in order. */
fun evChargeGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2025.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun evChargeGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
