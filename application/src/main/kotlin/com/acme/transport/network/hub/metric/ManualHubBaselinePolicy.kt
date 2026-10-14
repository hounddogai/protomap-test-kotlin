package com.acme.transport.network.hub.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 58177.0, in order. */
fun manualHubBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58177.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualHubBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual hub baseline code in its canonical MHB- form. */
fun normalizeManualHubBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MHB-")) {
        return clean
    }
    return "MHB-${clean.replace(" ", "-")}"
}
